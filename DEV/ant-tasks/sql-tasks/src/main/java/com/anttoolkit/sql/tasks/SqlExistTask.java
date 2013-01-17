package com.anttoolkit.sql.tasks;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;
import com.anttoolkit.sql.common.*;

public class SqlExistTask
		extends GenericTask
{
	private String m_query = null;
	private String m_property = null;

	public void setQuery(String query)
	{
		m_query = query;
	}

	public void addText(String text)
	{
		m_query = getProject().replaceProperties(text);
	}

	public void setProperty(String property)
	{
		m_property = property;
	}

	public void doWork() throws BuildException
	{
		if (m_query == null)
		{
			throw new BuildException("SQL query should be specified");
		}

		if (m_property == null)
		{
			throw new BuildException("Property should be specified");
		}

		boolean exist = SqlHelper.exist(SqlSessionManager.getSession(), m_query);
		this.getProject().setProperty(m_property, Boolean.toString(exist));
	}
}
