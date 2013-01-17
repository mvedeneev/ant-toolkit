package com.anttoolkit.documentum.tasks.alias;

import com.anttoolkit.documentum.common.*;

import java.util.*;
import java.text.*;

import org.apache.tools.ant.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

public class SetAliasValueTask
		extends GenericDocbaseTask
{
	private static final String DQL_GET_ALIAS_SET_ID_BY_NAME = "select r_object_id from dm_alias_set " +
			"where object_name=''{0}''";

	public class Alias
	{
		public void setName(String name)
		{
			m_name = name;
		}

		public String getName()
		{
			return m_name;
		}

		public void setValue(String value)
		{
			m_value = value;
		}

		public String getValue()
		{
			return m_value;
		}

		private String m_name = null;
		private String m_value = null;
	}

	public Alias createAlias()
	{
		Alias alias = new Alias();
		m_aliases.add(alias);
		return alias;
	}

	public void setAliasSet(String aliasSet)
	{
		m_aliasSet = aliasSet;
	}

	public void doWork()
			throws BuildException
	{
		if (m_aliasSet == null)
		{
			throw new BuildException("Alias set name should be specified");	
		}

		int count = m_aliases.size();
		if (count == 0)
		{
			return;
		}

		IDfAliasSet aliasSet = getAliasSetByName(m_aliasSet);
		for (int i = 0; i < count; i++)
		{
			Alias alias = (Alias)m_aliases.get(i);
			setAliasValue(aliasSet, alias.getName(), alias.getValue());
		}

		try
		{
			aliasSet.save();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to save alias set \"" + m_aliasSet +
					"\" after it was modified.\r\n" + e.toString());
		}
	}

	private IDfAliasSet getAliasSetByName(String name)
			throws BuildException
	{
		try
		{
			String dqlQuery = MessageFormat.format(DQL_GET_ALIAS_SET_ID_BY_NAME, new String[] {name});
			return getAliasSetById(DqlHelper.getStringParamFromFirstString(this.getSession(), dqlQuery));
		}
		catch (Exception e)
		{
			throw new BuildException("Failed to get alias set by name=" + name + "\r\n" + e.toString());
		}
	}

	public IDfAliasSet getAliasSetById(String objectId)
			throws BuildException
	{
		try
		{
			return (IDfAliasSet)this.getSession().getDfSession().getObject(new DfId(objectId));
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get alias set by id=" + objectId + "\r\n" + e.toString());
		}
	}

	public void setAliasValue(IDfAliasSet aliasSet, String aliasName, String aliasValue)
			throws BuildException
	{
		try
		{
			int index = aliasSet.findAliasIndex(aliasName);

			if (index == -1)
			{
				aliasSet.appendAlias(aliasName, aliasValue, IDfAliasSet.CATETORY_UNKNOWN, 0, "");
			}
			else
			{
				aliasSet.setAliasValue(index, aliasValue);
			}

			aliasSet.save();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to set alias value=" + aliasValue + " to alias=" + aliasName +
					" in alias set=" + m_aliasSet + "\r\n" + e.toString());
		}
	}


	private String m_aliasSet = null;
	private List m_aliases = new LinkedList();
}
