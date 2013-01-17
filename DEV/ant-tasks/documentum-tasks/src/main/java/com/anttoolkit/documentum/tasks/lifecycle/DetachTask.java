package com.anttoolkit.documentum.tasks.lifecycle;

import com.anttoolkit.documentum.common.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

import org.apache.tools.ant.*;

public class DetachTask
		extends GenericDocbaseTask
{
	private String m_objectId = null;

	public void setObjectId(String id)
	{
		m_objectId = id;
	}

	public void doWork() throws BuildException
	{
		if (m_objectId == null)
		{
			throw new BuildException("objectId should be specified");
		}

		processObjectsBatch(m_objectId);
	}

	protected void processSingleObjectFromBatch(int iteration, IDfId objectId)
			throws BuildException
	{
		IDfPersistentObject obj = getDfObject(objectId);
		if (!(obj instanceof IDfSysObject))
		{
			throw new BuildException("Object " + objectId + " is not an instance of IDfSysObject");
		}

		IDfSysObject sysObj = (IDfSysObject)obj;

		try
		{
			sysObj.detachPolicy();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to detach lifecycle " +
					"from object " + objectId, e);
		}
	}
}
