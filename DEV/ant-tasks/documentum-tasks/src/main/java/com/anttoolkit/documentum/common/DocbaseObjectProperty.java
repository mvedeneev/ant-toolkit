package com.anttoolkit.documentum.common;

import com.anttoolkit.common.*;

import com.documentum.fc.common.*;
import com.documentum.fc.client.*;

import java.util.*;

import org.apache.tools.ant.*;

public class DocbaseObjectProperty
{
	public DocbaseObjectProperty(){}

	public DocbaseObjectProperty(String propertyName,
								 String propertyValue)
	{
		setName(propertyName);
		setValue(propertyValue);
	}

	public void setName(String name)
	{
		m_name = name;
	}

	public String getName()
	{
		return m_name;
	}

	public void setValue(String value)
	{
		m_value = value;
	}

	public String getValue()
	{
		return m_value;
	}

	public void setQuery(String query)
	{
		m_query = query;
	}

	public String getQuery()
	{
		return m_query;
	}

	public void setFormat(String format)
	{
		m_format = format;
	}

	public String getFormat()
	{
		return m_format;
	}

	boolean isMetadataInitialized()
	{
		return m_isMetadataInitialized;
	}

	void setMetadata(int dataType,
							int length,
							boolean isRepeatin)
	{
		m_dataType = dataType;
		m_length = length;
		m_isRepeating = isRepeatin;
		m_isMetadataInitialized = true;
	}

	public void setDataType(int type)
	{
		m_dataType = type;
	}

	public int getDataType()
	{
		return m_dataType;
	}

	public int getLength()
	{
		return m_length;
	}

	public boolean isRepeating()
	{
		return m_isRepeating;
	}

	public Object resolvePropertyValue(DocbaseSession session)
			throws BuildException
	{
		if (m_resolvedValue != null)
		{
			return m_resolvedValue;
		}

		if (m_value != null)
		{
			m_resolvedValue = getSimpleValue();
		}
		else if (m_query != null)
		{
			m_resolvedValue = getValueFromQuery(session);
		}
		else
		{
			return null;
		}

		return m_resolvedValue;
	}

	private Object getValueFromQuery(DocbaseSession session)
			throws BuildException
	{
		try
		{
			switch (m_dataType)
			{
				case IDfType.DF_BOOLEAN:
					return BooleanHelper.getBoxedBoolean(DqlHelper.getBooleanParamFromFirstString(session, m_query));
				case IDfType.DF_DOUBLE:
					return new Double(DqlHelper.getDoubleParamFromFirstString(session, m_query));
				case IDfType.DF_ID:
					return DqlHelper.getIdParamFromFirstString(session, m_query);
				case IDfType.DF_INTEGER:
					return new Integer(DqlHelper.getIntegerParamFromFirstString(session, m_query));
				case IDfType.DF_STRING:
					return DqlHelper.getStringParamFromFirstString(session, m_query);
				case IDfType.DF_TIME:
					return DqlHelper.getTimeParamFromFirstString(session, m_query);
			}

			DqlHelper.getParamFromFirstString(session, this.getQuery());
		}
		catch (DfEndOfCollectionException e)
		{
			throw new BuildException("No results returned by the query for the property " + this.getName() +
			"\r\nQuery: " + this.getQuery());
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to execute query\r\n" + e.toString());
		}

		throw new BuildException("Undefined datatype for property " + m_name);
	}

	private Object getSimpleValue()
			throws BuildException
	{
		switch (m_dataType)
		{
			case IDfType.DF_BOOLEAN:
				return BooleanHelper.getBoxedBoolean(m_value);
			case IDfType.DF_DOUBLE:
				return new Double(m_value);
			case IDfType.DF_ID:
				return getIDValue();
			case IDfType.DF_INTEGER:
				return new Integer(m_value);
			case IDfType.DF_STRING:
				return m_value;
			case IDfType.DF_TIME:
				return getTimeValue();
		}

		throw new BuildException("Undefined datatype for property " + m_name);
	}

	private IDfId getIDValue()
	{
		if (m_value.equals(DfId.DF_NULLID_STR) || m_value.trim().toUpperCase().equals("NULL"))
		{
			return DfId.DF_NULLID;
		}

		return new DfId(m_value);
	}

	private IDfTime getTimeValue()
	{
		String newValue = m_value.trim().toUpperCase();
		if (newValue.equals("TODAY"))
		{
			return new DfTime(getCurrentCalendar().getTime());
		}
		else if (newValue.equals("NOW"))
		{
			return new DfTime();
		}
		else if (newValue.equals("YESTERDAY"))
		{
			Calendar calendar = getCurrentCalendar();
			calendar.add(Calendar.DATE, -1);
			return new DfTime(calendar.getTime());
		}
		else if (newValue.equals("TOMORROW"))
		{
			Calendar calendar = getCurrentCalendar();
			calendar.add(Calendar.DATE, 1);
			return new DfTime(calendar.getTime());
		}
		else if (newValue.equals("NULL"))
		{
			return DfTime.DF_NULLDATE;
		}

		return new DfTime(m_value, m_format);
	}

	private Calendar getCurrentCalendar()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.clear(Calendar.MILLISECOND);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.HOUR_OF_DAY);
		calendar.clear(Calendar.HOUR);

		return calendar;
	}

	private String m_name = null;
	private String m_value = null;
	private String m_query = null;
	private String m_format = IDfTime.DF_TIME_PATTERN_DEFAULT;

	private int m_dataType = IDfAttr.DM_UNDEFINED;
	private int m_length = -1;
	private boolean m_isRepeating = false;
	private boolean m_isMetadataInitialized = false;

	private Object m_resolvedValue = null;
}
