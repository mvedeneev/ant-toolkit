package com.anttoolkit.general.tasks.concurrent;

import java.util.concurrent.*;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;
import com.anttoolkit.general.tasks.concurrent.util.*;

public class WaitCyclicBarrierTask
		extends GenericTask
{
	private String m_barrierName = null;

	public void setName(String name)
	{
		m_barrierName = name;
	}

	public void doWork() throws BuildException
	{
		if (m_barrierName == null)
		{
			throw new BuildException("Cyclic barrier name should be specified");
		}

		Thread thread = Thread.currentThread();
		if (!(thread instanceof TasksThread))
		{
			throw new BuildException("WaitCyclicBarrierTask could only be performed inside TasksThread");
		}

		TasksThread tasksThread = (TasksThread)thread;

		CyclicBarrier barrier = ThreadManager.getCyclicBarrier(m_barrierName);
		if (barrier == null)
		{
			throw new BuildException("There are no cyclic barrier with name: " + m_barrierName);
		}

		try
		{
			tasksThread.log("Waiting for barrier '" + m_barrierName + "'");
			barrier.await();
			tasksThread.log("Waiting for barrier '" + m_barrierName + "' completed");
		}
		catch (InterruptedException e)
		{
			throw new BuildException("Waiting for cyclic barrier '" + m_barrierName + "' was interrupted", e);
		}
		catch (BrokenBarrierException ex)
		{
			throw new BuildException("Cyclic barrier '" + m_barrierName + "' is broken", ex);
		}
	}
}
