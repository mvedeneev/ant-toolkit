package com.anttoolkit.documentum.tasks.bof;

import org.apache.tools.ant.*;

import com.documentum.fc.common.*;

import java.text.*;

public class UpdateException
		extends BuildException
{
	private static final String GENERIC_ERROR_MESSAGE = "Failed to update {0} \"{1}\"\r\n{2}";
	private static final String ENHANCED_ERROR_MESSAGE = "Failed to update {0} \"{1}\" - child object of {2} \"{3}\"\r\n{4}";

	public UpdateException(String sourceObjectName, String sourceObjectType, DfException e)
	{
		m_sourceObjectName = sourceObjectName;
		m_sourceObjectType = sourceObjectType;
		m_exceptionDescription = e.toString();
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
		if (m_parentName != null &&
			m_parentType != null)
		{
			return MessageFormat.format(ENHANCED_ERROR_MESSAGE,
					new String[] {m_sourceObjectType, m_sourceObjectName, m_parentType, m_parentName, m_exceptionDescription});
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

	private String m_sourceObjectName = null;
	private String m_sourceObjectType = null;
	private String m_parentName = null;
	private String m_parentType = null;
}
