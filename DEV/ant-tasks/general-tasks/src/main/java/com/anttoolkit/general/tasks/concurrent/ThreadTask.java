package com.anttoolkit.general.tasks.concurrent;

import java.util.*;

import org.apache.tools.ant.*;

import com.anttoolkit.general.tasks.concurrent.util.*;
import com.anttoolkit.common.*;

public class ThreadTask
		extends GenericTask
		implements TaskContainer
{
	private List<Task> m_tasks = new LinkedList<Task>();

	private String m_threadName = null;
	private String m_threadGroup = null;
	private String m_cyclicBarrier = null;
	private String m_logFile = null;

	public void setName(String name)
	{
		m_threadName = name;
	}

	public void setGroup(String group)
	{
		m_threadGroup = group;
	}

	public void setCyclicBarrier(String barrier)
	{
		m_cyclicBarrier = barrier;
	}

	public void setLogFile(String file)
	{
		m_logFile = file;
	}

	public void doWork() throws BuildException
	{
		ThreadManager.startThread(m_threadName, m_threadGroup, m_cyclicBarrier, m_logFile, m_tasks);
	}

	public void addTask(Task task)
	{
		try
		{
			Task newTask = null;

			if (task instanceof UnknownElement)
			{
				newTask = ((UnknownElement)task).copy(getProject());
				newTask.setProject(getProject());
			}
			else
			{
				newTask = (Task)task.clone();
			}

			m_tasks.add(newTask);
		}
		catch (Throwable e)
		{
			throw new BuildException("Failed to clone task", e);
		}
	}
}
