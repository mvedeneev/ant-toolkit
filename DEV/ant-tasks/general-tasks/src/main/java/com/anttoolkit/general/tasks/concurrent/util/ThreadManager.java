package com.anttoolkit.general.tasks.concurrent.util;

import java.util.*;
import java.util.concurrent.*;

import org.apache.tools.ant.*;

public class ThreadManager
{
	private static volatile Map<String, CyclicBarrier> m_barrierMap = new HashMap<String, CyclicBarrier>();
	private static volatile Map<String, List<TasksThread>> m_threadMap = new HashMap<String, List<TasksThread>>();
	private static volatile Map<String, Object> m_synchSections = new HashMap<String, Object>();

	public static void startThread(String threadName, String threadGroup, String barrierName,
								   String logFile, List<Task> tasks)
	{
		if (tasks == null || tasks.isEmpty())
		{
			return;
		}

		if (threadGroup == null || threadGroup.trim().length() == 0)
		{
			throw new BuildException("Thread group is not specified");
		}

		TasksThread thread = new TasksThread(threadName, barrierName, logFile, tasks);

		synchronized (m_threadMap)
		{
			List<TasksThread> threads = m_threadMap.get(threadGroup);
			if (threads == null)
			{
				threads = new LinkedList<TasksThread>();
				m_threadMap.put(threadGroup, threads);
			}

			threads.add(thread);
		}

		thread.start();
	}

	public static List<TasksThread> waitForCompletion(String threadGroup)
	{
		if (threadGroup == null || threadGroup.trim().length() == 0)
		{
			return null;
		}

		List<TasksThread> threads;

		synchronized (m_threadMap)
		{
			threads = m_threadMap.remove(threadGroup);
		}

		if (threads == null || threads.isEmpty())
		{
			return null;
		}

		for (TasksThread thread : threads)
		{
			try
			{
				thread.join();
			}
			catch (InterruptedException e) {}
		}

		return threads;
	}

	public static void createCyclicBarrier(String barrierName, int parties)
	{
		synchronized (m_barrierMap)
		{
			CyclicBarrier barrier = m_barrierMap.get(barrierName);
			if (barrier != null && barrier.getNumberWaiting() != 0)
			{
				throw new BuildException("Cyclic barrier with name '" + barrierName + "' already exists and " +
					barrier.getNumberWaiting() + " threads waiting waiting for it");
			}

			CyclicBarrier previousBarrier = m_barrierMap.put(barrierName, new CyclicBarrier(parties));

			if (previousBarrier != null)
			{
				previousBarrier.reset();
			}
		}
	}

	public static CyclicBarrier getCyclicBarrier(String barrierName)
	{
		synchronized (m_barrierMap)
		{
			if (m_barrierMap.containsKey(barrierName))
			{
				return m_barrierMap.get(barrierName);
			}

			throw new BuildException("Cyclic barrier with name '" + barrierName + "' doesn't exist");
		}
	}

	public static Object getSynchronizationSectionObject(String sectionName)
	{
		if (sectionName == null)
		{
			return null;
		}

		synchronized (m_synchSections)
		{
			if (m_synchSections.containsKey(sectionName))
			{
				return m_synchSections.get(sectionName);
			}

			m_synchSections.put(sectionName, sectionName);

			return m_synchSections.get(sectionName);
		}
	}
}
