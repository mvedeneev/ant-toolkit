package com.anttoolkit.common;

import org.apache.tools.ant.*;

import java.io.*;

import java.util.regex.*;

public abstract class GenericTask
		extends Task
{
	private static final int READ_FILE_BUFFER_SIZE = 1024;

	private boolean m_failOnError = true;

	public static String substituteProperties(Project project,
											  String text)
			throws BuildException
	{
		PropertyHelper helper = PropertyHelper.getPropertyHelper(project);

		return helper.replaceProperties(null, text, null);
	}

	public String substituteProperties(String text)
			throws BuildException
	{
		return substituteProperties(this.getProject(), text);
	}

	public static String getFileFullPath(Project project,
										 String fileName)
			throws BuildException
	{
		if(fileName == null || fileName.length() <= 0)
		{
			return fileName;
		}

		File file = new File(fileName);
		if(file.isAbsolute())
		{
			return fileName;
		}

		file = new File(project.getBaseDir(), fileName);
		try
		{
			return file.getCanonicalPath();
		}
		catch(IOException e)
		{
			throw new BuildException("Failed to get file canonical path\r\n" + e.toString());
		}
	}

	public String getFileFullPath(String fileName)
			throws BuildException
	{
		return getFileFullPath(this.getProject(), fileName);
	}

	public void setFailonerror(String failOnError)
	{
		if (failOnError == null)
		{
			m_failOnError = true;
			return;
		}

		failOnError = failOnError.trim().toLowerCase();

		if (failOnError.equals("false") || failOnError.equals("no") ||
			failOnError.equals("off") || failOnError.equals("0"))
		{
			m_failOnError = false;
		}
		else if (failOnError.equals("true") || failOnError.equals("yes") ||
				failOnError.equals("on") || failOnError.equals("1"))
		{
			m_failOnError = true;
		}
		else
		{
			throw new BuildException("Incorrect value '" + failOnError + "' specified for failonerror attribute");
		}
	}

	public boolean failOnError()
	{
		return m_failOnError;
	}

	public void execute()
			throws BuildException
	{
		try
		{
			doWork();
		}
		catch (Throwable e)
		{
			if (!failOnError())
			{
				log("Handled exception occured during task execution");
				log(ExceptionHelper.stackTraceToString(e));
				return;
			}

			if (e instanceof BuildException)
			{
				throw (BuildException)e;
			}

			throw new BuildException("Exception occured", e);
		}
	}

	public abstract void doWork() throws BuildException;

	protected void initDelegateTask(Task delegateTask)
	{
		String name = delegateTask.getClass().getName();
		name = name.substring(name.lastIndexOf('.') + 1);
		this.initDelegateTask(delegateTask, name);
	}

	protected void initDelegateTask(Task delegateTask, String taskName)
	{
		if (delegateTask == null)
		{
			return;
		}

		delegateTask.init();
		delegateTask.setProject(this.getProject());
		delegateTask.setOwningTarget(this.getOwningTarget());
		delegateTask.setLocation(this.getLocation());
		delegateTask.setRuntimeConfigurableWrapper(this.getRuntimeConfigurableWrapper());
		delegateTask.setTaskName(taskName);
	}

	protected String loadFileContent(String fileName)
	{
		String fullPath = getFileFullPath(fileName);
		if (fullPath == null)
		{
			return null;
		}

		InputStream in = null;

		try
		{
			File file = new File(fullPath);
			int fileLength = (int)file.length();
			if (fileLength == 0)
			{
				return null;
			}

			in = new FileInputStream(file);

			byte[] buffer = new byte[fileLength];

			int offset = 0;
			int bytesRead = 0;

			while (offset < fileLength && bytesRead >= 0)
			{
				int bytesToRead = fileLength - offset < READ_FILE_BUFFER_SIZE ? fileLength - offset : READ_FILE_BUFFER_SIZE;
				bytesRead = in.read(buffer, offset, bytesToRead);

				offset += bytesRead;
			}

			return new String(buffer);
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
