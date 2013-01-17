package com.anttoolkit.general.tasks.arrays;

import com.anttoolkit.common.*;

import com.anttoolkit.general.tasks.arrays.util.ArrayManager;
import org.apache.tools.ant.*;

public class AddArrayElementTask
		extends GenericTask
{
	private String m_arrayName;
	private String m_value = null;

	public void setArrayName(String name)
	{
		m_arrayName = name;
	}

	public void setValue(String value)
	{
		m_value = value;
	}

	public void doWork()
			throws BuildException
	{
		ArrayManager.addArrayElement(m_arrayName, m_value);
	}
}
