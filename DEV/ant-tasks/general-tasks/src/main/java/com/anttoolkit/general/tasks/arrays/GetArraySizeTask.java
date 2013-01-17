package com.anttoolkit.general.tasks.arrays;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;
import com.anttoolkit.general.tasks.arrays.util.*;

public class GetArraySizeTask
		extends GenericTask
{
	private String m_arrayName = null;
	private String m_property = null;

	public void setArrayName(String name)
	{
		m_arrayName = name;
	}

	public void setProperty(String property)
	{
		m_property = property;
	}

	public void doWork() throws BuildException
	{
		if (m_arrayName == null)
		{
			throw new BuildException("Array name should be specified");
		}

		if (m_property == null)
		{
			throw new BuildException("Property name should be specified");
		}

		int size = ArrayManager.getArraySize(m_arrayName);

		this.getProject().setProperty(m_property, Integer.toString(size));
	}
}