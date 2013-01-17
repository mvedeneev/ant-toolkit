package com.anttoolkit.general.tasks.arrays;

import com.anttoolkit.general.tasks.arrays.util.ArrayManager;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;

public class ClearArrayTask
		extends GenericTask
{
	private String m_arrayName = null;

	public void setArrayName(String name)
	{
		m_arrayName = name;
	}

	public void doWork() throws BuildException
	{
		if (m_arrayName == null)
		{
			throw new BuildException("Array name should be specified");
		}

		ArrayManager.clearArray(m_arrayName);
	}
}
