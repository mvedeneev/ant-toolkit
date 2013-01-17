package com.anttoolkit.documentum.tasks.object;

import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.tools.ant.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

import com.anttoolkit.documentum.common.*;

public class GetFileTask
		extends GenericDocbaseTask
{
	private static final String DQL_GET_FORMAT_EXTENSION = "select dos_extension from dm_format where name=''{0}''";

	private static Map<String, String> m_formatExtensions = new ConcurrentHashMap<String, String>();

	private String m_objectId = null;
	private String m_folder = null;
	private String m_fileName = null;
	private String m_fileNameProperty = null;
	private boolean m_resolveFileExtension = true;

	public void setObjectId(String objectId)
	{
		m_objectId = objectId;
	}

	public void setFolder(String folder)
	{
		m_folder = folder;
	}

	public void setFile(String name)
	{
		m_fileName = name;
	}

	public void setUseObjectPropertyAsFileName(String property)
	{
		m_fileNameProperty = property;
	}

	public void setResolveFileExtension(boolean resolve)
	{
		m_resolveFileExtension = resolve;
	}

	public void doWork() throws BuildException
	{
		if (m_objectId == null)
		{
			return;
		}

		if (m_fileName == null && m_fileNameProperty == null)
		{
			throw new BuildException("Either fileName or fileNameProperty should be specified");
		}

		processObjectsBatch(m_objectId);
	}

	protected void processSingleObjectFromBatch(int iteration, IDfId objectId)
			throws BuildException
	{
		IDfSysObject sysObj = null;

		try
		{
			IDfPersistentObject obj = getDfObject(objectId);
			if (!(obj instanceof IDfSysObject))
			{
				if (failOnError())
				{
					throw new BuildException("Can't get object file for object with id='" + objectId +
						"' because object is not an instance of the IDfSysObject");
				}

				return;
			}

			sysObj = (IDfSysObject)obj;
		}
		catch (BuildException e)
		{
			if (failOnError())
			{
				throw e;
			}

			log(e, Project.MSG_WARN);

			return;
		}

		try
		{
			if (sysObj.getContentSize() == 0)
			{
				String msg = "Object " + objectId + " doesn't have content";

				if (!failOnError())
				{
					log(msg, Project.MSG_WARN);
					return;
				}

				throw new BuildException(msg);
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to verify object " + objectId + " content size", e);
		}

		String fileFullPath = buildFileFullPath(sysObj);

		try
		{
			sysObj.getFile(fileFullPath);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get file for object " + objectId, e);
		}
	}

	private String buildFileFullPath(IDfSysObject sysObj)
	{
		String fullPath = "";

		if (m_folder != null)
		{
			fullPath = m_folder;
			fullPath = fullPath.trim().replace("\\", "/");

			if (fullPath.charAt(fullPath.length() - 1) != '/')
			{
				fullPath = fullPath + "/";
			}
		}

		if (m_fileName != null)
		{
			return fullPath + m_fileName;
		}

		if (m_fileNameProperty != null)
		{
			try
			{
				fullPath = fullPath + sysObj.getString(m_fileNameProperty);
				fullPath = fullPath.trim();
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to get object property '" + m_fileNameProperty + "' value", e);
			}
		}

		if (m_resolveFileExtension)
		{
			String extension = getFormatExtension(sysObj).toLowerCase();

			if (!fullPath.endsWith(extension))
			{
				fullPath = fullPath + extension;
			}
		}

		return fullPath;
	}

	private String getFormatExtension(IDfSysObject sysObj)
	{
		try
		{
			String contentType = sysObj.getContentType();
			if (m_formatExtensions.containsKey(contentType))
			{
				return m_formatExtensions.get(contentType);
			}


			String query = MessageFormat.format(DQL_GET_FORMAT_EXTENSION, contentType);
			String formatExtension = DqlHelper.getStringParamFromFirstString(getSession(), query);

			m_formatExtensions.put(contentType, formatExtension);

			return formatExtension;
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get format extension for object", e);
		}
	}
}
