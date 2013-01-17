package com.anttoolkit.sql.tasks;

import java.sql.*;
import java.util.*;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;
import com.anttoolkit.sql.common.*;
import com.anttoolkit.general.tasks.arrays.util.*;

public class SqlLoopTask
		extends GenericTask
		implements TaskContainer
{
	private String m_query = null;
	private String m_columnNamesArray = null;
	private List<Task> m_tasks = new LinkedList<Task>();
	private static ThreadLocal<Stack<ResultSet>> m_currentResultSet = new ThreadLocal<Stack<ResultSet>>()
	{
		protected Stack<ResultSet> initialValue()
		{
			return new Stack<ResultSet>();
		}
	};

	static String getCurrentRowColumn(String columnName, String format)
	{
		if (m_currentResultSet.get() == null || m_currentResultSet.get().isEmpty())
		{
			throw new BuildException("There are no SQL query executed to return any column values");
		}

		ResultSet resultSet = m_currentResultSet.get().peek();

		return SqlHelper.getColumnValue(resultSet, columnName, format);
	}

	public void setQuery(String query)
	{
		m_query = query.trim();
	}

	public void setColumnNamesArray(String array)
	{
		m_columnNamesArray = array;
	}

	public void addTask(Task task)
	{
		m_tasks.add(task);
	}

	public void doWork()
			throws BuildException
	{
		if (m_query == null)
		{
			throw new BuildException("Query should be specified");
		}

		String _query = m_query.toUpperCase();
		if (_query.indexOf("SELECT") != 0)
		{
			throw new BuildException("Invalid query type. Only SELECT like queries could be used.");
		}

		if (m_tasks.size() == 0)
		{
			return;
		}

		ResultSet resultSet = null;
		try
		{
			resultSet = SqlHelper.executeQuery(SqlSessionManager.getSession(), m_query);
			if (resultSet == null)
			{
				return;
			}

			initColumnsArray(resultSet);

			m_currentResultSet.get().push(resultSet);

			while (resultSet.next())
			{
				for (Task task : m_tasks)
				{
					task.perform();
				}
			}

			m_currentResultSet.get().pop();
		}
		catch (SQLException e)
		{
			throw new BuildException("Exception occured while trying to iterate through result set", e);
		}
		finally
		{
			SqlHelper.closeResultSet(resultSet);
		}
	}

	private void initColumnsArray(ResultSet resultSet)
	{
		if (m_columnNamesArray == null)
		{
			return;
		}

		List<String> names = SqlHelper.getColumnNames(resultSet);
		ArrayManager.initArray(m_columnNamesArray, names);
	}
}
