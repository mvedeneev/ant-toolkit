package com.anttoolkit.documentum.tasks.usergroup;

import com.anttoolkit.documentum.common.*;

import org.apache.tools.ant.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

public abstract class GenericGroupTask
		extends GenericDocbaseTask

{
	public void setRootGroup(String group)
	{
		m_rootGroup = group.toLowerCase();
	}

	public String getRootGroupName()
	{
		return m_rootGroup;
	}

	public void setUser(String user)
	{
		m_user = user.toLowerCase();
	}

	public void setGroup(String group)
	{
		m_group = group.toLowerCase();
	}

	public void doWork()
			throws BuildException
	{
		if (m_rootGroup == null)
		{
			throw new BuildException("rootGroup doesn't specified");
		}

		if (m_user == null && m_group == null)
		{
			throw new BuildException("user ot group should be specified");
		}
	}

	protected String[] getUsers()
	{
		if (m_user == null)
		{
			return new String[]{};
		}

		String[] users = m_user.split(",");

		int count = users.length;
		for (int i = 0; i < count; i++)
		{
			users[i] = users[i].trim();
		}

		return users;
	}

	protected String[] getGroups()
	{
		if (m_group == null)
		{
			return new String[]{};
		}

		String[] groups = m_group.split(",");

		int count = groups.length;
		for (int i = 0; i < count; i++)
		{
			groups[i] = groups[i].trim();
		}

		return groups;
	}

	protected IDfGroup getRootGroup()
			throws BuildException
	{
		try
		{
			return this.getSession().getDfSession().getGroup(m_rootGroup);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get root group=" + m_rootGroup + " from docbase" +
					"\r\n" + e.toString());
		}
	}

	protected void saveRootGroup(IDfGroup group)
			throws BuildException
	{
		try
		{
			group.save();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to save root group=" + m_rootGroup +
					"\r\n" + e.toString());
		}
	}

	private String m_rootGroup = null;
	private String m_user = null;
	private String m_group = null;
}
