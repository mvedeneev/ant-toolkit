package com.anttoolkit.documentum.tasks.alias;

import org.apache.tools.ant.*;

import com.anttoolkit.documentum.common.*;

import com.documentum.fc.common.*;

import java.util.*;

public class GetAliasValueTask
		extends GenericDocbaseTask
{
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

		public void setPropertyName(String name)
		{
			m_propertyName = name;
		}

		public String getPropertyName()
		{
			return m_propertyName;
		}

		private String m_name = null;
		private String m_propertyName = null;
	}

	private String m_aliasSet = null;
	private List<Alias> m_aliases = new LinkedList<Alias>();

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

		Map<String, String> values = null;

		try
		{
			values = AliasHelper.getAliasSetNameValuePairs(getSession(), m_aliasSet);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get alias set '" + m_aliasSet + "' values");
		}
		catch (DfEndOfCollectionException e)
		{
			throw new BuildException("Alias set '" + m_aliasSet + "' doesn't exist");
		}

		if (values == null || values.size() != count)
		{
			throw new BuildException("Not all aliases exist in requested alias set '" + m_aliasSet + "'");
		}

		for (Alias alias : m_aliases)
		{
			if (!values.containsKey(alias.getName()))
			{
				throw new BuildException("Alias '" + alias.getName() + "' doesn't exist in requested alias set '" + m_aliasSet + "'");
			}

			this.getProject().setProperty(alias.getName(), values.get(alias.getName()));
		}
	}
}
