package com.anttoolkit.documentum.tasks.permissions;

import java.util.*;

import org.apache.tools.ant.*;

import com.documentum.fc.common.*;
import com.documentum.fc.client.*;

import com.anttoolkit.documentum.common.*;

public class AclTask
		extends GenericDocbaseTask
{
	private String m_objectId = null; 
	private String m_aclName = null;
	private String m_aclDomain = null;
	private int m_aclClass = 3; // public ACL by default
	private String m_description = null;

	private List<PermitAction> m_actions = new LinkedList<PermitAction>();

	public void setObjectId(String objectId)
	{
		m_objectId = objectId;
	}

	public void setAclName(String name)
	{
		m_aclName = name;
	}

	public void setAclDomain(String domain)
	{
		m_aclDomain = domain;
	}

	public void setAclClass(int aclClass)
	{
		m_aclClass = aclClass;
	}

	public void setDescription(String description)
	{
		m_description = description;
	}

	public void addConfiguredGrant(PermitAction action)
	{
		action.markActionType(true);
		m_actions.add(action);
	}

	public void addConfiguredRevoke(PermitAction action)
	{
		action.markActionType(false);
		m_actions.add(action);
	}

	public void doWork()
			throws BuildException
	{
		if (m_objectId == null && m_aclName == null)
		{
			throw new BuildException("Acl name or objectId should be specified");
		}

		IDfACL acl = m_objectId != null ? (IDfACL)getDfObject(m_objectId) : (IDfACL)createDfObject("dm_acl");

		try
		{
			if (m_aclName != null)
			{
				acl.setObjectName(m_aclName);
			}

			if (m_aclDomain != null)
			{
				acl.setDomain(m_aclDomain);
			}

			if (m_description != null)
			{
				acl.setDescription(m_description);
			}

			if (acl.getACLClass() != m_aclClass)
			{
				acl.setACLClass(m_aclClass);
			}

			for (PermitAction action : m_actions)
			{
				action.execute(acl);
			}

			if (acl.isNew() || acl.isDirty())
			{
				acl.save();
			}
		}
		catch (DfException e)
		{
			if (m_objectId != null)
			{
				throw new BuildException("Failed to update ACL", e);
			}
			else
			{
				throw new BuildException("Failed to create ACL", e);
			}
		}
	}
}
