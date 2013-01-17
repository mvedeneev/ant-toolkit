package com.anttoolkit.documentum.tasks.build.util;

import java.net.*;
import java.util.*;

public class BuildVersionManager
{
	private static String m_hostIp;
	private static String m_hostName;
	private static String m_currentUser;

	private static ThreadLocal<Stack<String>> m_qualifierStack = new ThreadLocal<Stack<String>>()
	{
		protected Stack<String> initialValue()
		{
			return new Stack<String>();
		}
	};

	static
	{
		try
		{
			InetAddress address = InetAddress.getLocalHost();
			m_hostName = address.getHostName();
			m_hostIp = address.getHostAddress();
		}
		catch (UnknownHostException e)
		{
			m_hostName = "undefined";
			m_hostIp = "undefined";
		}

		m_currentUser = System.getProperty("user.name");
	}

	public static String getHostName()
	{
		return m_hostName;
	}

	public static String getHostIp()
	{
		return m_hostIp;
	}

	public static String getCurrentOsUser()
	{
		return m_currentUser;
	}

	public static void setContextQualifier(String qualifier)
	{
		m_qualifierStack.get().push(qualifier);
	}

	public static String getContextQualifier()
	{
		try
		{
			return m_qualifierStack.get().peek();
		}
		catch (EmptyStackException e)
		{
			return "";
		}
	}

	public static void releaseContextQualifier()
	{
		try
		{
			m_qualifierStack.get().pop();
		}
		catch (EmptyStackException e) {}
	}

}
