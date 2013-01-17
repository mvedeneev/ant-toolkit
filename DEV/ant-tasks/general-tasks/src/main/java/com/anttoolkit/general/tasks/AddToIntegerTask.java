package com.anttoolkit.general.tasks;

import com.anttoolkit.common.*;

import org.apache.tools.ant.*;

public class AddToIntegerTask
	extends GenericTask
{
	public void setPropertyName(String name)
	{
		m_propertyName = name;
	}

	public void setArgument(String argument)
	{
		m_argument = argument;	
	}

	public void doWork()
			throws BuildException
	{
		verify();

		String propertyValue = this.getProject().getProperty(m_propertyName);
		try
		{
			int value = Integer.parseInt(propertyValue);
			this.getProject().setProperty(m_propertyName, Integer.toString(value + m_numberToAdd));
		}
		catch (NumberFormatException e)
		{
			throw new BuildException("Property " + m_propertyName + " has incorrect value: " +
					propertyValue + " (it should be integer)");	
		}
	}

	private void verify()
	{
		if (m_propertyName == null || m_propertyName.trim().length() == 0)
		{
			throw new BuildException("Property name doesn't specified");
		}

		if (m_argument == null || m_argument.trim().length() == 0)
		{
			throw new BuildException("Argument doesn't specified");	
		}

		try
		{
			m_numberToAdd = Integer.parseInt(m_argument);
		}
		catch (NumberFormatException e)
		{
			throw new BuildException("Incorrect argument value: " + m_argument);	
		}
	}


	private String m_propertyName;
	private String m_argument;
	private int m_numberToAdd;
}
