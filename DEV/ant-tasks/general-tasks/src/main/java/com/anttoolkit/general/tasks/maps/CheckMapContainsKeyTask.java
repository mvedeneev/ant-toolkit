package com.anttoolkit.general.tasks.maps;

import com.anttoolkit.general.tasks.maps.util.*;
import org.apache.tools.ant.*;

import com.anttoolkit.common.*;

public class CheckMapContainsKeyTask
		extends GenericTask
{
	private String m_mapName = null;
	private String m_key = null;
	private String m_property = null;

	public void setMapName(String name)
	{
		m_mapName = name;
	}

	public void setKey(String key)
	{
		m_key = key;
	}

	public void setProperty(String property)
	{
		m_property = property;
	}

	public void doWork() throws BuildException
	{
		if (m_mapName == null)
		{
			throw new BuildException("Map name should be specified");
		}

		if (m_key == null)
		{
			throw new BuildException("Map key should be specified");
		}

		if (m_property == null)
		{
			throw new BuildException("Ant property name should be specified");
		}

		boolean contains = MapManager.containsKey(m_mapName, m_key);

		getProject().setProperty(m_property, Boolean.toString(contains));
	}
}
