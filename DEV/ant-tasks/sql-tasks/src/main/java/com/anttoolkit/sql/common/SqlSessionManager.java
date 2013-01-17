package com.anttoolkit.sql.common;

import java.util.*;

import org.apache.tools.ant.*;

public class SqlSessionManager
{
	private static ThreadLocal<Map<String, SqlSession>> m_sessions = new ThreadLocal<Map<String, SqlSession>>()
	{
		protected Map<String, SqlSession> initialValue()
		{
			return new HashMap<String, SqlSession>();
		}
	};

	private static ThreadLocal<Stack<String>> m_loginInfoStack = new ThreadLocal<Stack<String>>()
	{
		protected Stack<String> initialValue()
		{
			return new Stack<String>();
		}
	};

	public static SqlSession getSession()
	{
		try
		{
			return m_sessions.get().get(m_loginInfoStack.get().peek());
		}
		catch (EmptyStackException e)
		{
			throw new BuildException("Failed to get session - login info wasn't specified");
		}
	}

	public static void setCurrentSessionContext(LoginInfo loginInfo)
	{
		if (!m_sessions.get().containsKey(loginInfo.toString()))
		{
			m_sessions.get().put(loginInfo.toString(), new SqlSession(loginInfo));
		}

		m_loginInfoStack.get().push(loginInfo.toString());
	}

	public static void releaseCurrentSessionContext()
	{
		String loginInfo = m_loginInfoStack.get().pop();

		if (!m_loginInfoStack.get().contains(loginInfo))
		{
			SqlSession session = m_sessions.get().remove(loginInfo);
			session.closeConnection();
		}
	}
}
