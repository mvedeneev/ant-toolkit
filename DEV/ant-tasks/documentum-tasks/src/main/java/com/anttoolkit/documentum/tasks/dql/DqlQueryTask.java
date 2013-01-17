package com.anttoolkit.documentum.tasks.dql;

import com.anttoolkit.documentum.common.*;

import com.documentum.fc.common.*;

import org.apache.tools.ant.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import java.io.*;

public class DqlQueryTask
		extends GenericDocbaseTask
{
	private static final String IDQL_QUERY_DELIMITER = "go";

	private String m_dqlStatement = null;
	private String m_file = null;
	private String m_encoding = null;

	public void setFile(String file)
	{
		m_file = file;
	}

	public void setEncoding(String encoding)
	{
		m_encoding = encoding;
	}

	public void setQuery(String query)
	{
		m_dqlStatement = query;
	}

	public void addText(String text)
	{
		m_dqlStatement = getProject().replaceProperties(text);
	}

	public void doWork()
			throws BuildException
	{
		if (m_dqlStatement == null && m_file == null)
		{
			throw new BuildException("DQL statement or file should be specified");
		}

		//execute single DQL query
		if (m_dqlStatement != null)
		{
			executeSingleQuery(m_dqlStatement);
		}

		//execute multiple DQL queries from xml file
		if (m_file != null)
		{
			executeQueriesFromFile();
		}
	}

	private void executeSingleQuery(String statement)
			throws BuildException
	{
		if (statement.trim().length() == 0)
		{
			return;
		}

		log("Executing DQL query: " + statement);

		try
		{
			DqlHelper.executeQuery(this.getSession(), statement);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to execute DQL query\r\n" + e.toString());
		}
	}

	private void executeQueriesFromFile()
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

			StringBuffer buffer = new StringBuffer();
			String line;

			while ((line = reader.readLine()) != null)
			{
				if (IDQL_QUERY_DELIMITER.equals(line.trim().toLowerCase()) && buffer.length() > 0)
				{
					executeSingleQuery(buffer.toString());
					buffer = new StringBuffer();
					continue;
				}

				buffer.append(" ").append(line);
    		}

			if (buffer.length() > 0)
			{
				executeSingleQuery(buffer.toString());
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
