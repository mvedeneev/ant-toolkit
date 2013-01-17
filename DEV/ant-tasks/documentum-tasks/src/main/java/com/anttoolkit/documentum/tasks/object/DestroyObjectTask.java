package com.anttoolkit.documentum.tasks.object;

import com.documentum.fc.client.*;
import org.apache.tools.ant.*;

import com.documentum.fc.common.*;

import com.anttoolkit.documentum.common.*;

public class DestroyObjectTask
		extends GenericDocbaseTask
{
	private String m_objectId = null;
	private boolean m_allVersions = false;

	public void setObjectId(String objectId)
	{
		m_objectId = objectId;
	}

	public void setAllVersions(boolean allVersions)
	{
		m_allVersions = allVersions;
	}

	public void doWork() throws BuildException
	{
		if (m_objectId == null)
		{
			throw new BuildException("Object id should be specified");
		}

		processObjectsBatch(m_objectId);
	}

	protected void processSingleObjectFromBatch(int iteration, IDfId objectId)
			throws BuildException
	{
		IDfPersistentObject obj = getDfObject(objectId);

		try
		{
			if (!m_allVersions || !(obj instanceof IDfSysObject))
			{
				obj.destroy();
				return;
			}

			IDfSysObject sysObj = (IDfSysObject)obj;
			sysObj.destroyAllVersions();
		}
		catch (DfException e)
		{
			if (failOnError())
			{
				throw new BuildException("Failed to destroy object r_object_id=" + objectId.getId(), e);
			}

			log("Failed to destroy object r_object_id=" + objectId.getId(), e, Project.MSG_WARN);
		}
	}

}
