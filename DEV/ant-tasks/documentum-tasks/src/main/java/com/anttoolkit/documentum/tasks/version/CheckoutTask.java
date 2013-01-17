package com.anttoolkit.documentum.tasks.version;

import org.apache.tools.ant.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

import com.anttoolkit.documentum.common.*;

public class CheckoutTask extends GenericDocbaseTask
{
	private String m_objectId = null;

	public void setObjectId(String objectId)
	{
		m_objectId = objectId;
	}

	public void doWork() throws BuildException
	{
		if (m_objectId == null || m_objectId.trim().length() == 0)
		{
			throw new BuildException("Object id should be specified");
		}

		processObjectsBatch(m_objectId);
	}

	protected void processSingleObjectFromBatch(int iteration, IDfId objectId)
			throws BuildException
	{
		IDfPersistentObject obj = getDfObject(objectId);
		if (!(obj instanceof IDfSysObject))
		{
			throw new BuildException("Object with r_object_id=" + objectId.getId() + " is not an instance of IDfSysObject");
		}

		DocbaseSession.checkoutDfObject((IDfSysObject)obj);
	}
}
