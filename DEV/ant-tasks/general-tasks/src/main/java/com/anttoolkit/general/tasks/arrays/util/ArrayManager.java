package com.anttoolkit.general.tasks.arrays.util;

import java.util.*;

import org.apache.tools.ant.*;

public class ArrayManager
{
	private static final String GLOBAL_ARRAY_PREFIX = "g:";

	private static volatile Map<String, List> m_globalArrays = new HashMap<String, List>();
	private static ThreadLocal<Map<String, List>> m_localArrays = new ThreadLocal<Map<String, List>>()
	{
		protected synchronized Map<String, List> initialValue()
		{
			return new Hashtable<String, List>();
		}
	};

	public static List getArrayData(String name)
	{
		return getArrayData(name, false);
	}

	public static List getArrayData(String name, boolean createIfNotExist)
	{
		if (name == null)
		{
			throw new BuildException("Array name couldn't be null");
		}

		if (isGlobalArray(name))
		{
			synchronized (m_globalArrays)
			{
				if (m_globalArrays.containsKey(name))
				{
					return m_globalArrays.get(name);
				}

				if (!createIfNotExist)
				{
					throw new BuildException("Array with name " + name + " doesn't initialized");
				}

				m_globalArrays.put(name, Collections.synchronizedList(new LinkedList()));

				return m_globalArrays.get(name);
			}
		}

		if (m_localArrays.get().containsKey(name))
		{
			return m_localArrays.get().get(name);
		}

		if (!createIfNotExist)
		{
			throw new BuildException("Array with name " + name + " doesn't initialized");
		}

		m_localArrays.get().put(name, new LinkedList());

		return m_localArrays.get().get(name);
	}

	public static void clearArray(String name)
	{
		if (name == null)
		{
			throw new BuildException("Array name couldn't be null");
		}

		if (isGlobalArray(name))
		{
			synchronized (m_globalArrays)
			{
				List data = m_globalArrays.get(name);
				if (data != null)
				{
					data.clear();
				}
			}

			return;
		}

		List data = m_localArrays.get().get(name);
		if (data != null)
		{
			data.clear();
		}
	}

	public static String getArrayElement(String name, int index)
	{
		if (index < 0)
		{
			throw new BuildException("Invalid array index " + Integer.toString(index) + " specified");
		}

		List data = getArrayData(name);
		if (index >= data.size())
		{
			throw new BuildException("Index " + Integer.toString(index) + " is out of range for array " + name);
		}

		return (String)data.get(index);
	}

	public static void addArrayElement(String name, String value)
	{
		List data = getArrayData(name);
		data.add(value);
	}

	public static void addArrayElement(String name, String value, boolean createArrayIfNotExists)
	{
		List data = getArrayData(name, createArrayIfNotExists);
		data.add(value);
	}

	public static int getArraySize(String name)
	{
		return getArrayData(name).size();
	}

	public static void initArray(String name, List data)
	{
		if (name == null)
		{
			throw new BuildException("Array name couldn't be null");
		}

		if (isGlobalArray(name))
		{
			synchronized (m_globalArrays)
			{
				m_globalArrays.put(name, Collections.synchronizedList(data));
				return;
			}
		}

		m_localArrays.get().put(name, data);
	}

	public static void destroyArray(String name)
	{
		if (name == null)
		{
			throw new BuildException("Array name couldn't be null");
		}

		if (isGlobalArray(name))
		{
			synchronized (m_globalArrays)
			{
				m_globalArrays.remove(name);
				return;
			}
		}

		m_localArrays.get().remove(name);
	}

	public static boolean isArrayExists(String name)
	{
		if (name == null)
		{
			return false;
		}

		if (isGlobalArray(name))
		{
			synchronized (m_globalArrays)
			{
				return m_globalArrays.containsKey(name);
			}
		}

		return m_localArrays.get().containsKey(name);
	}

	private static boolean isGlobalArray(String name)
	{
		return name != null && name.toLowerCase().startsWith(GLOBAL_ARRAY_PREFIX);
	}
}
