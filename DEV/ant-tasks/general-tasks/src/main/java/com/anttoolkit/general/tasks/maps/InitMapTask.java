package com.anttoolkit.general.tasks.maps;

import java.io.*;
import java.util.*;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;
import com.anttoolkit.general.tasks.maps.util.*;

public class InitMapTask
		extends GenericTask
{
	private String m_mapName;
	private String m_file;
	private Map<String, String> m_configuredMap = new HashMap<String, String>();
	private String m_encoding;
	private String m_separatorPattern = ",";
	private boolean m_trimValues = true;
	private String m_mapSizeProperty;

	public static class MapEntry
	{
		private String m_key = null;
		private String m_value = null;

		public void setKey(String name)
		{
			m_key = name;
		}

		public String getKey()
		{
			return m_key;
		}

		public void setValue(String value)
		{
			m_value = value;
		}

		public String getValue()
		{
			return m_value;
		}
	}

	public void setMapName(String name)
	{
		m_mapName = name;
	}

	public void addConfiguredEntry(MapEntry entry)
	{
		m_configuredMap.put(entry.getKey(), entry.getValue());
	}

	public void setFile(String file)
	{
		m_file = file;
	}

	public void setFileEncoding(String encoding)
	{
		m_encoding = encoding;
	}

	public void setSeparatorPattern(String separator)
	{
		m_separatorPattern = separator;
	}

	public void setTrimValues(boolean trim)
	{
		m_trimValues = trim;
	}

	public void doWork()
			throws BuildException
	{
		verify();

		Map<String, String> data = getMapData();

		MapManager.initMap(m_mapName, data);

		if (m_mapSizeProperty != null)
		{
			this.getProject().setProperty(m_mapSizeProperty, Integer.toString(data.size()));
		}
	}

	private void verify()
	{
		if (m_mapName == null || m_mapName.trim().length() == 0)
		{
			throw new BuildException("Map name doesn't specified");
		}

		if (m_separatorPattern == null || m_separatorPattern.trim().length() == 0)
		{
			throw new BuildException("Map key and value separator doesn't specified");
		}
	}

	private Map<String, String> getMapData()
	{
		if (m_file == null)
		{
			return m_configuredMap;
		}

		String fullPath = getFileFullPath(m_file);

		InputStream in = null;
		BufferedReader reader = null;

		try
		{
			File file = new File(fullPath);
			if (!file.isFile() || !file.exists())
			{
				throw new BuildException("Incorrect file specified: " + fullPath);
			}

			int fileLength = (int)file.length();
			if (fileLength == 0)
			{
				return m_configuredMap;
			}

			in = new FileInputStream(file);
			reader = m_encoding == null ?
					new BufferedReader(new InputStreamReader(in)) :
					new BufferedReader(new InputStreamReader(in, m_encoding));

			String line;

			while ((line = reader.readLine()) != null)
			{
				String[] parts = line.split(m_separatorPattern);
				if (parts.length != 2)
				{
					continue;
				}

				String key = m_trimValues ? parts[0].trim() : parts[0];
				String value = m_trimValues ? parts[1].trim() : parts[1];

				if (key.length() != 0 && value.length() != 0)
				{

				}

				m_configuredMap.put(key, value);
    		}
		}
		catch (FileNotFoundException e)
		{
			throw new BuildException("Failed to open file " + fullPath, e);
		}
		catch (IOException e)
		{
			throw new BuildException("Failed read file " + fullPath, e);
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (Throwable ex) {}
			}

			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (Throwable ex) {}
			}
		}

		return m_configuredMap;
	}
}
