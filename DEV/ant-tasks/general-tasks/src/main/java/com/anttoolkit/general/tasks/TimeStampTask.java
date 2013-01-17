package com.anttoolkit.general.tasks;

import java.text.*;
import java.util.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

import com.anttoolkit.common.*;

public class TimeStampTask
		extends GenericTask
{
	private List<CustomFormat> customFormats = new LinkedList<CustomFormat>();
	private String prefix = "";

	/**
	 * Set a prefix for the properties. If the prefix does not end with a "."
	 * one is automatically added.
	 * @param prefix the prefix to use.
	 * @since Ant 1.5
	 */
	public void setPrefix(String prefix)
	{
		this.prefix = prefix;

		if (!this.prefix.endsWith("."))
		{
			this.prefix += ".";
		}
	}

	/**
	 * create a custom format with the current prefix.
	 * @return a ready to fill-in format
	 */
	public CustomFormat createFormat()
	{
		CustomFormat cts = new CustomFormat();
		customFormats.add(cts);
		return cts;
	}

	public void doWork() throws BuildException
	{
		try
		{
			Date d = new Date();

			for (CustomFormat cts : customFormats)
			{
				cts.execute(getProject(), d, getLocation());
			}

			SimpleDateFormat dstamp = new SimpleDateFormat ("yyyyMMdd");
			setProperty("DSTAMP", dstamp.format(d));

			SimpleDateFormat tstamp = new SimpleDateFormat ("HHmm");
			setProperty("TSTAMP", tstamp.format(d));

			SimpleDateFormat today = new SimpleDateFormat ("MMMM d yyyy", Locale.US);
			setProperty("TODAY", today.format(d));
		}
		catch (Exception e)
		{
			throw new BuildException(e);
		}
	}

	/**
	 * This nested element that allows a property to be set
	 * to the current date and time in a given format.
	 * The date/time patterns are as defined in the
	 * Java SimpleDateFormat class.
	 * The format element also allows offsets to be applied to
	 * the time to generate different time values.
	 * @todo consider refactoring out into a re-usable element.
	 */
	public class CustomFormat
	{
		private TimeZone timeZone;
		private String propertyName;
		private String pattern;
		private String language;
		private String country;
		private String variant;
		private int offset = 0;
		private int field = Calendar.DATE;

		/**
		 * Create a format
		 */
		public CustomFormat()
		{
		}

		/**
		 *  The property to receive the date/time string in the given pattern
		 * @param propertyName the name of the property.
		 */
		public void setProperty(String propertyName)
		{
			this.propertyName = propertyName;
		}

		/**
		 * The date/time pattern to be used. The values are as
		 * defined by the Java SimpleDateFormat class.
		 * @param pattern the pattern to use.
		 * @see java.text.SimpleDateFormat
		 */
		public void setPattern(String pattern)
		{
			this.pattern = pattern;
		}

		/**
		 * The locale used to create date/time string.
		 * The general form is "language, country, variant" but
		 * either variant or variant and country may be omitted.
		 * For more information please refer to documentation
		 * for the java.util.Locale  class.
		 * @param locale the locale to use.
		 * @see java.util.Locale
		 */
		public void setLocale(String locale)
		{
			StringTokenizer st = new StringTokenizer(locale, " \t\n\r\f,");

			try
			{
				language = st.nextToken();

				if (st.hasMoreElements())
				{
					country = st.nextToken();

					if (st.hasMoreElements())
					{
						variant = st.nextToken();
						if (st.hasMoreElements())
						{
							throw new BuildException("bad locale format", getLocation());
						}
					}
				}
				else
				{
					country = "";
				}
			}
			catch (NoSuchElementException e)
			{
				throw new BuildException("bad locale format", e, getLocation());
			}
		}

		/**
		 * The timezone to use for displaying time.
		 * The values are as defined by the Java TimeZone class.
		 * @param id the timezone value.
		 * @see java.util.TimeZone
		 */
		public void setTimezone(String id)
		{
			timeZone = TimeZone.getTimeZone(id);
		}

		/**
		 * The numeric offset to the current time.
		 * @param offset the offset to use.
		 */
		public void setOffset(int offset)
		{
			this.offset = offset;
		}

		/**
		 * Set the unit type (using String).
		 * @param unit the unit to use.
		 * @deprecated since 1.5.x.
		 *             setUnit(String) is deprecated and is replaced with
		 *             setUnit(Tstamp.Unit) to make Ant's
		 *             Introspection mechanism do the work and also to
		 *             encapsulate operations on the unit in its own
		 *             class.
		 */
		public void setUnit(String unit)
		{
			log("DEPRECATED - The setUnit(String) method has been deprecated."
				+ " Use setUnit(Tstamp.Unit) instead.");

			Unit u = new Unit();
			u.setValue(unit);
			field = u.getCalendarField();
		}

		/**
		 * The unit of the offset to be applied to the current time.
		 * Valid Values are
		 * <ul>
		 *    <li>millisecond</li>
		 *    <li>second</li>
		 *    <li>minute</li>
		 *    <li>hour</li>
		 *    <li>day</li>
		 *    <li>week</li>
		 *    <li>month</li>
		 *    <li>year</li>
		 * </ul>
		 * The default unit is day.
		 * @param unit the unit to use.
		 */
		public void setUnit(Unit unit)
		{
			field = unit.getCalendarField();
		}

		/**
		 * validate parameter and execute the format.
		 * @param project project to set property in.
		 * @param date date to use as a starting point.
		 * @param location line in file (for errors)
		 */
		public void execute(Project project, Date date, Location location)
		{
			if (propertyName == null)
			{
				throw new BuildException("property attribute must be provided", location);
			}

			if (pattern == null)
			{
				throw new BuildException("pattern attribute must be provided", location);
			}

			SimpleDateFormat sdf;
			if (language == null)
			{
				sdf = new SimpleDateFormat(pattern);
			}
			else if (variant == null)
			{
				sdf = new SimpleDateFormat(pattern, new Locale(language, country));
			}
			else
			{
				sdf = new SimpleDateFormat(pattern, new Locale(language, country, variant));
			}

			if (offset != 0)
			{
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				calendar.add(field, offset);
				date = calendar.getTime();
			}

			if (timeZone != null)
			{
				sdf.setTimeZone(timeZone);
			}

			TimeStampTask.this.setProperty(propertyName, sdf.format(date));
		}
	}

	/**
	 * set of valid units to use for time offsets.
	 */
	public static class Unit extends EnumeratedAttribute
	{

		private static final String MILLISECOND = "millisecond";
		private static final String SECOND = "second";
		private static final String MINUTE = "minute";
		private static final String HOUR = "hour";
		private static final String DAY = "day";
		private static final String WEEK = "week";
		private static final String MONTH = "month";
		private static final String YEAR = "year";

		private static final String[] UNITS = {
												MILLISECOND,
												SECOND,
												MINUTE,
												HOUR,
												DAY,
												WEEK,
												MONTH,
												YEAR
											  };

		private Map calendarFields = new HashMap();

		/** Constructor for Unit enumerated type. */
		public Unit()
		{
			calendarFields.put(MILLISECOND,
							   new Integer(Calendar.MILLISECOND));
			calendarFields.put(SECOND, new Integer(Calendar.SECOND));
			calendarFields.put(MINUTE, new Integer(Calendar.MINUTE));
			calendarFields.put(HOUR, new Integer(Calendar.HOUR_OF_DAY));
			calendarFields.put(DAY, new Integer(Calendar.DATE));
			calendarFields.put(WEEK, new Integer(Calendar.WEEK_OF_YEAR));
			calendarFields.put(MONTH, new Integer(Calendar.MONTH));
			calendarFields.put(YEAR, new Integer(Calendar.YEAR));
		}

		/**
		 * Convert the value to int unit value.
		 * @return an int value.
		 */
		public int getCalendarField()
		{
			String key = getValue().toLowerCase(Locale.ENGLISH);
			Integer i = (Integer) calendarFields.get(key);
			return i.intValue();
		}

		/**
		 * Get the valid values.
		 * @return the value values.
		 */
		public String[] getValues()
		{
			return UNITS;
		}
	}

	/**
	 * helper that encapsulates prefix logic and property setting
	 * policy (i.e. we use setNewProperty instead of setProperty).
	 */
	private void setProperty(String name, String value)
	{
		PropertyHelper helper = PropertyHelper.getPropertyHelper(getProject());
		helper.setProperty(prefix + name, value, false);
	}
}