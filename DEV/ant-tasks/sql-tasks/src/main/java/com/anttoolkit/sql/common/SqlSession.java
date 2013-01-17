package com.anttoolkit.sql.common;

import org.apache.tools.ant.*;

import java.sql.*;

public class SqlSession
{
	private LoginInfo m_loginInfo;
	private Connection m_connection;
	private boolean m_hasActiveTransaction = false;

	SqlSession(LoginInfo loginInfo)
	{
		m_loginInfo = loginInfo;
	}

	public Connection getConnection()
	{
		if (m_connection != null)
		{
			return m_connection;
		}

		try
		{
			m_connection = m_loginInfo.driver.connect(m_loginInfo.url, m_loginInfo.connectionProperties);
			m_connection.setAutoCommit(false);
			return m_connection;
		}
		catch (SQLException e)
		{
			throw new BuildException("Failed to connect to URL: " + m_loginInfo.url, e);
		}
	}

	public boolean hasActiveTransaction()
	{
		return m_hasActiveTransaction;
	}

	public void beginTransaction(int isolationLevel)
	{
		if (m_hasActiveTransaction)
		{
			throw new BuildException("Nested transactions are not supported");
		}

		try
		{
			getConnection().setTransactionIsolation(isolationLevel);
			m_hasActiveTransaction = true;
		}
		catch (SQLException e)
		{
			throw new BuildException("Failed to set transaction isolation level to " + isolationLevel, e);
		}
	}

	public void commitTransaction()
	{
		if (m_connection == null || !m_hasActiveTransaction)
		{
			return;
		}

		try
		{
			m_connection.commit();
			m_hasActiveTransaction = false;
		}
		catch (SQLException e)
		{
			throw new BuildException("Failed to commit transaction", e);
		}
	}

	public void rollbackTransaction()
	{
		if (m_connection == null || !m_hasActiveTransaction)
		{
			return;
		}

		try
		{
			m_connection.rollback();
			m_hasActiveTransaction = false;
		}
		catch (SQLException e)
		{
			throw new BuildException("Failed to rollback transaction", e);
		}
	}

	public void closeConnection()
	{
		try
		{
			if (m_connection != null && !m_connection.isClosed())
			{
				m_connection.close();
			}

			m_connection = null;
		}
		catch (SQLException e) {}
	}
}
