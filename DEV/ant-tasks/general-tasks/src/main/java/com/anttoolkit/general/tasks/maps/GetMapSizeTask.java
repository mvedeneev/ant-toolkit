package com.anttoolkit.general.tasks.maps;

import org.apache.tools.ant.*;

import com.anttoolkit.general.tasks.maps.util.*;
import com.anttoolkit.common.*;

public class GetMapSizeTask
		extends GenericTask
{
	private String m_mapName = null;
	private String m_property = null;

	public void setMapName(String name)
	{
		m_mapName = name;
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

		if (m_property == null)
		{
			throw new BuildException("Property name should be specified");
		}

		int size = MapManager.getMapSize(m_mapName);

		this.getProject().setProperty(m_property, Integer.toString(size));
	}
}