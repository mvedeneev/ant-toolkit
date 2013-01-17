package com.anttoolkit.general.tasks.maps;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;
import com.anttoolkit.general.tasks.maps.util.*;

public class PutMapElementTask
		extends GenericTask
{
	private String m_mapName;
	private String m_key;
	private String m_value;

	public void setMapName(String name)
	{
		m_mapName = name;
	}

	public void setKey(String key)
	{
		m_key = key;
	}

	public void setValue(String value)
	{
		m_value = value;
	}

	public void doWork()
			throws BuildException
	{
		MapManager.putMapElement(m_mapName, m_key, m_value);
	}

}
