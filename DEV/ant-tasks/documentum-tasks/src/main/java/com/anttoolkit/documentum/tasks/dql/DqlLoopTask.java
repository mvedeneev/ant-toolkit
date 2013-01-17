package com.anttoolkit.documentum.tasks.dql;

import java.util.*;
import org.apache.tools.ant.*;

import com.anttoolkit.documentum.common.*;
import com.anttoolkit.general.tasks.arrays.util.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

public class DqlLoopTask
		extends GenericDocbaseTask
		implements TaskContainer
{
	private String m_query = null;
	private String m_columnNamesArray = null;
	private List<Task> m_tasks = new LinkedList<Task>();
	private static ThreadLocal<Stack<IDfTypedObject>> m_currentRow = new ThreadLocal<Stack<IDfTypedObject>>()
	{
		protected Stack<IDfTypedObject> initialValue()
		{
			return new Stack<IDfTypedObject>();
		}
	};

	static String getCurrentRowColumn(String columnName, String format)
	{
		if (m_currentRow.get() == null || m_currentRow.get().isEmpty())
		{
			throw new BuildException("There are no dql query executed to return any column values");
		}

		return DocbaseObjectsHelper.getAttributeValueAsString(m_currentRow.get().peek(), columnName, format);
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

		IDfCollection coll = null;
		try
		{
			coll = DqlHelper.executeReadQuery(getSession(), m_query);
			if (coll == null)
			{
				return;
			}

			initColumnsArray(coll);

			while (coll.next())
			{
				m_currentRow.get().push(coll.getTypedObject());

				for (Task task : m_tasks)
				{
					task.perform();
				}

				m_currentRow.get().pop();
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Exception occured during dql loop\r\n" + e.toString());
		}
		finally
		{
			DqlHelper.closeCollection(coll);
		}
	}

	private void initColumnsArray(IDfCollection coll)
	{
		if (m_columnNamesArray == null)
		{
			return;
		}

		try
		{
			int count = coll.getAttrCount();
			for (int i = 0; i < count; i++)
			{
				IDfAttr attr = coll.getAttr(i);
				ArrayManager.addArrayElement(m_columnNamesArray, attr.getName(), true);
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get query columns info");
		}
	}
}
