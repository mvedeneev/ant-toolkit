package com.anttoolkit.general.tasks.props;

import java.text.*;
import java.util.*;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;

public class ConditionTask
		extends GenericTask
		implements TaskContainer
{
	private static final String EQUAL_CONDITION_1 = "=";
	private static final String EQUAL_CONDITION_2 = "==";
	private static final String NOT_EQUAL_CONDITION_1 = "!=";
	private static final String NOT_EQUAL_CONDITION_2 = "<>";
	private static final String MORE_CONDITION = ">";
	private static final String LESS_CONDITION = "<";
	private static final String MORE_OR_EQUAL_CONDITION = ">=";
	private static final String LESS_OR_EQUAL_CONDITION = "<=";

	private static class DoubleComparator implements Comparator<Double>
	{
		public static final DoubleComparator instance = new DoubleComparator();

		private DoubleComparator() {}

		public int compare(Double arg1, Double arg2)
		{
			return arg1.compareTo(arg2);
		}
	}

	private static class StringComparator implements Comparator<String>
	{
		public static final StringComparator instance = new StringComparator();

		private StringComparator() {}

		public int compare(String arg1, String arg2)
		{
			return arg1.compareTo(arg2);
		}
	}

	private static class DateComparator implements Comparator<Date>
	{
		public static final DateComparator instance = new DateComparator();

		private DateComparator() {}

		public int compare(Date arg1, Date arg2)
		{
			return arg1.compareTo(arg2);
		}
	}

	private String m_arg1 = null;
	private String m_arg2 = null;
	private boolean m_ignoreCase = false;
	private boolean m_isDate = false;
	private String m_format = null;
	private String m_locale = null;
	private String m_condition = null;

	private List<Task> m_tasks = new LinkedList<Task>();

	public void setArg1(String value)
	{
		m_arg1 = value;
	}

	public void setArg2(String value)
	{
		m_arg2 = value;
	}

	public void setIsDate(boolean isDate)
	{
		m_isDate = isDate;
	}

	public void setIgnoreCase(boolean ignore)
	{
		m_ignoreCase = ignore;
	}

	public void setFormat(String format)
	{
		m_format = format;
	}

	public void setLocale(String locale)
	{
		m_locale = locale;
	}

	public void setCondition(String condition)
	{
		m_condition = condition;
	}

	public void addTask(Task task)
	{
		if (task == null)
		{
			return;
		}

		m_tasks.add(task);
	}

	public void doWork()
			throws BuildException
	{

		validate();

		m_arg1 = m_ignoreCase ? m_arg1.toLowerCase() : m_arg1;
		m_arg2 = m_ignoreCase ? m_arg2.toLowerCase() : m_arg2;

		m_arg1 = m_arg1.trim();
		m_arg2 = m_arg2.trim();

		if (!isConditionSatisfied() || m_tasks.isEmpty())
		{
			return;
		}

		this.log("Perform tasks for condition: " + m_arg1 + " " + m_condition + " " + m_arg2);

		for (Task task : m_tasks)
		{
			task.perform();
		}
	}

	private void validate()
	{
		if (m_arg1 == null)
		{
			throw new BuildException("arg1 is not specified");
		}

		if (m_arg2 == null)
		{
			throw new BuildException("arg2 is not specified");
		}

		if (m_condition == null)
		{
			throw new BuildException("condition is not specified");
		}
	}

	private boolean isConditionSatisfied()
	{
		if (m_isDate)
		{
			try
			{
				String[] parts = m_locale == null ? null : m_locale.split(",");
				SimpleDateFormat formatter = parts == null ?
						new SimpleDateFormat(m_format) :
						new SimpleDateFormat(m_format, new Locale(parts[0], parts[1]));

				Date arg1 = formatter.parse(m_arg1);
				Date arg2 = formatter.parse(m_arg2);

				return isConditionSatisfied(arg1, arg2, DateComparator.instance);
			}
			catch (ParseException e)
			{
				throw new BuildException("One of date arguments has incorrect format: " + m_arg1 + ", " + m_arg2);
			}
		}

		try
		{
			Double arg1 = Double.parseDouble(m_arg1);
			Double arg2 = Double.parseDouble(m_arg2);
			return isConditionSatisfied(arg1, arg2, DoubleComparator.instance);
		}
		catch (NumberFormatException e) {}

		return isConditionSatisfied(m_arg1, m_arg2, StringComparator.instance);
	}

	private boolean isConditionSatisfied(Object arg1, Object arg2, Comparator comparator)
	{
		if (EQUAL_CONDITION_1.equals(m_condition) || EQUAL_CONDITION_2.equals(m_condition))
		{
			return comparator.compare(arg1, arg2) == 0;
		}

		if (NOT_EQUAL_CONDITION_1.equals(m_condition) || NOT_EQUAL_CONDITION_2.equals(m_condition))
		{
			return comparator.compare(arg1, arg2) != 0;
		}

		if (MORE_CONDITION.equals(m_condition))
		{
			return comparator.compare(arg1, arg2) > 0;
		}

		if (LESS_CONDITION.equals(m_condition))
		{

			return comparator.compare(arg1, arg2) < 0;
		}

		if (MORE_OR_EQUAL_CONDITION.equals(m_condition))
		{
			return comparator.compare(arg1, arg2) >= 0;
		}

		if (LESS_OR_EQUAL_CONDITION.equals(m_condition))
		{
			return comparator.compare(arg1, arg2) <= 0;
		}

		throw new BuildException("Incorrect condition specified: " + m_condition);
	}

}
