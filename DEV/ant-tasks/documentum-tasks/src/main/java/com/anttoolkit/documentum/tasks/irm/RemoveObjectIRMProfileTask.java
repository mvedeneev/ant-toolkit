package com.anttoolkit.documentum.tasks.irm;

import org.apache.tools.ant.*;

import com.documentum.fc.common.*;
import com.documentum.services.irm.*;

public class RemoveObjectIRMProfileTask
		extends GenericIRMTask
{
	private String m_objectId = null;

	public void setObjectId(String objectId)
	{
		m_objectId = objectId;
	}

	public void doWork()
			throws BuildException
	{
		if (m_objectId == null)
		{
			throw new BuildException("objectId and IRM profile should be specified");
		}

		processObjectsBatch(m_objectId);
	}

	protected void processSingleObjectFromBatch(int iteration, IDfId objectId)
			throws BuildException
	{
		IIRMService irmService = getIRMService();

		try
		{
			irmService.removeObjectIRMProfile(getSession().getDfSession(), objectId);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to remove IRM profile for object r_object_id=" + m_objectId, e);
		}
	}
}
