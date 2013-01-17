package com.anttoolkit.general.tasks.concurrent.util;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;

public class TasksThread extends Thread implements Comparable
{
	private String m_barrierName = null;
	private CyclicBarrier m_barrier = null;
	private String m_logFile = null;
	private List<Task> m_tasks = null;
	private Throwable m_error = null;

	TasksThread(String threadName, String barrier, String logFile, List<Task> tasks)
	{
		super(threadName);

		m_barrierName = barrier;
		m_barrier = barrier == null ? null : ThreadManager.getCyclicBarrier(barrier);
		m_logFile = logFile;
		m_tasks = tasks;
	}

	public void run()
	{
		try
		{
			if (m_barrier != null)
			{
				log("Waiting for barrier '" + m_barrierName + "'");
				m_barrier.await();
				log("Waiting for barrier '" + m_barrierName + "' completed");
			}

			long startTime = System.currentTimeMillis();

			log("Execution started");

			for (Task task : m_tasks)
			{
				task.perform();
			}

			log("Execution completed, duration: " + getDurationInfo(startTime));
		}
		catch (Throwable e)
		{
			m_error = e;
			log(e);
		}
	}

	public boolean isFailed()
	{
		return m_error != null;
	}

	public Throwable getError()
	{
		return m_error;
	}

	public String getLogFile()
	{
		return m_logFile;
	}

	public String toString()
	{
		return getThreadDisplayName();
	}

	public void log(String message)
	{
		logToFile(message);

		if (m_tasks == null || m_tasks.isEmpty())
		{
			System.out.println("[" + getThreadDisplayName() + "] " + message);
		}
		else
		{
			m_tasks.get(0).getProject().log("[" + getThreadDisplayName() + "] " + message);
		}
	}

	private void log(Throwable e)
	{
		log("[EXCEPTION]: " + ExceptionHelper.stackTraceToString(e));
	}

	private void logToFile(String message)
	{
		if (m_logFile == null)
		{
			return;
		}

		try
		{
		  PrintWriter writer = new PrintWriter(new FileOutputStream(m_logFile, true));
		  writer.println(message);
		  writer.close();
		}
		catch (Throwable e) {}
	}

	private String getThreadDisplayName()
	{
		if (getName() != null && getName().trim().length() != 0)
		{
			return getName();
		}

		return Long.toString(getId());
	}

	private String getDurationInfo(long startTime)
	{
		long milliseconds = System.currentTimeMillis() - startTime;
		if (milliseconds < 1000)
		{
			return milliseconds + " milliseconds";
		}

		long seconds = milliseconds / 1000;
		return seconds + " seconds";
	}

	public int compareTo(Object obj)
	{
		return this.toString().compareTo(obj.toString());
	}
}
