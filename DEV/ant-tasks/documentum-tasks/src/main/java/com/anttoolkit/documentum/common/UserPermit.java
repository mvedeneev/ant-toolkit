package com.anttoolkit.documentum.common;

import com.documentum.fc.client.*;

import org.apache.tools.ant.*;

import java.util.*;

public class UserPermit
{
	public void setName(String name)
	{
		m_name = name;
	}

	public String getName()
	{
		return m_name;
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

	public void setIntPermit(int permit)
	{
		if (permit != IDfACL.DF_PERMIT_NONE &&
			permit != IDfACL.DF_PERMIT_BROWSE &&
			permit != IDfACL.DF_PERMIT_READ &&
			permit != IDfACL.DF_PERMIT_RELATE &&
			permit != IDfACL.DF_PERMIT_VERSION &&
			permit != IDfACL.DF_PERMIT_WRITE &&
			permit != IDfACL.DF_PERMIT_DELETE)
		{
			throw new BuildException("Unknown permit " + permit);
		}

		m_permit = permit;
	}

	public int getPermit()
	{
		return m_permit;
	}

	public void setXpermit(String xpermits)
	{
		m_xpermits.clear();

		if (AclHelper.ALL_XPERMIT_SYNONIM.equals(xpermits.toUpperCase()))
		{
			appendXpermits(AclHelper.ALL_XPERMIT);
		}
		else
		{
			appendXpermits(xpermits);
		}
	}

	public String getXpermit()
	{
		if (m_xpermits.size() == 0)
		{
			return null;
		}

		StringBuffer xpermits = new StringBuffer();
		int count = m_xpermits.size();
		for (int i = 0; i < count; i++)
		{
			if (xpermits.length() == 0)
			{
				xpermits.append(m_xpermits.get(i));
				continue;
			}

			xpermits.append(",");
			xpermits.append(m_xpermits.get(i));
		}

		return xpermits.toString();
	}

	public void appendXpermits(String xpermits)
	{
		if (xpermits == null)
		{
			return;
		}

		String[] xpermitsArray = xpermits.toUpperCase().split(",");
		int count = xpermitsArray.length;
		for (int i = 0; i < count; i++)
		{
			appendXpermit(xpermitsArray[i]);
		}
	}

	private void appendXpermit(String xpermit)
	{
		if (!xpermit.equals(IDfACL.DF_XPERMIT_EXECUTE_PROC_STR) &&
			!xpermit.equals(IDfACL.DF_XPERMIT_CHANGE_LOCATION_STR) &&
			!xpermit.equals(IDfACL.DF_XPERMIT_CHANGE_STATE_STR) &&
			!xpermit.equals(IDfACL.DF_XPERMIT_CHANGE_PERMIT_STR) &&
			!xpermit.equals(IDfACL.DF_XPERMIT_CHANGE_OWNER_STR) &&
			!xpermit.equals(IDfACL.DF_XPERMIT_DELETE_OBJECT_STR) &&
			!xpermit.equals(IDfACL.DF_XPERMIT_CHANGE_FOLDER_LINKS_STR))
		{
			throw new BuildException("Unknown  Xpermit " + xpermit);
		}

		if (!m_xpermits.contains(xpermit))
		{
			m_xpermits.add(xpermit);
		}
	}

	private String m_name = null;
	private int m_permit = IDfACL.DF_PERMIT_NONE;
	private List m_xpermits = new Vector();

}
