package com.anttoolkit.general.tasks.maps.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.tools.ant.*;

public class MapManager
{
	private static final String GLOBAL_MAP_PREFIX = "g:";

	private static volatile Map<String, Map<String, String>> m_globalMaps = new HashMap<String, Map<String, String>>();
	private static ThreadLocal<Map<String, Map<String, String>>> m_localMaps = new ThreadLocal<Map<String, Map<String, String>>>()
	{
		protected synchronized Map<String, Map<String, String>> initialValue()
		{
			return new Hashtable<String, Map<String, String>>();
		}
	};

	public static Map<String, String> getMapData(String name)
	{
		return getMapData(name, false);
	}

	public static Map<String, String> getMapData(String name, boolean createIfNotExist)
	{
		if (name == null)
		{
			throw new BuildException("Map name couldn't be null");
		}

		if (isGlobalMap(name))
		{
			synchronized (m_globalMaps)
			{
				if (m_globalMaps.containsKey(name))
				{
					return m_globalMaps.get(name);
				}

				if (!createIfNotExist)
				{
					throw new BuildException("Map with name " + name + " doesn't initialized");
				}

				m_globalMaps.put(name, new ConcurrentHashMap<String, String>());

				return m_globalMaps.get(name);
			}
		}

		if (m_localMaps.get().containsKey(name))
		{
			return m_localMaps.get().get(name);
		}

		if (!createIfNotExist)
		{
			throw new BuildException("Map with name " + name + " doesn't initialized");
		}

		m_localMaps.get().put(name, new HashMap<String, String>());

		return m_localMaps.get().get(name);
	}

	public static void clearMap(String name)
	{
		if (name == null)
		{
			throw new BuildException("Array name couldn't be null");
		}

		if (isGlobalMap(name))
		{
			synchronized (m_globalMaps)
			{
				Map data = m_globalMaps.get(name);
				if (data != null)
				{
					data.clear();
				}
			}

			return;
		}

		Map data = m_localMaps.get().get(name);
		if (data != null)
		{
			data.clear();
		}
	}

	public static boolean containsKey(String name, String key)
	{
		if (key == null)
		{
			throw new BuildException("Key can't be null");
		}

		Map data = getMapData(name);

		return data.containsKey(key);
	}

	public static String getMapElement(String name, String key)
	{
		if (key == null)
		{
			throw new BuildException("Key can't be null");
		}

		Map<String, String> data = getMapData(name);

		String value = data.get(key);

		return value == null ? "" : value;
	}

	public static void putMapElement(String name, String key, String value)
	{
		Map<String, String> data = getMapData(name);
		data.put(key, value);
	}

	public static void putMapElement(String name, String key, String value, boolean createMapIfNotExists)
	{
		Map<String, String> data = getMapData(name, createMapIfNotExists);
		data.put(key, value);
	}

	public static int getMapSize(String name)
	{
		return getMapData(name).size();
	}

	public static void initMap(String name, Map data)
	{
		if (name == null)
		{
			throw new BuildException("Map name couldn't be null");
		}

		if (isGlobalMap(name))
		{
			synchronized (m_globalMaps)
			{
				m_globalMaps.put(name, new ConcurrentHashMap<String, String>(data));
				return;
			}
		}

		m_localMaps.get().put(name, data);
	}

	public static void destroyMap(String name)
	{
		if (name == null)
		{
			throw new BuildException("Map name couldn't be null");
		}

		if (isGlobalMap(name))
		{
			synchronized (m_globalMaps)
			{
				m_globalMaps.remove(name);
				return;
			}
		}

		m_localMaps.get().remove(name);
	}

	public static boolean isMapExists(String name)
	{
		if (name == null)
		{
			return false;
		}

		if (isGlobalMap(name))
		{
			synchronized (m_globalMaps)
			{
				return m_globalMaps.containsKey(name);
			}
		}

		return m_localMaps.get().containsKey(name);
	}

	private static boolean isGlobalMap(String name)
	{
		return name != null && name.toLowerCase().startsWith(GLOBAL_MAP_PREFIX);
	}

}
