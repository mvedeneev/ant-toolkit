package com.anttoolkit.general.tasks.arrays;

import com.anttoolkit.general.tasks.arrays.util.ArrayManager;

import com.anttoolkit.common.*;

import org.apache.tools.ant.*;

public class GetArrayElementTask
		extends GenericTask
{
	public void setArrayName(String name)
	{
		m_arrayName = name;
	}

	public void setIndex(int index)
	{
		m_index = index;
	}

	public void setPropertyName(String name)
	{
		m_propertyName = name;
	}

	public void doWork()
			throws BuildException
	{
		verify();

		this.getProject().setProperty(m_propertyName, ArrayManager.getArrayElement(m_arrayName, m_index));
	}

	private void verify()
	{
		if (m_arrayName == null || m_arrayName.trim().length() == 0)
		{
			throw new BuildException("Array name doesn't specified");	
		}

		if (m_index == -1)
		{
			throw new BuildException("Array index doesn't specified");	
		}

		if (m_propertyName == null || m_propertyName.trim().length() == 0)
		{
			throw new BuildException("Property name doesn't specified");	
		}
	}

	private String m_arrayName;
	private int m_index = -1;
	private String m_propertyName;
}
