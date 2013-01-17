package com.anttoolkit.documentum.tasks.dql;

import org.apache.tools.ant.*;

import com.anttoolkit.documentum.common.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

public class DqlSetPropertyTask
		extends GenericDocbaseTask
{
	public void addText(String text)
	{
		m_dqlStatement = getProject().replaceProperties(text);
	}

	public void setPropertyName(String name)
	{
		m_propName = name;
	}

	public void setFormat(String format)
	{
		m_format = format;
	}

	public void doWork()
			throws BuildException
	{
		if (m_dqlStatement == null)
		{
			throw new BuildException("DQL statement should be specified");
		}

		if (m_propName == null)
		{
			throw new BuildException("propName attribute should be specified");	
		}

		try
		{
			IDfTypedObject obj = DqlHelper.getFirstString(this.getSession(), m_dqlStatement);
			String attrName = obj.getAttr(0).getName();
			String value = DocbaseObjectsHelper.getAttributeValueAsString(obj, attrName, m_format);

			this.getProject().setProperty(m_propName, value);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to execute DQL query\r\n" + e.toString());
		}
	}

	private String m_dqlStatement = null;
	private String m_propName = null;
	private String m_format = IDfTime.DF_TIME_PATTERN18;
}
