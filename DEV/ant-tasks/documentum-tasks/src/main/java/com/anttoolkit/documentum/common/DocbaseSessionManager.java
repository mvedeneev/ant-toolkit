package com.anttoolkit.documentum.common;

import org.apache.tools.ant.*;

import java.util.*;

public class DocbaseSessionManager
{
	private static ThreadLocal<Map<String, DocbaseSession>> m_sessions = new ThreadLocal<Map<String, DocbaseSession>>()
	{
		protected Map<String, DocbaseSession> initialValue()
		{
			return new HashMap<String, DocbaseSession>();
		}
	};

	private static ThreadLocal<Stack<String>> m_loginInfoStack = new ThreadLocal<Stack<String>>()
	{
		protected Stack<String> initialValue()
		{
			return new Stack<String>();
		}
	};

	public static DocbaseSession getSession()
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
			m_sessions.get().put(loginInfo.toString(), new DocbaseSession(loginInfo));
		}

		m_loginInfoStack.get().push(loginInfo.toString());
	}

	public static void releaseCurrentSessionContext()
	{
		String loginInfo = m_loginInfoStack.get().pop();

		if (!m_loginInfoStack.get().contains(loginInfo))
		{
			DocbaseSession session = m_sessions.get().remove(loginInfo);
			session.releaseSession();
		}
	}

	public static LoginInfo getCurrentSessionContext()
	{
		String loginString = m_loginInfoStack.get().peek();
		return loginString == null ? null : new LoginInfo(loginString);
	}
}
