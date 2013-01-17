package com.anttoolkit.general.tasks.maps;

import java.util.*;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;
import com.anttoolkit.general.tasks.*;
import com.anttoolkit.general.tasks.maps.util.*;

public class GetRandomMapElementTask
		extends GenericTask
{
	private String m_mapName = null;
	private String m_keyProperty = null;
	private String m_valueProperty = null;

	public void setMapName(String name)
	{
		m_mapName = name;
	}

	public void setValueProperty(String name)
	{
		m_valueProperty = name;
	}

	public void setKeyProperty(String name)
	{
		m_keyProperty = name;
	}

	public void doWork() throws BuildException
	{
		if (m_mapName == null)
		{
			throw new BuildException("Map name should be specified");
		}

		if (m_valueProperty == null && m_keyProperty == null)
		{
			throw new BuildException("Value and key property should be specified");
		}

		int size = MapManager.getMapSize(m_mapName);
		int randomIndex = GenerateRandomIntegerTask.getRandomInt(0, size - 1);
		Map<String, String> map = MapManager.getMapData(m_mapName);
		Set<Map.Entry<String, String>> set = map.entrySet();

		int i = 0;
		for (Map.Entry<String, String> entry : set)
		{
			if (i == randomIndex)
			{
				if (m_keyProperty != null)
				{
					getProject().setProperty(m_keyProperty, entry.getKey());
				}

				if (m_valueProperty != null)
				{
					getProject().setProperty(m_valueProperty, entry.getValue());
				}

				return;
			}

			i++;
		}
	}
}
