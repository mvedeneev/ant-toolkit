package com.anttoolkit.documentum.tasks.lifecycle;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.*;

import org.apache.tools.ant.*;

import com.anttoolkit.documentum.common.*;

public class AttachTask
		extends GenericDocbaseTask
{
	private String m_objectId = null;
	private String m_lifecycle = null;
	private IDfId m_lifecycleId = null;
	private String m_state = "0";

	public void setObjectId(String id)
	{
		m_objectId = id;
	}

	public void setLifecycle(String lifecycle)
	{
		m_lifecycle = lifecycle;
	}

	public void setState(String state)
	{
		m_state = state;
	}

	public void doWork() throws BuildException
	{
		if (m_objectId == null || m_lifecycle == null)
		{
			throw new BuildException("objectId and lifecycle should be specified");
		}

		m_lifecycleId = DfId.isObjectId(m_lifecycle) ? new DfId(m_lifecycle) :
				LifecycleHelper.getLifecycleId(getSession(), m_lifecycle);

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
			sysObj.attachPolicy(m_lifecycleId, m_state, "");
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to attach lifecycle '" + m_lifecycle + "' " +
					"to object " + objectId, e);
		}
	}
}
