package com.anttoolkit.documentum.tasks.object;

import java.util.*;
import java.util.concurrent.*;

import com.documentum.com.*;
import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import com.documentum.operations.*;
import org.apache.tools.ant.*;

import com.anttoolkit.documentum.common.*;


public class SetFileTask
	extends GenericDocbaseTask
{
	private String m_objectId = null;
	private String m_file = null;
	private String m_contentType = null;

	private static Map<String, String> m_fileContentTypes = new ConcurrentHashMap<String, String>();

	public void setObjectId(String objectId)
	{
		m_objectId = objectId;
	}

	public void setFile(String file)
	{
		m_file = getFileFullPath(file);
	}

	public void setContentType(String type)
	{
		m_contentType = type;
	}

	public void doWork()
			throws BuildException
	{
		if (m_objectId == null)
		{
			return;
		}

		IDfSysObject sysObj = null;

		try
		{
			IDfPersistentObject obj = getDfObject(m_objectId);
			if (!(obj instanceof IDfSysObject))
			{
				if (failOnError())
				{
					throw new BuildException("Can't change object file for object with id='" + m_objectId +
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

		setDfObjectContent(sysObj);
	}

	private String getFileContentType()
	{
		int index = m_file.lastIndexOf('.');
		if (index == -1)
		{
			return DocbaseObject.UNKNOWN_CONTENT_TYPE;
		}

		String fileExtension = m_file.substring(index + 1);

		String contentType = m_fileContentTypes.get(fileExtension);
		if (contentType != null)
		{
			return contentType;
		}

		try
		{
			IDfClientX clientx = new DfClientX();
			IDfFormatRecognizer recognizer = clientx.getFormatRecognizer(getSession().getDfSession(), m_file, "");

			contentType = recognizer.getDefaultSuggestedFileFormat() != null ? recognizer.getDefaultSuggestedFileFormat() : DocbaseObject.UNKNOWN_CONTENT_TYPE;
			m_fileContentTypes.put(fileExtension, contentType);

			return contentType;
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get content format for file \"" + m_file + "\"", e);
		}
	}

	private void setDfObjectContent(IDfSysObject obj)
			throws BuildException
	{
		removeDfObjectContent(obj);

		if (m_file == null || m_file.trim().length() == 0 ||
			m_file.trim().toLowerCase().equals("null"))
		{
			return;
		}

		try
		{
			String contentType = m_contentType != null ? m_contentType : getFileContentType();

			obj.setFileEx(m_file, contentType, 0, null);
			obj.save();
		}
		catch (DfException e)
		{
			String errorMsg = "Failed to set content file \"" + m_file + "\" for object " + m_objectId + "\r\n" + e.toString();

			if (failOnError())
			{
				throw new BuildException(errorMsg, e);
			}

			log(errorMsg, e, Project.MSG_INFO);
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
			String errorMsg = "Failed to remove content for object " + m_objectId + "\r\n" + e.toString();

			if (failOnError())
			{
				throw new BuildException(errorMsg, e);
			}

			log(errorMsg, e, Project.MSG_INFO);
		}
	}
}
