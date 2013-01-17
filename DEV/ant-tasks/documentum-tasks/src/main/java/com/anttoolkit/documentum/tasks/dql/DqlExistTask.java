package com.anttoolkit.documentum.tasks.dql;

import org.apache.tools.ant.*;

import com.documentum.fc.common.*;

import com.anttoolkit.documentum.common.*;

public class DqlExistTask
		extends GenericDocbaseTask
{
	private String m_dqlStatement = null;
	private String m_property = null;

	public void setQuery(String query)
	{
		m_dqlStatement = query;
	}

	public void addText(String text)
	{
		m_dqlStatement = getProject().replaceProperties(text);
	}

	public void setProperty(String property)
	{
		m_property = property;
	}

	public void doWork()
			throws BuildException
	{
		if (m_dqlStatement == null)
		{
			throw new BuildException("DQL statement should be specified");
		}

		if (m_property == null)
		{
			throw new BuildException("Property should be specified");
		}

		try
		{
			boolean exist = DqlHelper.exist(this.getSession(), m_dqlStatement);
			this.getProject().setProperty(m_property, Boolean.toString(exist));
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to execute DQL query\r\n" + e.toString());
		}
	}
}
