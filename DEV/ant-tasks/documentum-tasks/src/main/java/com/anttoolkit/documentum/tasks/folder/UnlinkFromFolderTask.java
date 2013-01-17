package com.anttoolkit.documentum.tasks.folder;

import org.apache.tools.ant.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

import com.anttoolkit.documentum.common.*;

public class UnlinkFromFolderTask extends GenericDocbaseTask
{
	private String m_objectId = null;
	private String m_folder = null;

	public void setObjectId(String objectId)
	{
		m_objectId = objectId;
	}

	public void setFolder(String folder)
	{
		m_folder = folder;
	}

	public void doWork()
			throws BuildException
	{
		if (m_objectId == null || m_folder == null)
		{
			throw new BuildException("objectId and folder should be specified");
		}

		processObjectsBatch(m_objectId);
	}

	protected void processSingleObjectFromBatch(int iteration, IDfId objectId)
			throws BuildException
	{
		IDfPersistentObject obj = getDfObject(objectId);
		if (!(obj instanceof IDfSysObject))
		{
			throw new BuildException("Can't unlink object r_object_id=" + objectId + " from folder='" + m_folder + "', because it's not of type IDfSysObject");
		}

		IDfSysObject sysObj = (IDfSysObject)obj;

		try
		{
			sysObj.unlink(m_folder);
			sysObj.save();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to unlink object r_object_id=" + objectId + " from folder='" + m_folder + "'", e);
		}
	}
}
