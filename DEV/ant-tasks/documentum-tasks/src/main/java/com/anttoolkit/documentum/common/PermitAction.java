package com.anttoolkit.documentum.common;

import com.documentum.fc.common.*;
import org.apache.tools.ant.*;

import com.documentum.fc.client.*;

public class PermitAction
{
	private boolean m_isGrantAction = true;

	private String m_accessorName = null;
	private int m_permit = -1;
	private String m_xpermit = null;

	public void setAccessorName(String name)
	{
		m_accessorName = name;
	}

	public void setPermit(String permit)
	{
		String _permit = permit.trim().toUpperCase();
		if (_permit.equals("NONE"))
		{
			m_permit = IDfACL.DF_PERMIT_NONE;
		}
		else if (_permit.equals("BROWSE"))
		{
			m_permit = IDfACL.DF_PERMIT_BROWSE;
		}
		else if (_permit.equals("READ"))
		{
			m_permit = IDfACL.DF_PERMIT_READ;
		}
		else if (_permit.equals("RELATE"))
		{
			m_permit = IDfACL.DF_PERMIT_RELATE;
		}
		else if (_permit.equals("VERSION"))
		{
			m_permit = IDfACL.DF_PERMIT_VERSION;
		}
		else if (_permit.equals("WRITE"))
		{
			m_permit = IDfACL.DF_PERMIT_WRITE;
		}
		else if (_permit.equals("DELETE"))
		{
			m_permit = IDfACL.DF_PERMIT_DELETE;
		}
		else
		{
			throw new BuildException("Unknown permit " + permit);
		}
	}

	public void setXpermit(String xpermit)
	{
		m_xpermit = AclHelper.ALL_XPERMIT_SYNONIM.equals(xpermit.toUpperCase()) ? AclHelper.ALL_XPERMIT : xpermit;
	}

	public void markActionType(boolean isGrantAction)
	{
		m_isGrantAction = isGrantAction;	
	}

	public void execute(IDfACL acl)
			throws DfException
	{
		if (m_accessorName == null || m_accessorName.trim().length() == 0)
		{
			throw new BuildException("Accessor name should be specified");
		}

		if (m_isGrantAction)
		{
			if (m_permit == -1)
			{
				throw new BuildException("Accessor permit should be specified");
			}

			try
			{
				acl.grant(m_accessorName, m_permit, m_xpermit);
			}
			catch (DfException e)
			{
				throw new DfException("Failed to grant permit to '" + m_accessorName + "'", e);
			}
		}
		else
		{
			try
			{
				acl.revoke(m_accessorName, m_xpermit);
			}
			catch (DfException e)
			{
				throw new DfException("Failed to revoke '" + m_accessorName + "' permits", e);
			}
		}
	}
}
