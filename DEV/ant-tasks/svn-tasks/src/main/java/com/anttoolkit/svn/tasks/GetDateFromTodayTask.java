package com.anttoolkit.svn.tasks;

import com.anttoolkit.common.*;

import org.apache.tools.ant.*;

import java.util.*;
import java.text.*;

public class GetDateFromTodayTask
	extends GenericTask
{
	private static final String DATE_FORMAT = "{0}-{1}-{2} 00:00:00";
	
	public void setDaysToAdd(int days)
	{
		m_daysToAdd = days;
	}

	public void setPropertyName(String name)
	{
		m_propertyName = name;
	}

	public void doWork()
			throws BuildException
	{
		verify();
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, m_daysToAdd);

		String year = Integer.toString(calendar.get(Calendar.YEAR));

		String month = Integer.toString(calendar.get(Calendar.MONTH) + 1);
		month = month.length() == 1 ? "0" + month : month;

		String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
		day = day.length() == 1 ? "0" + day : day;

		this.getProject().setProperty(m_propertyName,
			MessageFormat.format(DATE_FORMAT, new String[] {year, month, day}));
	}

	private void verify()
	{
		if (m_propertyName == null || m_propertyName.trim().length() == 0)
		{
			throw new BuildException("Property name doesn't specified");
		}
	}

	
	private int m_daysToAdd = 0;
	private String m_propertyName;
}
