package com.anttoolkit.documentum.common;

import java.text.*;
import java.util.*;

import org.apache.tools.ant.*;

import com.documentum.com.*;
import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

public class DocbaseSession
{
	private static final String DQL_GET_USER_NAME_BY_LOGIN = "select user_name " +
			"from dm_user where user_login_name = ''{0}''";

	private static final String NAME_ATTRIBUTE = "name";

	private static final String DQL_GET_CUSTOM_TYPES = "select name from dm_type " +
			"where name not like 'dm%' and super_name is NULLSTRING";

	DocbaseSession(LoginInfo loginInfo)
	{
		m_loginInfo = loginInfo;
	}

	public IDfSession getDfSession()
			throws BuildException
	{
		try
		{
			if (m_session != null)
			{
				return m_session;
			}

			return m_session = getSessionManager().getSession(m_loginInfo.getDocbase());
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to establish docbase session", e);
		}
	}

	public boolean isDfSessionOpened()
	{
		return m_session != null;
	}

	public String getUserLogin()
	{
		return m_loginInfo.getLogin();
	}

	public String getUserName()
	{
		if (m_userName != null)
		{
			return m_userName;
		}

		String query = MessageFormat.format(DQL_GET_USER_NAME_BY_LOGIN, new String[] {m_loginInfo.getLogin()});

		try
		{
			return m_userName = DqlHelper.getStringParamFromFirstString(getDfSession(), query);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get user name by login", e);
		}
	}

	public String getDocbaseName()
	{
		return m_loginInfo.getDocbase();
	}

	public IDfPersistentObject createDfObject(String type)
			throws BuildException
	{
		try
		{
			return this.getDfSession().newObject(type);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to create object object of type " + type +
					"\r\n" + e.toString());
		}
	}

	public IDfPersistentObject getDfObject(String objectId)
			throws BuildException
	{
		try
		{
			return this.getDfSession().getObject(new DfId(objectId));
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get object with r_object_id=" + objectId +
					" from docbase.\r\n" + e.toString());
		}
	}

	public IDfPersistentObject getDfObject(IDfId objectId)
			throws BuildException
	{
		try
		{
			return this.getDfSession().getObject(objectId);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get object with r_object_id=" + objectId +
					" from docbase.\r\n" + e.toString());
		}
	}

	public static String saveDfObject(IDfPersistentObject obj)
			throws BuildException
	{
		String objectId = null;

		try
		{
			objectId = obj.getObjectId().getId();
			obj.save();
			return obj.getObjectId().toString();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to save persistent object to docbase, r_object_id=" + objectId, e);
		}
	}

	public static void checkoutDfObject(IDfSysObject obj)
			throws BuildException
	{
		String objectId = null;

		try
		{
			objectId = obj.getObjectId().getId();
			obj.checkout();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to checkout object, r_object_id=" + objectId, e);
		}
	}

	public static void cancelCheckoutDfObject(IDfSysObject obj)
			throws BuildException
	{
		String objectId = null;

		try
		{
			objectId = obj.getObjectId().getId();
			obj.cancelCheckout();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to cancel checkout object, r_object_id=" + objectId, e);
		}
	}

	public static String checkinDfObject(IDfSysObject obj)
			throws BuildException
	{
		String objectId = null;

		try
		{
			objectId = obj.getObjectId().getId();
			return obj.checkin(false, null).getId();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to checkin object to docbase, r_object_id=" + objectId, e);
		}
	}

	public void startTransaction()
			throws BuildException
	{

		try
		{
			if (m_session != null && !m_session.isTransactionActive())
			{
				releaseSession();
			}

			getSessionManager().beginTransaction();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to start transaction\r\n" + e.toString());
		}
	}

	public void commitTransaction()
			throws BuildException
	{
		try
		{
			if (m_session == null || !m_session.isTransactionActive())
			{
				throw new BuildException("There are no transactions to commit");
			}

			getSessionManager().commitTransaction();
			releaseSession();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to commit transaction\r\n" + e.toString());
		}
	}

	public void abortTransaction()
			throws BuildException
	{
		try
		{
			if (m_session == null || !m_session.isTransactionActive())
			{
				throw new BuildException("There are no transactions to abort");
			}

			getSessionManager().abortTransaction();
			releaseSession();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to abort transaction", e);
		}
	}

	public boolean isTransactionActive()
			throws BuildException
	{
		try
		{
			return m_session != null && m_session.isTransactionActive();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to verify transaction status", e);
		}
	}

	public IDfService newService(String serviceName)
	{
		try
		{
			IDfClientX clientX = new DfClientX();
			return clientX.getLocalClient().newService(serviceName, getSessionManager());
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to instantiate service: " + serviceName, e);
		}
	}

	public void flushCaches()
	{
		IDfSession session = getDfSession();

		try
		{
			session.flush("querycache", null);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to flush user's query cache", e);
		}

		try
		{
			session.flush("aclcache", null);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to flush ACLs in the session cache", e);
		}

		try
		{
			session.flush("groupcache", null);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to flush group objects in the session cache", e);
		}

		flushDataDictionaryCache("dm_sysobject");
		flushDataDictionaryCache("dm_user");


		String[] customTypes = getCustomTypes();
		for (int i = 0; i < customTypes.length; i++)
		{
			flushDataDictionaryCache(customTypes[i]);
		}

		try
		{
			session.flush("persistentcache", null);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to flush persistent cache", e);
		}

		try
		{
			session.flush("persistentobjcache", null);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to flush registry objects in the " +
					"session’s registry cache\r\n" + e.toString());
		}

		try
		{
			session.flushCache(true);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to remove objects from the server and client caches", e);
		}
	}

	void releaseSession()
	{
		if (m_session == null)
		{
			return;
		}

		try
		{
			if (m_manager.isTransactionActive())
			{
				m_manager.abortTransaction();
			}
		}
		catch (DfException e)
		{
			System.out.println("Failed to abort opened transaction\r\n\r\n" + e.toString());
		}

		m_manager.release(m_session);
		m_manager.clearIdentities();

		m_session = null;
		m_manager = null;
	}

	protected void finalize()
		throws Throwable
	{
		releaseSession();
		super.finalize();
	}

	private IDfSessionManager getSessionManager()
	{
		if (m_manager != null)
		{
			return m_manager;
		}

		IDfClientX clientX = new DfClientX();
		IDfClient localClient;

		try
		{
			localClient = clientX.getLocalClient();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to instantiate local client", e);
		}

		IDfLoginInfo loginInfo = (clientX).getLoginInfo();
		loginInfo.setUser(m_loginInfo.getLogin());
		loginInfo.setPassword(m_loginInfo.getPassword());
		if (m_loginInfo.getDomain() != null &&
			m_loginInfo.getDomain().trim().length() != 0)
		{
			loginInfo.setDomain(m_loginInfo.getDomain());
		}

		m_manager = localClient.newSessionManager();

		try
		{
			m_manager.setIdentity(m_loginInfo.getDocbase(), loginInfo);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to set identity for session manager\r\n" + e.toString());
		}

		return m_manager;
	}

	private void flushDataDictionaryCache(String type)
			throws BuildException
	{
		try
		{
			m_session.flush("ddcache", type);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to flush data dictionary cache for type " + type +
					"\r\n" + e.toString());
		}
	}

	private String[] getCustomTypes()
	{
		IDfCollection coll = null;
		ArrayList types = new ArrayList();

		try
		{
			coll = DqlHelper.executeReadQuery(m_session, DQL_GET_CUSTOM_TYPES);
			while (coll.next())
			{
				types.add(coll.getString(NAME_ATTRIBUTE));
			}
		}
		catch (DfException e)
		{
			System.out.println("Failed to get custom types from docbase\r\n" + e.toString());
		}
		finally
		{
			DqlHelper.closeCollection(coll);
		}

		if (types.size() == 0)
		{
			return new String[]{};
		}

		return (String[])types.toArray(new String[]{});
	}

	private String m_userName = null;
	private LoginInfo m_loginInfo = null;
	private IDfSessionManager m_manager = null;
	private IDfSession m_session = null;
}

