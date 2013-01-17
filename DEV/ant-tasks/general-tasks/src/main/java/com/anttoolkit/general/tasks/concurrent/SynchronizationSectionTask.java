package com.anttoolkit.general.tasks.concurrent;

import java.util.*;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;
import com.anttoolkit.general.tasks.concurrent.util.*;

public class SynchronizationSectionTask
		extends GenericTask
		implements TaskContainer
{
	private List<Task> m_tasks = new LinkedList<Task>();

	private String m_sectionName;

	public void setSectionName(String name)
	{
		m_sectionName = name;
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

	public void doWork() throws BuildException
	{
		if (m_sectionName == null)
		{
			throw new BuildException("Synchronization section name should be specified");
		}

		Thread thread = Thread.currentThread();
		if (!(thread instanceof TasksThread))
		{
			throw new BuildException("SynchronizationSectionTask could only be performed inside TasksThread");
		}

		TasksThread tasksThread = (TasksThread)thread;

		Object synchSectionObject = ThreadManager.getSynchronizationSectionObject(m_sectionName);

		tasksThread.log("Trying to enter synchronization section '" + m_sectionName + "'");

		synchronized (synchSectionObject)
		{
			tasksThread.log("Entered synchronization section '" + m_sectionName + "'");

			for (Task task : m_tasks)
			{
				task.perform();
			}

			tasksThread.log("Left synchronization section '" + m_sectionName + "'");
		}
	}
}
