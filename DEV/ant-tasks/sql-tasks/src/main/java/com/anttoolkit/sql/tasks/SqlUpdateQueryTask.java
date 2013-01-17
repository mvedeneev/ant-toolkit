package com.anttoolkit.sql.tasks;

import java.io.*;
import java.util.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.types.resources.*;
import org.apache.tools.ant.util.*;

import com.anttoolkit.common.*;
import com.anttoolkit.sql.common.*;

public class SqlUpdateQueryTask
		extends GenericTask
{
	public static class DelimiterType extends EnumeratedAttribute
	{
		public static final String NORMAL = "normal";
		public static final String ROW = "row";

		public String[] getValues()
		{
			return new String[] {NORMAL, ROW};
		}
	}

	private String m_delimiterType = DelimiterType.NORMAL;
	private String m_delimiter = ";";
	private String m_sqlCommand = "";
	private File m_file = null;
	private Union m_resources;
	private String m_encoding;
	private boolean m_keepformat = false;


	public void setSrc(File file)
	{
    	m_file = file;
	}

	public void addText(String sql)
	{
		m_sqlCommand += sql;
	}

	public void addFileset(FileSet set)
	{
		add(set);
	}

	public void add(ResourceCollection rc)
	{
		if (rc == null)
		{
			throw new BuildException("Cannot add null ResourceCollection");
		}

		synchronized (this)
		{
			if (m_resources == null)
			{
				m_resources = new Union();
			}
		}

		m_resources.add(rc);
	}

	public void setEncoding(String encoding)
	{
		m_encoding = encoding;
	}

	public void setDelimiter(String delimiter)
	{
		m_delimiter = delimiter;
	}

	public void setDelimiterType(DelimiterType delimiterType)
	{
		m_delimiterType = delimiterType.getValue();
	}

	public void setKeepformat(boolean keepformat)
	{
		m_keepformat = keepformat;
	}

	public void doWork() throws BuildException
	{
		validate();

		if (m_sqlCommand.length() != 0)
		{
			Reader reader = new StringReader(m_sqlCommand);

			try
			{
				SqlHelper.executeUpdateStatements(SqlSessionManager.getSession(), reader,
						this.getProject(), m_keepformat, m_delimiter, m_delimiterType);
			}
			finally
			{
				FileUtils.close(reader);
			}
		}

		if (m_file != null)
		{
			InputStream in = null;
			Reader reader = null;

			try
			{
				in = new FileResource(m_file).getInputStream();
				reader = m_encoding == null ? new InputStreamReader(in) : new InputStreamReader(in, m_encoding);

				SqlHelper.executeUpdateStatements(SqlSessionManager.getSession(), reader,
						this.getProject(), m_keepformat, m_delimiter, m_delimiterType);
			}
			catch (IOException e)
			{
				throw new BuildException("Failed to read from file: " + m_file.getName(), e);
			}
			finally
			{
				FileUtils.close(in);
			    FileUtils.close(reader);
			}
		}

		if (m_resources != null)
		{
			InputStream in = null;
			Reader reader = null;

			Iterator iter = m_resources.iterator();
			while (iter.hasNext())
			{
				Resource resource = (Resource) iter.next();

				try
				{
					in = resource.getInputStream();
					reader = m_encoding == null ? new InputStreamReader(in) : new InputStreamReader(in, m_encoding);

					SqlHelper.executeUpdateStatements(SqlSessionManager.getSession(), reader,
							this.getProject(), m_keepformat, m_delimiter, m_delimiterType);
				}
				catch (IOException e)
				{
					throw new BuildException("Failed to read from resource: " + resource.getName(), e);
				}
				finally
				{
					FileUtils.close(in);
					FileUtils.close(reader);
				}
			}
		}


		if (!SqlSessionManager.getSession().hasActiveTransaction())
		{
			SqlSessionManager.getSession().commitTransaction();
		}
	}

	private void validate()
	{
		m_sqlCommand = m_sqlCommand.trim();

		if (m_file == null && m_sqlCommand.length() == 0 && m_resources == null)
		{
          throw new BuildException("File or resource collection or sql statement must be specified", getLocation());
		}

		if (m_file != null && !m_file.isFile())
		{
			throw new BuildException("Specified file " + m_file + " is not a file!", getLocation());
		}
	}
}
