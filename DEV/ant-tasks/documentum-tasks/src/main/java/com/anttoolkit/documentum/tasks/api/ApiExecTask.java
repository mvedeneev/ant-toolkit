package com.anttoolkit.documentum.tasks.api;

import com.anttoolkit.documentum.common.*;

import org.apache.tools.ant.*;

import com.documentum.fc.common.*;

import java.io.*;

public class ApiExecTask
		extends GenericDocbaseTask
{
	private String m_apiStatement = null;
	private String m_file = null;
	private String m_encoding = null;

	public void addText(String text)
	{
		m_apiStatement = getProject().replaceProperties(text);
	}

	public void setCommand(String command)
	{
		m_apiStatement = command;
	}

	public void setFile(String file)
	{
		m_file = file;
	}

	public void setEncoding(String encoding)
	{
		m_encoding = encoding;
	}

	public void doWork()
			throws BuildException
	{
		if (m_apiStatement == null && m_file == null)
		{
			throw new BuildException("Either API statement or batch file should be specified");
		}

		if (m_apiStatement != null)
		{
			executeSingleStatement(m_apiStatement);
		}

		if (m_file != null)
		{
			executeStatementsFromFile();
		}
	}

	private void executeSingleStatement(String statement)
			throws BuildException
	{
		if (statement.trim().length() == 0)
		{
			return;
		}

		int index = statement.indexOf(",");
		if (index == -1)
		{
			throw new BuildException("Incorrect API statement");
		}

		log("Executing IAPI: " + statement);

		String command = statement.substring(0, index);
		String args = statement.substring(index + 1);
		index = args.indexOf(",");
		args = index == - 1 ? "" : args.substring(index + 1);

		try
		{
			//noinspection deprecation
			this.getSession().getDfSession().apiExec(command, args);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to execute api function " +
					command + "\r\n" + e.toString());
		}
	}

	private void executeStatementsFromFile()
			throws BuildException
	{
		String fullPath = getFileFullPath(m_file);
		if (fullPath == null)
		{
			throw new BuildException("File should be specified");
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
				executeSingleStatement(line);
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
