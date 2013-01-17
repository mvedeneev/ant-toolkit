package com.anttoolkit.general.tasks.concurrent;

import java.util.*;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;
import com.anttoolkit.general.tasks.concurrent.util.*;


public class WaitThreadTask
		extends GenericTask
{
	private String m_threadGroup = null;
	private boolean m_failOnAny = false;

	public void setGroup(String group)
	{
		m_threadGroup = group;
	}

	public void setFailonany(boolean fail)
	{
		m_failOnAny = fail;

	}

	public void doWork() throws BuildException
	{
		if (m_threadGroup == null)
		{
			throw new BuildException("Thread group should be specified");
		}

		List<TasksThread> threads = ThreadManager.waitForCompletion(m_threadGroup);
		Collections.sort(threads);

		int failedCount = 0;

		for (TasksThread thread : threads)
		{
			failedCount += thread.isFailed() ? 1 : 0;
		}

		log("All threads in group '" + m_threadGroup + "' completed. Success=" +
				Integer.toString(threads.size() - failedCount) + ", Failed=" + failedCount + ".");

		if (failedCount != 0)
		{
			log("Failed threads:");

			for (TasksThread thread : threads)
			{
				if (thread.isFailed())
				{
					log(thread.toString());
				}
			}
		}

		if (m_failOnAny && failedCount != 0)
		{
			throw new BuildException(failedCount + " of " +
					Integer.toString(threads.size() - failedCount) + " threads are failed");
		}
	}
}
