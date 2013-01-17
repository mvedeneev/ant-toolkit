package com.anttoolkit.documentum.tasks.bof;

import org.apache.tools.ant.*;

import com.documentum.fc.common.*;

import java.text.*;

public class CheckOutException
	extends BuildException
{
	private static final String LOCKED_BY_KEYWORD = "locked by";
	private static final String PESSIMISTIC_LOCK_ERROR = "[DM_SYSOBJECT_E_CANT_LOCK]";
	private static final String GENERIC_ERROR_MESSAGE = "Failed to check out {0} \"{1}\"\r\n{2}";
	private static final String PESSIMISTIC_LOCK_SIMPLE_MESSAGE = "{0} \"{1}\" is already locked by {2}";
	private static final String PESSIMISTIC_LOCK_ADVANCED_MESSAGE = "Child object {0} \"{1}\" of {2} \"{3}\" is already locked by {4}";

	public CheckOutException(String sourceObjectName, String sourceObjectType, DfException e)
	{
		m_sourceObjectName = sourceObjectName;
		m_sourceObjectType = sourceObjectType;
		m_exceptionDescription = e.toString();

		if (m_exceptionDescription.indexOf(PESSIMISTIC_LOCK_ERROR) == -1)
		{
			return;
		}

		int index = m_exceptionDescription.indexOf(LOCKED_BY_KEYWORD);
		if (index == -1)
		{
			return;
		}

		String temp = m_exceptionDescription.substring(index + LOCKED_BY_KEYWORD.length());
		index = temp.indexOf(".\"");
		if (index == -1)
		{
			return;
		}

		m_lockedBy = temp.substring(0, index).trim();
	}

	public String getMessage()
	{
		return toString();
	}

	public String getLocalizedMessage()
	{
		return toString();
	}

	public String toString()
	{
		if (m_lockedBy != null &&
			m_parentName != null &&
			m_parentType != null)
		{
			return MessageFormat.format(PESSIMISTIC_LOCK_ADVANCED_MESSAGE,
					new String[] {m_sourceObjectType, m_sourceObjectName, m_parentType, m_parentName, m_lockedBy});
		}

		if (m_lockedBy != null)
		{
			return MessageFormat.format(PESSIMISTIC_LOCK_SIMPLE_MESSAGE,
					new String[] {m_sourceObjectType, m_sourceObjectName, m_lockedBy});
		}

		return MessageFormat.format(GENERIC_ERROR_MESSAGE,
				new String[] {m_sourceObjectType, m_sourceObjectName, m_exceptionDescription});
	}

	public void setParentObjectInfo(String name, String readableType)
	{
		m_parentName = name;
		m_parentType = readableType;
	}

	private String m_exceptionDescription = null;

	private String m_lockedBy = null;
	private String m_sourceObjectName = null;
	private String m_sourceObjectType = null;
	private String m_parentName = null;
	private String m_parentType = null;
}
