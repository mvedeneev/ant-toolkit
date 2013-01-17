package com.anttoolkit.general.tasks.maps;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;
import com.anttoolkit.general.tasks.maps.util.*;

public class GetMapElementTask
		extends GenericTask
{
	private String m_mapName;
	private String m_key;
	private String m_propertyName;

	public void setMapName(String name)
	{
		m_mapName = name;
	}

	public void setKey(String key)
	{
		m_key = key;
	}

	public void setPropertyName(String name)
	{
		m_propertyName = name;
	}

	public void doWork()
			throws BuildException
	{
		verify();

		this.getProject().setProperty(m_propertyName, MapManager.getMapElement(m_mapName, m_key));
	}

	private void verify()
	{
		if (m_mapName == null || m_mapName.trim().length() == 0)
		{
			throw new BuildException("Map name doesn't specified");
		}

		if (m_key == null)
		{
			throw new BuildException("Mapy key doesn't specified");
		}

		if (m_propertyName == null || m_propertyName.trim().length() == 0)
		{
			throw new BuildException("Property name doesn't specified");
		}
	}
}
