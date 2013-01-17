package com.anttoolkit.documentum.tasks.permissions;

import com.anttoolkit.documentum.common.*;

import java.util.*;

import org.apache.tools.ant.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

public class SetPermitTask
		extends GenericDocbaseTask
{
	public void addConfiguredUser(UserPermit permit)
	{
		if (!m_userPermits.containsKey(permit.getName()))
		{
			m_userPermits.put(permit.getName(), permit);
			return;
		}

		UserPermit _permit = (UserPermit)m_userPermits.get(permit.getName());
		if (_permit.getPermit() < permit.getPermit())
		{
			_permit.setIntPermit(permit.getPermit());
		}

		_permit.appendXpermits(permit.getXpermit());
	}

	public void setObjectId(String objectId)
	{
		m_objectId = objectId;
	}

	public void setResetACL(boolean resetFlag)
	{
		m_resetFlag = resetFlag;
	}

	public void setOwner(String owner)
	{
		m_owner = owner;
	}

	public void doWork()
			throws BuildException
	{
		if (m_objectId == null)
		{
			throw new BuildException("objectId attribute is mandatory");
		}

		if (m_objectId.length() == 16)
		{
			setUserPermits((IDfSysObject)this.getDfObject(m_objectId));
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
					setUserPermits((IDfSysObject)this.getDfObject(objectId.getId()));
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

	private void setUserPermits(IDfSysObject sysObj)
			throws BuildException
	{
		if (m_resetFlag)
		{
			AclHelper.resetObjectACL(sysObj);
		}

		if (m_owner != null)
		{
			try
			{
				sysObj.setOwnerName(m_owner);
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to set object owner\r\n" + e.toString());
			}
		}

		Iterator names = m_userPermits.keySet().iterator();
		while (names.hasNext())
		{
			String userName = (String)names.next();
			UserPermit userPermit = (UserPermit)m_userPermits.get(userName);
			AclHelper.setPermitForUser(sysObj, userName, userPermit.getPermit(), userPermit.getXpermit());
		}

		this.saveDfObject(sysObj);
	}


	private String m_objectId = null;
	private boolean m_resetFlag = false;
	private String m_owner = null;
	private Map m_userPermits = new Hashtable();
}
