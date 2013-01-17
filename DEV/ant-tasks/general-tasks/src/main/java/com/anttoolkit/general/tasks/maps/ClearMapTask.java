package com.anttoolkit.general.tasks.maps;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;
import com.anttoolkit.general.tasks.maps.util.*;

public class ClearMapTask
		extends GenericTask
{
	private String m_mapName = null;

	public void setMapName(String name)
	{
		m_mapName = name;
	}

	public void doWork() throws BuildException
	{
		if (m_mapName == null)
		{
			throw new BuildException("Map name should be specified");
		}

		MapManager.clearMap(m_mapName);
	}
}
