package com.anttoolkit.general.tasks.arrays;

import com.anttoolkit.general.tasks.GenerateRandomIntegerTask;
import com.anttoolkit.general.tasks.arrays.util.ArrayManager;
import org.apache.tools.ant.*;

import com.anttoolkit.common.*;

public class GetRandomArrayElementTask
		extends GenericTask
{
	private String m_arrayName = null;
	private String m_valueProperty = null;
	private String m_indexProperty = null;

	public void setArrayName(String name)
	{
		m_arrayName = name;
	}

	public void setValueProperty(String name)
	{
		m_valueProperty = name;
	}

	public void setIndexProperty(String name)
	{
		m_indexProperty = name;
	}

	public void doWork() throws BuildException
	{
		if (m_arrayName == null)
		{
			throw new BuildException("Array name should be specified");
		}

		if (m_valueProperty == null && m_indexProperty == null)
		{
			throw new BuildException("Value or index property should be specified");
		}

		int size = ArrayManager.getArraySize(m_arrayName);
		int index = GenerateRandomIntegerTask.getRandomInt(0, size - 1);
		String value = ArrayManager.getArrayElement(m_arrayName, index);

		if (m_valueProperty != null)
		{
			getProject().setProperty(m_valueProperty, value);
		}

		if (m_indexProperty != null)
		{
			getProject().setProperty(m_indexProperty, Integer.toString(index));
		}
	}
}
