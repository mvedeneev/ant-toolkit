package com.anttoolkit.documentum.tasks;

import com.anttoolkit.documentum.common.*;

import org.apache.tools.ant.*;

import java.util.*;

public class PropertyConditionTask
		extends GenericDocbaseTask
		implements TaskContainer
{
	private String m_property = null;
	private String m_value = null;
	private List<Task> m_tasks = new LinkedList<Task>();

	public void setProperty(String property)
	{
		m_property = property;
	}

	public void setEquals(String value)
	{
		m_value= value;
	}

	public void addTask(Task task)
	{
		if (task == null)
		{
			return;
		}

		m_tasks.add(task);
	}

	public void doWork()
			throws BuildException
	{
		if (m_property == null)
		{
			throw new BuildException("Property name wasn't specified");
		}

		String value = this.getProject().getProperty(m_property);
		if ((m_value == null && value == null) ||
			(m_value != null && !m_value.equals(value)))
		{
			return;
		}

		m_value = m_value == null ? "!=null" : "=" + m_value;
		log("Perform tasks for property condition " + m_property + m_value);

		for (Task task : m_tasks)
		{
			task.perform();
		}
	}
}
