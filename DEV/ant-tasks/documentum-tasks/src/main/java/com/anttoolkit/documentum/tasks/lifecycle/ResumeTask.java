package com.anttoolkit.documentum.tasks.lifecycle;

import org.apache.tools.ant.*;

import com.anttoolkit.documentum.common.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

public class ResumeTask
		extends GenericDocbaseTask
{
	private String m_objectId = null;
	private String m_toState = null;
	private boolean m_toBase = false;

	public void setObjectId(String id)
	{
		m_objectId = id;
	}

	public void setToState(String state)
	{
		m_toState = state;
	}

	public void setToBase(boolean toBase)
	{
		m_toBase = toBase;
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
			sysObj.resume(m_toState, m_toBase, false, false);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to resume object " + objectId, e);
		}
	}
}
