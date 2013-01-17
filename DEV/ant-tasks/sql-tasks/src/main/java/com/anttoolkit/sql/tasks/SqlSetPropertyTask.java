package com.anttoolkit.sql.tasks;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;
import com.anttoolkit.sql.common.*;

public class SqlSetPropertyTask
		extends GenericTask
{
	private String m_sqlCommand = null;
	private String m_propName = null;
	private String m_format = "dd.MM.yyyy hh:mm:ss";

	public void addText(String text)
	{
		m_sqlCommand = getProject().replaceProperties(text);
	}

	public void setPropertyName(String name)
	{
		m_propName = name;
	}

	public void setFormat(String format)
	{
		m_format = format;
	}

	public void doWork() throws BuildException
	{
		String value = SqlHelper.getStringParamFromFirstString(SqlSessionManager.getSession(), m_sqlCommand, 1, m_format);
		this.getProject().setProperty(m_propName, value);
	}

	private void validate()
	{
		if (m_sqlCommand == null || m_sqlCommand.trim().length() == 0)
		{
			throw new BuildException("SQL command is not specified");
		}

		if (m_propName == null)
		{
			throw new BuildException("Property name is not specified");
		}
	}
}
