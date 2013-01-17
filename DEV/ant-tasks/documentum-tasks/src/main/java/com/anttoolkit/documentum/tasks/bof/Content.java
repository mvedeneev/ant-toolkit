package com.anttoolkit.documentum.tasks.bof;

import com.anttoolkit.documentum.common.*;

import com.documentum.fc.common.*;

import org.apache.tools.ant.*;

public class Content
		extends DocAppObject
{
	public static Content instance(String objectId,
								   String name,
								   String objectType,
								   DocbaseSession session,
								   String localFile)
			throws BuildException
	{
		return new Content(objectId, name, objectType, session, localFile);
	}

	private Content(String objectId,
					String name,
					String type,
					DocbaseSession session,
					String localFile)
	{
		super(objectId, name, type, session);
		m_localFile = localFile;
	}

	public void checkOut()
			throws CheckOutException
	{
		if (m_isCheckedOut || m_localFile == null)
		{
			return;
		}

		try
		{
			if (!this.isCheckedOutByMe(this.getDfObject()))
			{
				this.getDfObject().checkout();
			}
		}
		catch (DfException e)
		{
			throw new CheckOutException(this.getName(), this.getType(), e);
		}

		m_isCheckedOut = true;
		m_wasUpdated = false;
	}

	public void cancelCheckOut()
	{
		if (!m_isCheckedOut)
		{
			return;
		}

		try
		{
			this.getDfObject().cancelCheckout();
		}
		catch (DfException e)
		{
			System.out.println("Failed to cancel checkout object of type=" + this.getType() +
					" with name=" + this.getName() + " and r_object_id=" + this.getObjectId() +
					"\r\n" + e.toString());
		}

		m_isCheckedOut = false;
	}

	public void update()
			throws UpdateException
	{
		if (m_wasUpdated || m_localFile == null)
		{
			m_isCheckedOut = false;
			return;
		}

		try
		{
			this.getDfObject().setFile(m_localFile);
			this.getDfObject().checkin(false, null);
		}
		catch (DfException e)
		{
			throw new UpdateException(this.getName(), this.getType(), e);
		}
		finally
		{
			m_wasUpdated = true;
		}

		m_isCheckedOut = false;
	}

	private boolean m_isCheckedOut = false;
	private boolean m_wasUpdated = false;

	private String m_localFile = null;
}
