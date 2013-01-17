package com.anttoolkit.general.tasks.arrays;

import com.anttoolkit.general.tasks.arrays.util.ArrayManager;
import org.apache.tools.ant.*;

import java.util.*;

import com.anttoolkit.common.*;

public class ArrayLoopTask
		extends GenericTask
		implements TaskContainer
{
	public void addTask(Task task)
	{
		m_tasks.add(task);
	}

	public void setArrayName(String name)
	{
		m_arrayName = name;	
	}

	public void setElementPropertyName(String name)
	{
		m_elementPropertyName = name;
	}

	public void setIndexPropertyName(String name)
	{
		m_indexPropertyName = name;
	}

	public void doWork()
			throws BuildException
	{
		int tasksCount = m_tasks.size();
		if (tasksCount == 0)
		{
			return;
		}

		List data = ArrayManager.getArrayData(m_arrayName);
		int count = data.size();

		for (int i = 0; i < count; i++)
		{
			if (m_indexPropertyName != null)
			{
				this.getProject().setProperty(m_indexPropertyName, Integer.toString(i));
			}

			if (m_elementPropertyName != null)
			{
				this.getProject().setProperty(m_elementPropertyName, (String)data.get(i));
			}

			int taskCount = m_tasks.size();
			for (int j = 0; j < taskCount; j++)
			{
				((Task)m_tasks.get(j)).perform();
			}
		}
	}


	private Vector m_tasks = new Vector();

	private String m_arrayName;
	private String m_elementPropertyName;
	private String m_indexPropertyName;
}
