package com.anttoolkit.documentum.tasks.dql;

import org.apache.tools.ant.*;

import com.anttoolkit.documentum.common.*;

public class DqlLoopPropertyTask
		extends GenericDocbaseTask
{
	private String m_propertyName = null;
	private String m_columnName = null;
	private String m_format = null;
	private String m_nullValue = null;
	private String m_replaceSubstring = null;
	private String m_replaceWith = null;

	public void setPropertyName(String name)
	{
		m_propertyName = name;
	}

	public void setColumnName(String name)
	{
		m_columnName= name;
	}

	public void setFormat(String format)
	{
		m_format= format;
	}

	public void setNullValue(String value)
	{
		m_nullValue = value;
	}

	public void setReplaceSubstring(String value)
	{
		m_replaceSubstring = value;
	}

	public void setReplaceWith(String value)
	{
		m_replaceWith = value;
	}

	public void doWork()
			throws BuildException
	{
		String value = DqlLoopTask.getCurrentRowColumn(m_columnName, m_format);
		value = value == null ? m_nullValue : value;

		if (value != null && m_replaceSubstring != null && m_replaceWith != null)
		{
			value = value.replace(m_replaceSubstring, m_replaceWith);
		}

		this.getProject().setProperty(m_propertyName, value);
	}
}
