package com.anttoolkit.general.tasks;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;

public class ReplaceTextSubstringTask
		extends GenericTask
{
	private String m_text;
	private String m_substring;
	private String m_replaceWith;
	private String m_property;

	public void setString(String value)
	{
		m_text = value;
	}

	public void addText(String value)
	{
		m_text = getProject().replaceProperties(value);
	}

	public void setSubstring(String substring)
	{
		m_substring = substring;
	}

	public void setReplaceWith(String value)
	{
		m_replaceWith = value;
	}

	public void setProperty(String property)
	{
		m_property = property;
	}

	public void doWork() throws BuildException
	{
		validate();

		String value = m_text.replace(m_substring, m_replaceWith);

		this.getProject().setProperty(m_property, value);
	}

	private void validate()
	{
		if (m_text == null)
		{
			throw new BuildException("Text value is not specified");
		}

		if (m_substring == null)
		{
			throw new BuildException("Substring is not specified");
		}

		if (m_replaceWith == null)
		{
			throw new BuildException("Text to replace with is not specified");
		}

		if (m_property == null)
		{
			throw new BuildException("Property name is not specified");
		}
	}
}
