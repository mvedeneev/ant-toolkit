package com.anttoolkit.general.tasks.maps;

import java.util.*;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;
import com.anttoolkit.general.tasks.maps.util.*;

public class MapLoopTask
		extends GenericTask
		implements TaskContainer
{
	private List<Task> m_tasks = new LinkedList<Task>();

	private String m_mapName;
	private String m_valuePropertyName;
	private String m_keyPropertyName;

	public void addTask(Task task)
	{
		m_tasks.add(task);
	}

	public void setMapName(String name)
	{
		m_mapName = name;
	}

	public void setValuePropertyName(String name)
	{
		m_valuePropertyName = name;
	}

	public void setKeyPropertyName(String name)
	{
		m_keyPropertyName = name;
	}

	public void doWork()
			throws BuildException
	{
		if (m_mapName == null)
		{
			throw new BuildException("Map name should be specified");
		}

		int tasksCount = m_tasks.size();
		if (tasksCount == 0)
		{
			return;
		}

		Map<String, String> map = MapManager.getMapData(m_mapName);

		Set<String> keys = map.keySet();
		for (String key : keys)
		{
			if (m_keyPropertyName != null)
			{
				this.getProject().setProperty(m_keyPropertyName, key);
			}

			if (m_valuePropertyName != null)
			{
				this.getProject().setProperty(m_valuePropertyName, map.get(key));
			}

			int taskCount = m_tasks.size();
			for (int j = 0; j < taskCount; j++)
			{
				(m_tasks.get(j)).perform();
			}
		}
	}
}
