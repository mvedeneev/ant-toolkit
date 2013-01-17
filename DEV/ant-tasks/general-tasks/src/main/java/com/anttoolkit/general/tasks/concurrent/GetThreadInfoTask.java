package com.anttoolkit.general.tasks.concurrent;

import com.anttoolkit.general.tasks.concurrent.util.TasksThread;
import org.apache.tools.ant.*;

import com.anttoolkit.common.*;

public class GetThreadInfoTask
		extends GenericTask
{
	private String m_nameProperty = null;
	private String m_idProperty = null;
	private String m_logFileProperty = null;

	public void setNameProperty(String property)
	{
		m_nameProperty = property;
	}

	public void setIdProperty(String property)
	{
		m_idProperty = property;
	}

	public void setLogFileProperty(String property)
	{
		m_logFileProperty = property;
	}

	public void doWork() throws BuildException
	{
		Thread thread = Thread.currentThread();

		if (m_nameProperty != null)
		{
			getProject().setProperty(m_nameProperty, thread.getName());
		}

		if (m_idProperty != null)
		{
			getProject().setProperty(m_idProperty, Long.toString(thread.getId()));
		}

		if (m_logFileProperty != null &&
			thread instanceof TasksThread &&
			((TasksThread)thread).getLogFile() != null)
		{
			getProject().setProperty(m_logFileProperty, ((TasksThread)thread).getLogFile());
		}
	}
}
