package com.anttoolkit.documentum.tasks;

import java.util.*;

import org.apache.tools.ant.*;

import com.anttoolkit.documentum.common.*;
import com.anttoolkit.documentum.tasks.build.util.*;

public class DocbaseTaskContainer
		extends GenericDocbaseTask
		implements TaskContainer
{
	private String m_login = null;
	private String m_domain = null;
	private String m_password = null;
	private String m_docbase = null;
	private String m_buildQualifier = null;

	private LoginInfo m_loginInfo = null;

	private List<Task> m_tasks = new LinkedList<Task>();

	public void setLogin(String login)
	{
		m_login = login;
	}

	public void setDomain(String domain)
	{
		m_domain = domain;
	}

	public void setPassword(String password)
	{
		m_password = password;
	}

	public void setDocbase(String docbase)
	{
		m_docbase = docbase;
	}

	public void setBuildQualifier(String buildQualifier)
	{
		m_buildQualifier = buildQualifier;
	}

	public void addTask(Task task)
	{
		if (task == null)
		{
			return;
		}

		m_tasks.add(task);
	}

	public void doWork()
			throws BuildException
	{
		if (m_buildQualifier != null && m_buildQualifier.trim().length() != 0)
		{
			BuildVersionManager.setContextQualifier(m_buildQualifier);
		}

		DocbaseSessionManager.setCurrentSessionContext(getLoginInfo());

		try
		{
			for (Task task : m_tasks)
			{
				task.perform();
			}
		}
		catch (Throwable e)
		{
			if (DocbaseSessionManager.getSession().isTransactionActive())
			{
				DocbaseSessionManager.getSession().abortTransaction();
			}

			if (e instanceof BuildException)
			{
				throw (BuildException)e;
			}

			throw new BuildException("Exception occured", e);
		}
		finally
		{
			DocbaseSessionManager.releaseCurrentSessionContext();

			if (m_buildQualifier != null && m_buildQualifier.trim().length() != 0)
			{
				BuildVersionManager.releaseContextQualifier();
			}
		}
	}

	protected void setLoginInfo(LoginInfo loginInfo)
	{
		m_loginInfo = loginInfo;
		m_login = loginInfo.getLogin();
		m_domain = loginInfo.getDomain();
		m_password = loginInfo.getPassword();
		m_docbase = loginInfo.getDocbase();
	}

	protected LoginInfo getLoginInfo()
	{
		if (m_loginInfo != null)
		{
			return m_loginInfo;
		}

		return m_loginInfo = new LoginInfo(m_login, m_domain, m_password, m_docbase);
	}
}
