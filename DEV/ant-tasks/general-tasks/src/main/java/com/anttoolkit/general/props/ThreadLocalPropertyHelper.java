package com.anttoolkit.general.props;

import org.apache.tools.ant.*;

import java.util.*;

public class ThreadLocalPropertyHelper
		implements PropertyHelper.PropertyEvaluator, PropertyHelper.PropertySetter
{
	private static final String THREAD_LOCAL_PROPERTY_PREFIX = "threadLocal:";

	private static ThreadLocal<Map<String, Object>> m_threadProperties = new ThreadLocal<Map<String, Object>>()
	{
		protected Map<String, Object> initialValue()
		{
			return new HashMap<String, Object>();
		}
	};

	public static void setNewThreadLocalProperty(String name, Object value)
	{
		if (!m_threadProperties.get().containsKey(name))
		{
			m_threadProperties.get().put(name, value);
		}
	}

	public static void setThreadLocalProperty(String name, Object value)
	{
		m_threadProperties.get().put(name, value);
	}

	public Object evaluate(String property, PropertyHelper propertyHelper)
	{
		return m_threadProperties.get().get(property);
	}

	public boolean setNew(String property, Object value, PropertyHelper propertyHelper)
	{
		if (property != null && property.startsWith(THREAD_LOCAL_PROPERTY_PREFIX))
		{
			setThreadLocalProperty(property, value);
			return true;
		}

		return false;
	}

	public boolean set(String property, Object value, PropertyHelper propertyHelper)
	{
		if (property != null && property.startsWith(THREAD_LOCAL_PROPERTY_PREFIX))
		{
			setThreadLocalProperty(property, value);
			return true;
		}

		return false;
	}
}
