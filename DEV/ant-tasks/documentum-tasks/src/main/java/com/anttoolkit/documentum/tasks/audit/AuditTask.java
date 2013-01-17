package com.anttoolkit.documentum.tasks.audit;

import com.anttoolkit.common.*;

import java.util.*;

import org.apache.tools.ant.*;

import com.documentum.fc.common.*;

public class AuditTask
		extends GenericAuditTask
{
	public class Attribute
	{
		public void setName(String name)
		{
			m_name = name;
		}

		public String getName()
		{
			return m_name;
		}

		private String m_name = null;
	}

	public Attribute createAttribute()
	{
		Attribute attribute = new Attribute();
		m_attributes.add(attribute);
		return attribute;
	}

	public void setIncludeSubtypes(String include)
	{
		m_includeSubtypes = BooleanHelper.getBoolean(include);
	}

	public void setSignAudit(String signFlag)
	{
		m_signAudit = BooleanHelper.getBoolean(signFlag);
	}

	public void setAuthentication(int authentication)
	{
		m_authentication = authentication;
	}

	public void setEventDescription(String description)
	{
		m_eventDescription = description;
	}

	public void doWork()
			throws BuildException
	{
		super.doWork();

		IDfList attributes = getAttributesList();
		String policyId = this.getPolicyId();
		String[] types = this.getTypeNames();
		String[] events = this.getEventNames();

		for (int i = 0; i < types.length; i++)
		{
			String type = types[i].trim();

			for (int j = 0; j < events.length; j++)
			{
				String event = events[j].trim();
				this.registerEventForType(type, event, m_includeSubtypes, this.getApplication(),
						policyId, this.getStateName(), m_signAudit, m_authentication,
						m_eventDescription, attributes);
			}
		}
	}

	private IDfList getAttributesList()
	{
		int count = m_attributes.size();
		if (count == 0)
		{
			return null;
		}

		IDfList attributes = new DfList();
		for (int i = 0; i < count; i++)
		{
			try
			{
				Attribute attr = (Attribute)m_attributes.get(i);
				if (attributes.findStringIndex(attr.getName()) == -1)
				{
					attributes.appendString(attr.getName());
				}
			}
			catch (DfException e)
			{
				throw new BuildException(e.toString());
			}
		}

		return attributes;
	}


	private boolean m_includeSubtypes = false;
	private boolean m_signAudit = false;
	private int m_authentication = 0;
	private String m_eventDescription = null;

	private List m_attributes = new Vector();
}
