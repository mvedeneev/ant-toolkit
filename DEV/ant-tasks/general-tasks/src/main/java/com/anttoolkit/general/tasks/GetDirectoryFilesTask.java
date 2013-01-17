package com.anttoolkit.general.tasks;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.anttoolkit.general.tasks.arrays.util.ArrayManager;
import org.apache.tools.ant.*;

import com.anttoolkit.common.*;

public class GetDirectoryFilesTask
		extends GenericTask
{
	private String m_dir = null;
	private boolean m_recursive = false;
	private String m_fullNameArray = null;
	private String m_shortNameArray = null;
	private String m_nameArray = null;
	private String m_extensionArray = null;
	private String m_filter = null;

	public void setDir(String dir)
	{
		m_dir = dir;
	}

	public void setRecursive(boolean recursive)
	{
		m_recursive = recursive;
	}

	public void setFullNameArray(String array)
	{
		m_fullNameArray = array;
	}

	public void setShortNameArray(String array)
	{
		m_shortNameArray = array;
	}

	public void setNameArray(String array)
	{
		m_nameArray = array;
	}

	public void setExtensionArray(String array)
	{
		m_extensionArray = array;
	}

	public void setFilter(String filter)
	{
		m_filter = filter;
	}

	public void doWork() throws BuildException
	{
		if (m_dir == null)
		{
			throw new BuildException("Directory should be specified");
		}

		File dir = new File(m_dir);
		if (!dir.isDirectory())
		{
			throw new BuildException("Invalid directory specified: " + m_dir);
		}

		if (m_fullNameArray == null && m_shortNameArray == null &&
			m_extensionArray == null && m_nameArray == null)
		{
			return;
		}

		listDirectoryFiles(dir);
	}

	private void listDirectoryFiles(File dir)
	{
		List fullNameArray = m_fullNameArray == null ? null : ArrayManager.getArrayData(m_fullNameArray, true);
		List shortNameArray  = m_shortNameArray == null ? null : ArrayManager.getArrayData(m_shortNameArray, true);
		List nameArray = m_nameArray == null ? null : ArrayManager.getArrayData(m_nameArray, true);
		List extensionArray = m_extensionArray == null ? null : ArrayManager.getArrayData(m_extensionArray, true);

		File[] files = dir.listFiles();
		for (File file : files)
		{
			if (file.isDirectory())
			{
				if (!m_recursive)
				{
					continue;
				}

				listDirectoryFiles(file);

				continue;
			}

			String fileAbsolutePath = file.getAbsolutePath();
			if (m_filter != null && !Pattern.matches(m_filter, fileAbsolutePath))
			{
				continue;
			}

			int index = fileAbsolutePath.lastIndexOf(File.separatorChar);
			String shortName = index <= 0 ? fileAbsolutePath : fileAbsolutePath.substring(index + 1);

			index = shortName.lastIndexOf(".");
			String name = index <= 0 ? shortName : shortName.substring(0, index);
			String extension = index <= 0 ? "" : shortName.substring(index + 1).toLowerCase();

			if (fullNameArray != null)
			{
				fullNameArray.add(fileAbsolutePath);
			}

			if (shortNameArray != null)
			{
				shortNameArray.add(shortName);
			}

			if (nameArray != null)
			{
				nameArray.add(name);
			}

			if (extensionArray != null)
			{
				extensionArray.add(extension);
			}
		}
	}
}
