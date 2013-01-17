package com.anttoolkit.documentum.tasks.bof;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

import org.apache.tools.ant.*;

import com.anttoolkit.documentum.common.*;

public abstract class DocAppObject
{
	public static final String OBJECT_ID_ATTRIBUTE = "r_object_id";
	public static final String NAME_ATTRIBUTE = "object_name";
	public static final String TYPE_ATTRIBUTE = "r_object_type";

	public DocAppObject(String objectId,
						String name,
						String type,
						DocbaseSession session)
	{
		m_objectId = objectId;
		m_name = name;
		m_type = type;
		m_session = session;
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof DocAppObject)
		{
			return ((DocAppObject)obj).getObjectId().equals(m_objectId);
		}

		return false;
	}

	public String getObjectId()
	{
		return m_objectId;
	}

	public String getName()
	{
		return m_name;
	}

	public String getType()
	{
		return m_type;
	}

	public abstract void checkOut() throws CheckOutException;

	public abstract void cancelCheckOut();

	public abstract void update() throws UpdateException;

	protected IDfSysObject getDfObject()
			throws BuildException
	{
		if (m_sysObj != null)
		{
			return m_sysObj;
		}

		try
		{
			return m_sysObj = (IDfSysObject)m_session.getDfObject(m_objectId);
		}
		catch (BuildException e)
		{
			throw new BuildException("Failed to get object of type=" + m_type +
					" with object_name=" + m_name + " and r_object_id=" + m_objectId +
					" from docbase\r\n" + e.toString());
		}
	}

	protected DocbaseSession getSession()
	{
		return m_session;
	}

	protected boolean isCheckedOutByMe(IDfSysObject obj)
			throws DfException
	{
		return obj.isCheckedOutBy(this.getSession().getUserName());
	}

	private String m_objectId = null;
	private String m_name = null;
	private String m_type = null;

	private DocbaseSession m_session = null;
	private IDfSysObject m_sysObj = null;
}
