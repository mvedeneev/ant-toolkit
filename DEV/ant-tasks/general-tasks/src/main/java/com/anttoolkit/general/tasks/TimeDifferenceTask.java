package com.anttoolkit.general.tasks;

import java.text.*;
import java.util.*;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;

public class TimeDifferenceTask
		extends GenericTask
{
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

	private static final String MILLISECONDS = "ms";
	private static final String SECONDS = "s";
	private static final String MINUTES = "m";
	private static final String HOURS = "h";

	private String m_time1 = null;
	private String m_time2 = null;
	private String m_format = null;
	private Locale m_locale = Locale.getDefault();
	private String m_differenceUnits = SECONDS;
	private String m_property = null;

	public void setTime1(String time)
	{
		m_time1 = time;
	}

	public void setTime2(String time)
	{
		m_time2 = time;
	}

	public void setTimeFormat(String format)
	{
		m_format = format;
	}

	public void setLocale(String locale)
	{
		String[] parts = locale.split(",");
		if (parts.length != 2)
		{
			throw new BuildException("Invalid locale specified: " + locale);
		}

		m_locale = new Locale(parts[0], parts[1]);
	}

	public void setDifferenceUnits(String units)
	{
		m_differenceUnits = units.trim().toLowerCase();
	}

	public void setProperty(String property)
	{
		m_property = property;
	}

	public void doWork() throws BuildException
	{
		if (m_format == null)
		{
			throw new BuildException("Time format should be specified");
		}

		if (m_time1 == null)
		{
			throw new BuildException("time1 argument couldn't be null");
		}

		if (m_time2 == null)
		{
			throw new BuildException("time2 argument couldn't be null");
		}

		if (!MILLISECONDS.equals(m_differenceUnits) && !SECONDS.equals(m_differenceUnits) &&
			!MINUTES.equals(m_differenceUnits) && !HOURS.equals(m_differenceUnits))
		{
			throw new BuildException("Incorrect difference units specified: " + m_differenceUnits);
		}

		SimpleDateFormat dateFormat = null;

		try
		{
			dateFormat = new SimpleDateFormat(m_format, m_locale);
		}
		catch (IllegalArgumentException e)
		{
			throw new BuildException("Invalid time format specified: " + m_format);
		}

		Date time1 = null;
		Date time2 = null;

		try
		{
			time1 = dateFormat.parse(m_time1.toUpperCase());
		}
		catch (Throwable e)
		{
			throw new BuildException("Failed to parse time1: " + m_time1);
		}

		try
		{
			time2 = dateFormat.parse(m_time2.toUpperCase());
		}
		catch (Throwable e)
		{
			throw new BuildException("Failed to parse time2: " + m_time2);
		}

		long milliseconds = time2.getTime() - time1.getTime();

		if (m_property != null)
		{
			getProject().setProperty(m_property, getDifferenceInUnits(milliseconds));
		}
	}

	private String getDifferenceInUnits(long milliseconds)
	{
		Float dirrefence = null;

		if (MILLISECONDS.equals(m_differenceUnits))
		{
			dirrefence = (float)milliseconds;
		}

		if (SECONDS.endsWith(m_differenceUnits))
		{
			dirrefence = (float)milliseconds / (float)1000;
		}

		if (MINUTES.endsWith(m_differenceUnits))
		{
			dirrefence = (float)milliseconds / (float)60000;
		}

		if (HOURS.endsWith(m_differenceUnits))
		{
			dirrefence = (float)milliseconds / (float)3600000;
		}

		if (dirrefence == null)
		{
			throw new BuildException("Incorrect difference units specified: " + m_differenceUnits);
		}

		return DECIMAL_FORMAT.format(dirrefence);
	}
}
