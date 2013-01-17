package com.anttoolkit.general.tasks;

import com.anttoolkit.common.*;
import com.anttoolkit.general.tasks.arrays.util.ArrayManager;

import org.apache.tools.ant.*;

import java.text.*;

public class MessageFormatTask
	extends GenericTask
{
	public void setPattern(String pattern)
	{
		m_pattern = pattern;
	}

	public void setPropertyName(String name)
	{
		m_propertyName = name;
	}

	public void setArray(String array)
	{
		m_array = array;
	}

	public void setArraySeparatorPattern(String separator)
	{
		m_separatorPattern = separator;
	}

	public void setArrayName(String name)
	{
		m_arrayName = name;
	}

	public void doWork()
			throws BuildException
	{
		verify();
		this.getProject().setProperty(m_propertyName, MessageFormat.format(m_pattern, getArrayData()));
	}

	private void verify()
	{
		if (m_pattern == null || m_pattern.trim().length() == 0)
		{
			throw new BuildException("Pattern doesn't specified");	
		}

		if (m_propertyName == null || m_propertyName.trim().length() == 0)
		{
			throw new BuildException("Property name doesn't specified");	
		}

		if (m_array != null && m_arrayName != null)
		{
			throw new BuildException("Only one, array name or array data should be specified");	
		}
	}

	private String[] getArrayData()
	{
		if (m_array != null)
		{
			return m_array.split(m_separatorPattern);
		}

		if (m_arrayName != null)
		{
			return (String[]) ArrayManager.getArrayData(m_arrayName).toArray(new String[]{});
		}

		throw new BuildException("Array name or array data should be specified");
	}

	private String m_pattern;
	private String m_propertyName;
	private String m_array;
	private String m_separatorPattern = ",";
	private String m_arrayName;
}
