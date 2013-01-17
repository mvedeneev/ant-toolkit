package com.anttoolkit.documentum.tasks.permissions;

import com.anttoolkit.documentum.common.*;

import org.apache.tools.ant.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

public class SetOwnerTask
		extends GenericDocbaseTask
{
	public void setOwner(String owner)
	{
		m_owner = owner;
	}

	public void setObjectId(String objectId)
	{
		m_objectId = objectId;
	}

	public void doWork()
			throws BuildException
	{
		if (m_owner == null || m_objectId == null)
		{
			throw new BuildException("owner and objectId are mandatory attributes");
		}

		if (m_objectId.length() == 16)
		{
			setObjectOwner(m_objectId);
			return;
		}

		IDfCollection coll = null;
		try
		{
			try
			{
				coll = DqlHelper.executeReadQuery(this.getSession(), m_objectId);
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to execute query \"" + m_objectId + "\" to get id's collection\r\n" + e.toString());
			}

			try
			{
				while (coll.next())
				{
					IDfId objectId = coll.getValueAt(0).asId();
					setObjectOwner(objectId.getId());
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

	private void setObjectOwner(String objectId)
			throws BuildException
	{
		IDfSysObject obj = (IDfSysObject)this.getDfObject(objectId);

		try
		{
			obj.setOwnerName(m_owner);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to set owner name for object with r_object_id=" +
					objectId + "\r\n" + e.toString());
		}

		this.saveDfObject(obj);
	}

	private String m_owner = null;
	private String m_objectId = null;
}
