package com.anttoolkit.documentum.tasks.bof;

import com.anttoolkit.documentum.common.*;

import org.apache.tools.ant.*;

import java.util.*;
import java.text.*;
import java.net.*;
import java.io.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

public class BofDeploymentTask
		extends GenericDocbaseTask
		implements IContentStorage
{
	private static final String JAR_FILE_URL_TEMPLATE = "jar:file:{0}!/";
	private static final String DQL_GET_DOC_APP_OBJECTS = "select r_object_id, object_name, r_object_type " +
			"from dm_sysobject where r_object_id in (select application_object_id from dm_application " +
			"where object_name = ''{0}'') and r_object_type in (''dmc_module'', ''dmc_java_library'') " +
			"order by r_object_type desc";

	private static final String DQL_GET_MODULE_AND_JAVA_LIBRARY_OBJECTS = "select r_object_id, object_name, r_object_type " +
			"from dm_sysobject where object_name in ({0}) and r_object_type in (''dmc_module'', ''dmc_java_library'') " +
			"order by r_object_type desc";

	private static final String MODULE_TYPE = "dmc_module";
	private static final String JAVA_LIBRARY_TYPE = "dmc_java_library";

	public class FileHandler
	{
		String[] m_relatedNames = null;

		public void setRelatedNames(String names)
		{
			if (names == null || names.trim().length() == 0)
			{
				return;
			}

			String _names = BofDeploymentTask.this.substituteProperties(names);

			m_relatedNames = _names.split(",");
			for (int i = 0; i < m_relatedNames.length; i++)
			{
				m_relatedNames[i] = m_relatedNames[i].trim();
			}
		}

		public void addText(String fileName)
		{
			String _fileName = BofDeploymentTask.this.substituteProperties(fileName);

			String fullPath = BofDeploymentTask.this.getFileFullPath(_fileName);
			int index = fullPath.lastIndexOf(File.separatorChar);
			if (index == -1)
			{
				throw new BuildException("Invalid file name '" + _fileName +
						"' for file specified as '" + fileName + "'");
			}

			File file = new File(fullPath);
			if (!file.exists() || !file.isFile())
			{
				throw new BuildException("File '" + _fileName +
						"' specified as '" + fileName + "' doesn't exist");
			}

			String shortName = fullPath.substring(index + 1).trim();
			if (!BofDeploymentTask.this.m_locaFiles.containsKey(shortName))
			{
				BofDeploymentTask.this.m_locaFiles.put(shortName, fullPath);
			}

			if (m_relatedNames != null && m_relatedNames.length != 0)
			{
				for (String name : m_relatedNames)
				{
					if (!BofDeploymentTask.this.m_locaFiles.containsKey(name))
					{
						BofDeploymentTask.this.m_locaFiles.put(name, fullPath);
					}
				}
			}

			m_relatedNames = null;
		}

		public FileHandler clear()
		{
			m_relatedNames = null;
			return this;
		}
	}

	public class ModuleHandler
	{
		private String m_primaryClass = null;

		public void setPrimaryClass(String className)
		{
			if (className != null && className.trim().length() != 0)
			{
				m_primaryClass = BofDeploymentTask.this.substituteProperties(className.trim());
			}
		}

		public void addText(String moduleName)
		{
			String _moduleName = BofDeploymentTask.this.substituteProperties(moduleName);

			if (_moduleName != null && _moduleName.trim().length() > 0 &&
				!m_modules.containsKey(_moduleName.trim()))
			{
				m_modules.put(_moduleName.trim(), m_primaryClass == null ? "" : m_primaryClass);
			}

			m_primaryClass = null;
		}

		public ModuleHandler clear()
		{
			m_primaryClass = null;
			return this;
		}
	}

	public class JavaLibraryHandler
	{
		public void addText(String libraryName)
		{
			String _libraryName = BofDeploymentTask.this.substituteProperties(libraryName);

			if (_libraryName != null && _libraryName.trim().length() > 0 &&
				!m_javaLibraries.contains(_libraryName.trim()))
			{
				m_javaLibraries.add(_libraryName.trim());
			}
		}
	}

	public DocAppObject putContent(String objectId,
								   String name,
								   String objectType)
	{
		if (m_contentMap.containsKey(objectId))
		{
			return (DocAppObject)m_contentMap.get(objectId);
		}

		String localFile = m_locaFiles.containsKey(name) ? (String)m_locaFiles.get(name) : null;
		Content content = Content.instance(objectId, name, objectType, this.getSession(), localFile);
		m_contentMap.put(objectId, content);

		return content;
	}

	public FileHandler createFile()
	{
		return fileHandler;
	}

	public ModuleHandler createModule()
	{
		return moduleHandler.clear();
	}

	public JavaLibraryHandler createJavaLibrary()
	{
		return javaLibraryHandler;	
	}

	public void setDocApp(String docApp)
	{
		m_docApp = docApp;
	}

	public void setRemovePreviousVersions(boolean removeFlag)
	{
		m_removePreviousVersions = removeFlag;
	}

	public void doWork()
			throws BuildException
	{
		if (m_docApp == null && m_modules.size() == 0 && m_javaLibraries.size() == 0)
		{
			throw new BuildException("DocApp name or module names or java library names should be specified");
		}

		if (m_locaFiles.size() == 0)
		{
			this.log("No files was specified to deploy");
			return;
		}

		initDocAppJarFilesClassLoader();

		populateDocAppObjects();
		populateModulesAndJavaLibraries();

		try
		{
			checkOut();
			update();
		}
		catch (Exception e)
		{
			cancelCheckOut();
			throw new BuildException(e.toString());
		}

		removePreviousVersions();
		
		deinitDocAppJarFilesClassLoader();
	}

	private void populateDocAppObjects()
			throws BuildException
	{
		if (m_docApp == null || m_docApp.trim().length() == 0)
		{
			return;
		}

		try
		{
			populateObjects(MessageFormat.format(DQL_GET_DOC_APP_OBJECTS, new String[] {m_docApp}));
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get DocApp modules\r\n" + e.toString());
		}
	}

	private void populateModulesAndJavaLibraries()
			throws BuildException
	{
		StringBuffer buffer = new StringBuffer();

		Iterator iter = m_modules.keySet().iterator();
		while (iter.hasNext())
		{
			String name = (String)iter.next();

			if (buffer.length() != 0)
			{
				buffer.append(",");
			}

			buffer.append("'").append(name).append("'");
		}

		iter = m_javaLibraries.iterator();
		while (iter.hasNext())
		{
			String name = (String)iter.next();

			if (buffer.length() != 0)
			{
				buffer.append(",");
			}

			buffer.append("'").append(name).append("'");
		}

		if (buffer.length() == 0)
		{
			return;
		}

		try
		{
			populateObjects(MessageFormat.format(DQL_GET_MODULE_AND_JAVA_LIBRARY_OBJECTS, new String[] {buffer.toString()}));
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get specified modules and java libraries\r\n" + e.toString());
		}
	}

	private void populateObjects(String query)
			throws DfException
	{
		IDfCollection coll = null;

		try
		{
			coll = DqlHelper.executeReadQuery(this.getSession(), query);
			
			while (coll.next())
			{
				String objectId = coll.getString(DocAppObject.OBJECT_ID_ATTRIBUTE);
				String name = coll.getString(DocAppObject.NAME_ATTRIBUTE);
				String type = coll.getString(DocAppObject.TYPE_ATTRIBUTE);

				BasicDocAppObject obj;

				if (type.equals(MODULE_TYPE))
				{
					obj = new Module(objectId, name, type, (String)m_modules.get(name), this.getSession(), this);
				}
				else if (type.equals(JAVA_LIBRARY_TYPE))
				{
					obj = new JavaLibrary(objectId, name, type, this.getSession(), this);
				}
				else
				{
					throw new BuildException("Incorrect type=" + type + " for basic DocApp object");
				}

				m_basicObjects.add(obj);
			}
		}
		finally
		{
			DqlHelper.closeCollection(coll);
		}
	}

	private void checkOut()
			throws CheckOutException
	{
		for (Iterator iter = m_basicObjects.iterator(); iter.hasNext();)
		{
			DocAppObject obj = (DocAppObject)iter.next();
			obj.checkOut();
		}
	}

	private void cancelCheckOut()
	{
		for (Iterator iter = m_basicObjects.iterator(); iter.hasNext();)
		{
			DocAppObject obj = (DocAppObject)iter.next();
			obj.cancelCheckOut();
		}
	}

	private void update()
			throws UpdateException
	{
		for (Iterator iter = m_basicObjects.iterator(); iter.hasNext();)
		{
			DocAppObject obj = (DocAppObject)iter.next();
			obj.update();
		}
	}

	private void removePreviousVersions()
	{
		if (!m_removePreviousVersions)
		{
			return;
		}

		for (Iterator iter = m_basicObjects.iterator(); iter.hasNext();)
		{
			BasicDocAppObject obj = (BasicDocAppObject)iter.next();
			obj.removePreviousVersions();
		}
	}

	private URL[] getJarUrls()
			throws BuildException
	{
		ArrayList urls = new ArrayList();
		Set addedUrl = new HashSet();

		for (Iterator iter = m_locaFiles.values().iterator(); iter.hasNext();)
		{
			String fileName = (String)iter.next();
			if (!fileName.endsWith(".jar") || addedUrl.contains(fileName))
			{
				continue;
			}

			try
			{
				urls.add(new URL(MessageFormat.format(JAR_FILE_URL_TEMPLATE, new String[] {fileName})));
			}
			catch (MalformedURLException e)
			{
				throw new BuildException("Failed to get information from " +
						fileName + "\r\n" + e.toString());
			}

			addedUrl.add(fileName);
		}

		if (urls.size() == 0)
		{
			return null;
		}

		return (URL[])urls.toArray(new URL[]{});
	}

	private void initDocAppJarFilesClassLoader()
	{
		URL[] jarUrls = getJarUrls();
		if (jarUrls == null)
		{
			return;
		}

		ClassLoader defaultLoader = Thread.currentThread().getContextClassLoader();
		m_docAppJarClassLoader = new URLClassLoader(jarUrls, defaultLoader);
		Thread.currentThread().setContextClassLoader(m_docAppJarClassLoader);
	}

	private void deinitDocAppJarFilesClassLoader()
	{
		if (m_docAppJarClassLoader != null)
		{
			Thread.currentThread().setContextClassLoader(m_docAppJarClassLoader.getParent());
		}

		m_docAppJarClassLoader = null;
	}

	private URLClassLoader m_docAppJarClassLoader = null;

	private FileHandler fileHandler = new FileHandler();
	private ModuleHandler moduleHandler = new ModuleHandler();
	private JavaLibraryHandler javaLibraryHandler = new JavaLibraryHandler();

	private Map m_locaFiles = new Hashtable();
	private Map m_modules = new Hashtable();
	private List m_javaLibraries = new Vector();

	private String m_docApp = null;
	private boolean m_removePreviousVersions = false;
	private List m_basicObjects = new Vector();
	private Map m_contentMap = new Hashtable();
}
