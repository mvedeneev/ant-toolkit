package com.anttoolkit.documentum.tasks.bof;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import com.anttoolkit.documentum.common.*;

import org.apache.tools.ant.*;

import java.text.*;
import java.util.*;

public abstract class BasicDocAppObject
		extends DocAppObject
{
	private static final String DQL_GET_APP_REF_OBJECT = "select r_object_id " +
			"from dm_app_ref where application_obj_id = ''{0}''";

	private static final String DQL_REMOVE_PREVIOUS_VERSIONS = "delete dm_sysobject (ALL) object " +
			"where r_object_id in (select r_object_id from dm_sysobject (ALL) " +
			"where folder(ID(''{0}'')) and r_object_type in (''dm_document'', ''dmc_jar'') " +
			"and not (any r_version_label=''CURRENT''))";

	public BasicDocAppObject(String objectId,
							 String name,
							 String type,
							 DocbaseSession session,
							 IContentStorage storage)
	{
		super(objectId, name, type, session);
		m_storage = storage;
	}

	public void checkOut()
			throws CheckOutException
	{
		chechOutAppRef();
		loadContent();
		checkOutContent();
	}

	public void cancelCheckOut()
	{
		cancelAppRefCheckOut();
		cancelContentCheckOut();
	}

	public void update()
			throws UpdateException
	{
		updateContent();
		updateAppRef();
	}

	public void removePreviousVersions()
			throws BuildException
	{
		String query = MessageFormat.format(DQL_REMOVE_PREVIOUS_VERSIONS, new String[] {this.getObjectId()});

		try
		{
			DqlHelper.executeQuery(this.getSession(), query);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to remove previous versions of BOF objects for " +
					this.getReadableTypeName() + " \"" + this.getName() + "\"\r\n" + e.toString());
		}
	}

	protected abstract String getReadableTypeName();

	protected abstract String contentQueryTemplate();

	protected Iterator contentIterator()
	{
		return m_content.iterator();
	}

	private void loadContent()
			throws BuildException
	{
		if (m_isContentLoaded)
		{
			return;
		}

		IDfCollection coll = null;
		try
		{
			String query = MessageFormat.format(contentQueryTemplate(), new String[] {this.getObjectId()});
			coll = DqlHelper.executeReadQuery(this.getSession(), query);

			while (coll.next())
			{
				this.putContent(coll.getString(OBJECT_ID_ATTRIBUTE),
								coll.getString(NAME_ATTRIBUTE),
								coll.getString(TYPE_ATTRIBUTE));
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to load content for " + this.getReadableTypeName() +
					" " + this.getName() + "\r\n" + e.toString());
		}
		finally
		{
			DqlHelper.closeCollection(coll);
		}

		m_isContentLoaded = true;
	}

	private void putContent(String objectId,
							  String name,
							  String type)
	{
		m_content.add(m_storage.putContent(objectId, name, type));
	}

	private void chechOutAppRef()
			throws CheckOutException
	{
		if (m_isAppRefCheckedOut)
		{
			return;
		}

		try
		{
			if (this.getAppRef() != null && !this.isCheckedOutByMe(this.getAppRef()))
			{
				this.getAppRef().checkout();
			}
		}
		catch (DfException e)
		{
			throw new CheckOutException(this.getName(), this.getReadableTypeName(), e);
		}

		m_isAppRefCheckedOut = true;
		m_wasAppRefUpdated = false;
	}

	private void checkOutContent()
			throws CheckOutException
	{
		if (m_isContentCheckedOut)
		{
			return;
		}

		int count = m_content.size();
		for (int i = 0; i < count; i++)
		{
			Content obj = (Content)m_content.get(i);

			try
			{
				obj.checkOut();
			}
			catch (CheckOutException e)
			{
				e.setParentObjectInfo(this.getName(), this.getReadableTypeName());
				throw e;
			}
		}

		m_isContentCheckedOut = true;
		m_wasContentUpdated = false;
	}

	private void cancelAppRefCheckOut()
	{
		if (!m_isAppRefCheckedOut)
		{
			return;
		}

		try
		{
			if (this.getAppRef() != null)
			{
				this.getAppRef().cancelCheckout();
			}
		}
		catch (DfException e)
		{
			System.out.println("Failed to cancel checkout object of type=" + this.getType() +
					" with name=" + this.getName() + " and r_object_id=" + this.getObjectId() +
					"\r\n" + e.toString());
		}

		m_isAppRefCheckedOut = false;
	}

	private void cancelContentCheckOut()
	{
		if (!m_isContentCheckedOut)
		{
			return;
		}

		int count = m_content.size();
		for (int i = 0; i < count; i++)
		{
			Content obj = (Content)m_content.get(i);
			obj.cancelCheckOut();
		}

		m_isContentCheckedOut = false;
	}

	private void updateAppRef()
			throws UpdateException
	{
		if (m_wasAppRefUpdated)
		{
			m_isAppRefCheckedOut = false;
			return;
		}

		try
		{
			if (this.getAppRef() != null)
			{
				this.getAppRef().save();
			}
		}
		catch (DfException e)
		{
			throw new UpdateException(this.getName(), this.getReadableTypeName(), e);
		}

		m_wasAppRefUpdated = true;
		m_isAppRefCheckedOut = false;
	}

	private void updateContent()
			throws UpdateException
	{
		if (m_wasContentUpdated)
		{
			m_isContentCheckedOut = false;
			return;
		}

		int count = m_content.size();
		for (int i = 0; i < count; i++)
		{
			Content obj = (Content)m_content.get(i);

			try
			{
				obj.update();
			}
			catch (UpdateException e)
			{
				e.setParentObjectInfo(this.getName(), this.getReadableTypeName());
				throw e;
			}
		}

		m_wasContentUpdated = true;
		m_isContentCheckedOut = false;
	}

	private IDfSysObject getAppRef()
			throws DfException
	{
		if (m_triedToLoadAppRef)
		{
			return m_appRef;
		}

		try
		{
			String query = MessageFormat.format(DQL_GET_APP_REF_OBJECT, new String[] {this.getObjectId()});
			String appRefId = DqlHelper.getStringParamFromFirstString(this.getSession(), query);
			m_appRef = (IDfSysObject)this.getSession().getDfObject(appRefId);
		}
		catch (DfEndOfCollectionException e)
		{
		}
		finally
		{
			m_triedToLoadAppRef = true;
		}

		return m_appRef;
	}

	private boolean m_isAppRefCheckedOut = false;
	private boolean m_isContentCheckedOut = false;
	private boolean m_wasAppRefUpdated = false;
	private boolean m_wasContentUpdated = false;

	private IDfSysObject m_appRef = null;
	private boolean m_triedToLoadAppRef = false;

	private boolean m_isContentLoaded = false;
	private List m_content = new Vector();
	private IContentStorage m_storage = null;
}
