package com.anttoolkit.general.tasks;

import java.io.*;
import java.util.*;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;

public class FileLinesLoopTask
		extends GenericTask
		implements TaskContainer
{
	private String m_file = null;
	private String m_lineTextProperty = null;
	private String m_encoding = null;

	private List<Task> m_tasks = new LinkedList<Task>();

	public void addTask(Task task)
	{
		m_tasks.add(task);
	}

	public void setFile(String file)
	{
		m_file = file;
	}

	public void setEncoding(String encoding)
	{
		m_encoding = encoding;
	}

	public void setLineTextProperty(String property)
	{
		m_lineTextProperty = property;
	}

	public void doWork() throws BuildException
	{
		String fullPath = getFileFullPath(m_file);
		if (fullPath == null)
		{
			throw new BuildException("File should be specified");
		}

		if (m_lineTextProperty == null)
		{
			throw new BuildException("Line text property should be specified");
		}

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
				return;
			}

			in = new FileInputStream(file);
			reader = m_encoding == null ?
					new BufferedReader(new InputStreamReader(in)) :
					new BufferedReader(new InputStreamReader(in, m_encoding));

			String line;

			while ((line = reader.readLine()) != null)
			{
				this.getProject().setProperty(m_lineTextProperty, line);

				for (Task task : m_tasks)
				{
					task.perform();
				}
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
	}

}
