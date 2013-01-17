package com.anttoolkit.general.tasks.arrays;

import com.anttoolkit.common.*;

import com.anttoolkit.general.tasks.arrays.util.ArrayManager;
import org.apache.tools.ant.*;

import java.util.*;

public class InitArrayTask
		extends GenericTask
{
	private String m_arrayName;
	private String m_array;
	private String m_file;
	private String m_separatorPattern = ",";
	private boolean m_trimValues = true;
	private boolean m_removeEmptyValues = false;
	private String m_arrayLengthProperty;
	private int m_initialSize = 0;

	public void setArrayName(String name)
	{
		m_arrayName = name;	
	}

	public void setArray(String array)
	{
		m_array = array;	
	}

	public void setFile(String file)
	{
		m_file = file;
	}

	public void setInitialSize(String size)
	{
		int index = size.lastIndexOf('.');

		try
		{
			m_initialSize = index == -1 ? Integer.parseInt(size) : Integer.parseInt(size.substring(0, index));
		}
		catch (NumberFormatException e)
		{
			m_initialSize = 10;
		}

		if (m_initialSize <= 0)
		{
			m_initialSize = 10;
		}
	}

	public void setSeparatorPattern(String separator)
	{
		m_separatorPattern = separator;
	}

	public void setTrimValues(boolean trim)
	{
		m_trimValues = trim;	
	}

	public void setRemoveEmptyValues(boolean remove)
	{
		m_removeEmptyValues = remove;	
	}

	public void setArrayLengthProperty(String name)
	{
		m_arrayLengthProperty = name;
	}

	public void doWork()
			throws BuildException
	{
		verify();

		List data = getArrayData();

		ArrayManager.initArray(m_arrayName, data);

		if (m_arrayLengthProperty != null)
		{
			this.getProject().setProperty(m_arrayLengthProperty, Integer.toString(data.size()));
		}
	}

	private void verify()
	{
		if (m_arrayName == null || m_arrayName.trim().length() == 0)
		{
			throw new BuildException("Array name doesn't specified");
		}

		if (m_separatorPattern == null || m_separatorPattern.trim().length() == 0)
		{
			throw new BuildException("Array elements separator doesn't specified");
		}
	}

	private List getArrayData()
	{
		List data;

		if (m_array != null)
		{
			data = Arrays.asList(m_array.split(m_separatorPattern));
		}
		else if (m_file != null)
		{
			data = Arrays.asList(this.loadFileContent(m_file).split(m_separatorPattern));
		}
		else
		{
			return new ArrayList(m_initialSize);
		}

		LinkedList notEmptyElements = new LinkedList();

		int count = data.size();
		for (int i = 0; i < count; i++)
		{
			String val = (String)data.get(i);

			if (m_trimValues)
			{
				val = val.trim();
				data.set(i, val);
			}

			if (!m_removeEmptyValues || val.length() != 0)
			{
				//noinspection unchecked
				notEmptyElements.add(val);
			}
		}

		//noinspection unchecked
		return notEmptyElements;
	}
}
