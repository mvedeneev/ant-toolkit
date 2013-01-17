package com.anttoolkit.general.tasks.props;

import com.anttoolkit.common.GenericTask;
import com.anttoolkit.general.props.ThreadLocalPropertyHelper;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;

public class PropertyTask /*extends Property*/
	extends GenericTask
{
	private String m_name = null;
	private String m_value = null;
	private boolean m_threadLocal = false;

	public void setThreadLocal(boolean threadLocal)
	{
		m_threadLocal = threadLocal;
	}

	public void setName(String name)
	{
		m_name = name;
	}

	public void setValue(String value)
	{
		m_value = value;
	}

/*
	protected void addProperty(String name, Object value)
	{
		if (m_threadLocal)
		{
			ThreadLocalPropertyHelper.setThreadLocalProperty(name, value);
		}
		else
		{
			PropertyHelper helper = PropertyHelper.getPropertyHelper(getProject());
			helper.setProperty(name, value, false);
		}
	}
*/

	public void doWork() throws BuildException
	{
		if (m_threadLocal)
		{
			ThreadLocalPropertyHelper.setThreadLocalProperty(m_name, m_value);
		}
		else
		{
			getProject().setProperty(m_name, m_value);
		}
	}
}
