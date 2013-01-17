package com.anttoolkit.general.tasks;

import java.util.*;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;

public class ForLoopTask
		extends GenericTask
		implements TaskContainer
{
	private long m_from = -1;
	private long m_to = -1;
	private long m_increment = 1;
	private String m_counterProperty = null;

	private List<Task> m_tasks = new LinkedList<Task>();

	public void addTask(Task task)
	{
		m_tasks.add(task);
	}

	public void setFrom(long from)
	{
		m_from = from;
	}

	public void setTo(long to)
	{
		m_to = to;
	}

	public void setIncrement(long increment)
	{
		m_increment = increment;
	}

	public void setCounterProperty(String property)
	{
		m_counterProperty = property;
	}

	public void doWork()
			throws BuildException
	{
		if (m_increment == 0)
		{
			throw new BuildException("Increment couldn't be 0");
		}

		if ((m_increment > 0 && m_from > m_to) ||
			(m_increment < 0 && m_from < m_to))
		{
			throw new BuildException("Invalid cycle range specified");
		}

		for (long i = m_from; m_increment > 0 ? i <= m_to : i >= m_to ; i = i + m_increment)
		{
			if (m_counterProperty != null)
			{
				this.getProject().setProperty(m_counterProperty, Long.toString(i));
			}

			for (Task task : m_tasks)
			{
				task.perform();
			}
		}
	}
}
