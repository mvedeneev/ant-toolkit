package com.anttoolkit.documentum.common;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import com.documentum.com.*;
import com.documentum.operations.*;

import java.util.*;
import java.text.*;
import java.util.concurrent.*;

import org.apache.tools.ant.*;

public class DocbaseObject
{
	public static final String UNKNOWN_CONTENT_TYPE = "unknown";

	private static final String DQL_GET_FOLDER_ID_BY_PATH = "select r_object_id from dm_folder where any r_folder_path=''{0}''";

	private static final int FOLDER_IDS_CACHE_MAX_SIZE = 2048;

	private boolean m_useCheckinOnSave = false;

	private String m_type = null;
	private String m_objectId = null;
	private String m_folder = null;
	private String m_contentFile = null;
	private String m_contentType = null;

	private IDfType m_dfType = null;

	private List m_properties = new LinkedList();
	private DocbaseSession m_session = null;

	private String m_lastCommitedObjectId = null;

	private static Map<String, String> m_folderIdsCache = new ConcurrentHashMap<String, String>();

	private static Map m_types = new ConcurrentHashMap();


	public void setCheckinOperationFlag()
	{
		m_useCheckinOnSave = true;
	}

	public void addProperty(DocbaseObjectProperty property)
	{
		m_properties.add(property);
	}

	public DocbaseObjectProperty getProperty(String name)
	{
		for (Iterator iter = m_properties.iterator(); iter.hasNext();)
		{
			DocbaseObjectProperty property = (DocbaseObjectProperty)iter.next();
			if (property.getName().equals(name))
			{
				return property;	
			}
		}

		return null;
	}

	public void setContentFile(String file)
	{
		m_contentFile = file;
	}

	public String getContentFile()
	{
		return m_contentFile;
	}

	public void setContentType(String type)
	{
		m_contentType = type;
	}

	public String getContentType()
	{
		return m_contentType;
	}

	public void clearProperties()
	{
		m_properties.clear();
	}

	public void clear()
	{
		m_type = null;
		m_objectId = null;
		m_folder = null;
		m_dfType = null;

		clearProperties();
	}

	public void setType(String type)
	{
		m_type = type;

		try
		{
			if (m_dfType != null && !m_type.equals(m_dfType.getName()))
			{
				m_dfType = null;
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get type name from IDfType object.\r\n" + e.toString());
		}
	}

	public String getType()
	{
		return m_type;
	}

	public String getLastCommitedObjectId()
	{
		return m_lastCommitedObjectId;
	}

	public void setObjectId(String objectId)
	{
		m_objectId = objectId;
	}

	public String getObjectId()
	{
		return m_objectId;
	}

	public void setFolder(String folder)
	{
		m_folder = folder;
	}

	public void doWork()
			throws BuildException
	{
		if (m_objectId != null && m_type != null)
		{
			throw new BuildException("You should specify objectId or type arguments, but not both");
		}

		if ((m_objectId == null || m_objectId.trim().length() == 0 ||
			m_objectId.trim().equals(DfId.DF_NULLID_STR)) &&
			(m_type == null || m_type.trim().length() == 0))
		{
			throw new BuildException("Object objectId or type argument shoul be specified");
		}

		//create new object
		if (m_type != null)
		{
			commitObject(createDfObject());
			return;
		}

		//update single object
		if (m_objectId.length() == 16)
		{
			commitObject(getDfObject(m_objectId));
			return;
		}

		//update multiple objects
		commitBatch(m_objectId);
	}

	public void setSession(DocbaseSession session)
	{
		m_session = session;
	}

	private IDfPersistentObject createDfObject()
			throws BuildException
	{
		if (m_session == null)
		{
			throw new BuildException("DocbaseSession wasn't set for DocbaseObject");
		}

		initObjectPropertiesMetadata();

		try
		{
			IDfPersistentObject obj =  m_session.createDfObject(m_type);
			m_objectId = obj.getObjectId().toString();
			return obj;
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get object id for created object", e);
		}
	}

	private IDfPersistentObject getDfObject(String objectId)
			throws BuildException
	{
		if (m_session == null)
		{
			throw new BuildException("DocbaseSession wasn't set for DocbaseObject");
		}

		IDfPersistentObject obj = m_session.getDfObject(objectId);

		try
		{
			this.setType(obj.getType().getName());
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get object type name\r\n" + e.toString());
		}

		initObjectPropertiesMetadata();

		return obj;
	}

	private void commitObject(IDfPersistentObject obj)
			throws BuildException
	{
		for (Iterator iter = m_properties.iterator(); iter.hasNext();)
		{
			DocbaseObjectProperty prop = (DocbaseObjectProperty)iter.next();
			Object value = prop.resolvePropertyValue(m_session);

			try
			{
				switch (prop.getDataType())
				{
					case IDfType.DF_BOOLEAN:
						if (prop.isRepeating())
						{
							obj.appendBoolean(prop.getName(), ((Boolean)value).booleanValue());
						}
						else
						{
							obj.setBoolean(prop.getName(), ((Boolean)value).booleanValue());
						}
						break;
					case IDfType.DF_DOUBLE:
						if (prop.isRepeating())
						{
							obj.appendDouble(prop.getName(), ((Double)value).doubleValue());
						}
						else
						{
							obj.setDouble(prop.getName(), ((Double)value).doubleValue());
						}
						break;
					case IDfType.DF_ID:
						if (prop.isRepeating())
						{
							obj.appendId(prop.getName(), (IDfId)value);
						}
						else
						{
							obj.setId(prop.getName(), (IDfId)value);
						}
						break;
					case IDfType.DF_INTEGER:
						if (prop.isRepeating())
						{
							obj.appendInt(prop.getName(), ((Integer)value).intValue());
						}
						else
						{
							obj.setInt(prop.getName(), ((Integer)value).intValue());
						}
						break;
					case IDfType.DF_STRING:
						if (prop.isRepeating())
						{
							obj.appendString(prop.getName(), (String)value);
						}
						else
						{
							obj.setString(prop.getName(), (String)value);
						}
						break;
					case IDfType.DF_TIME:
						if (prop.isRepeating())
						{
							obj.appendTime(prop.getName(), (IDfTime)value);
						}
						else
						{
							obj.setTime(prop.getName(), (IDfTime)value);
						}
						break;
				}
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to set object property " + prop.getName() + "\r\n" + e.toString());
			}
		}

		linkDfObject(obj);
		setDfObjectContent(obj);
		saveDfObject(obj);
	}

	private void commitBatch(String query)
			throws BuildException
	{
		if (m_session == null)
		{
			throw new BuildException("DocbaseSession wasn't set for DocbaseObject");
		}

		IDfCollection coll = null;
		try
		{
			try
			{
				coll = DqlHelper.executeReadQuery(m_session, query);
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to execute query \"" + query + "\" to get id's collection\r\n" + e.toString());
			}

			try
			{
				while (coll.next())
				{
					IDfId objectId = coll.getValueAt(0).asId();
					commitObject(getDfObject(objectId.getId()));
				}
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to perform next interation on IDfCollection object to get object id\r\n" + e.toString());
			}
		}
		finally
		{
			DqlHelper.closeCollection(coll);
		}
	}

	private void linkDfObject(IDfPersistentObject obj)
			throws BuildException
	{
		if (m_folder == null)
		{
			return;
		}

		if (!(obj instanceof IDfSysObject))
		{
			throw new BuildException("Object of type " + m_type + " isn't inherited from dm_sysobject " +
					"so it couldn't be link to any folder");
		}

		try
		{
			((IDfSysObject)obj).link(getFolderId(m_folder));
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to link object to folder " + m_folder + "\r\n" + e.toString());
		}
	}

	private void setDfObjectContent(IDfPersistentObject obj)
			throws BuildException
	{
		if (!(obj instanceof IDfSysObject) ||
			m_contentFile == null || m_contentFile.trim().length() == 0)
		{
			return;
		}

		removeDfObjectContent((IDfSysObject)obj);

		if (m_contentFile.trim().toLowerCase().equals("null"))
		{
			return;
		}

		try
		{
			String contentType = m_contentType != null ? m_contentType : getFileContentType(m_contentFile);
			((IDfSysObject)obj).setFileEx(m_contentFile, contentType, 0, null);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to set content file \"" + m_contentFile + "\" for object " + m_objectId, e);
		}
	}

	private void removeDfObjectContent(IDfSysObject obj)
			throws BuildException
	{
		try
		{
			if (obj.getContentSize() == 0 || obj.isNew())
			{
				return;
			}

			obj.removeContent(0);
			obj.setContentType("");
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to remove content for object " + m_objectId, e);
		}
	}

	private void saveDfObject(IDfPersistentObject obj)
			throws BuildException
	{
		if (!m_useCheckinOnSave || !(obj instanceof IDfSysObject))
		{
			m_lastCommitedObjectId = DocbaseSession.saveDfObject(obj);
		}
		else
		{
			m_lastCommitedObjectId = DocbaseSession.checkinDfObject((IDfSysObject)obj);
		}
	}

	private void initObjectPropertiesMetadata()
			throws BuildException
	{
		if (m_properties.isEmpty())
		{
			return;
		}

		for (Iterator iter = m_properties.iterator(); iter.hasNext();)
		{
			DocbaseObjectProperty prop = (DocbaseObjectProperty)iter.next();
			if (prop.isMetadataInitialized())
			{
				continue;
			}

			try
			{
				IDfAttr dfAttr = getDfAttr(prop.getName());
				if (dfAttr == null)
				{
					throw new BuildException("Attribute '" + prop.getName() + "' for type '" + getType() + "' doesn't exist");
				}

				prop.setMetadata(dfAttr.getDataType(), dfAttr.getLength(), dfAttr.isRepeating());
			}
			catch (DfException e)
			{
				throw new BuildException("Error occured while trying to get metadata for attribute " + prop.getName());
			}
		}
	}

	private IDfType getDfType()
			throws DfException
	{
		if (m_dfType != null)
		{
			return m_dfType;
		}

		m_dfType = m_session.getDfSession().getType(m_type);

		return m_dfType;
	}

	private IDfAttr getDfAttr(String attrName)
			throws DfException
	{
		if (!m_types.containsKey(m_type))
		{
			m_types.put(m_type, new Hashtable());
		}

		Map cachedAttributes = (Map)m_types.get(m_type);
		if (cachedAttributes.containsKey(attrName))
		{
			return (IDfAttr)cachedAttributes.get(attrName);
		}

		IDfAttr dfAttr = this.getDfType().getTypeAttr(this.getDfType().findTypeAttrIndex(attrName));
		cachedAttributes.put(attrName, dfAttr);

		return dfAttr;
	}

	private String getFileContentType(String fileName)
	{
		if(fileName == null)
		{
			return null;
		}

		try
		{
			IDfClientX clientx = new DfClientX();
			IDfFormatRecognizer recognizer = clientx.getFormatRecognizer(m_session.getDfSession(), fileName, "");

			return recognizer.getDefaultSuggestedFileFormat() != null ? recognizer.getDefaultSuggestedFileFormat() : UNKNOWN_CONTENT_TYPE;
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get content format for file \"" + fileName + "\"", e);
		}
	}

	private String getFolderId(String folderExpression)
	{
		if (folderExpression == null || folderExpression.trim().length() == 0)
		{
			return null;
		}

		if (m_folderIdsCache.containsKey(folderExpression))
		{
			return m_folderIdsCache.get(folderExpression);
		}

		if (DfId.isObjectId(folderExpression))
		{
			return folderExpression;
		}

		if (!folderExpression.contains("/"))
		{
			throw new BuildException("Incorrect folder expression \"" + folderExpression + "\" specified to link object");
		}

		String dqlQuery = MessageFormat.format(DQL_GET_FOLDER_ID_BY_PATH, folderExpression);
		String folderId = null;

		try
		{
			folderId = DqlHelper.getStringParamFromFirstString(m_session, dqlQuery);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get folderId for folder \"" + folderExpression + "\"", e);	
		}

		if (m_folderIdsCache.size() == FOLDER_IDS_CACHE_MAX_SIZE)
		{
			m_folderIdsCache.clear();
		}

		m_folderIdsCache.put(folderExpression, folderId);

		return folderId;
	}
}
