package com.anttoolkit.documentum.tasks.audit;

import com.anttoolkit.documentum.common.*;

import org.apache.tools.ant.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

import java.text.*;

public abstract class GenericAuditTask
		extends GenericDocbaseTask
{
	private static final String DQL_GET_POLICY_ID_BY_NAME = "select r_object_id from dm_policy " +
			"where object_name=''{0}''";

	public void setTypeName(String name)
	{
		m_typeName = name;
	}

	public String[] getTypeNames()
	{
		return m_typeName.split(",");
	}

	public void setEventName(String name)
	{
		m_eventName = name;
	}

	public String[] getEventNames()
	{
		return m_eventName.split(",");
	}

	public void setApplication(String application)
	{
		m_application = application;
	}

	public String getApplication()
	{
		return m_application;
	}

	public void setPolicy(String policy)
	{
		m_policy = policy;
	}

	public String getPolicyId()
			throws BuildException
	{
		if (m_policy == null)
		{
			return null;
		}

		try
		{
			String dqlQuery = MessageFormat.format(DQL_GET_POLICY_ID_BY_NAME, new String[] {m_policy});
			return DqlHelper.getStringParamFromFirstString(this.getSession(), dqlQuery);
		}
		catch (DfException e)
		{
			throw new BuildException("Error occured while trying to get policy r_object_id " +
					"for policy " + m_policy + "\r\n" + e.toString());
		}
		catch (DfEndOfCollectionException e)
		{
			throw new BuildException("There are no policy with name " + m_policy);
		}
	}

	public void setStateName(String name)
	{
		m_stateName = name;
	}

	public String getStateName()
	{
		return m_stateName;
	}

	public void doWork()
			throws BuildException
	{
		if (m_typeName == null || m_eventName == null)
		{
			throw new BuildException("typeName and eventName are mandatory attributes");
		}

		if (m_stateName != null && m_policy == null)
		{
			throw new BuildException("Policy should be specified for state " + m_stateName);
		}
	}

	protected void registerEventForType(String typeName,
										String event,
										boolean auditSubtypes,
										String controllingApp,
										String policyId,
										String stateName,
										boolean signAudit,
										int authentication,
										String eventDescription,
										IDfList attributeList)
			throws BuildException
	{
		this.unregisterEventForType(typeName, event, controllingApp, policyId, stateName);

		try
		{
			IDfId _policyId = policyId == null ? null : new DfId(policyId);
			this.getAuditTrailManager().registerEventForType(typeName, event, auditSubtypes,
					controllingApp, _policyId, stateName, signAudit, authentication,
					eventDescription, attributeList);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to register event " + event + " for type " +
					typeName + "\r\n" + e.toString());
		}
	}

	protected void unregisterEventForType(String typeName,
										  String event,
										  String controllingApp,
										  String policyId,
										  String stateName)
			throws BuildException
	{
		if (!isEventAuditedForType(typeName, event, controllingApp, policyId, stateName))
		{
			return;
		}

		try
		{
			IDfId _policyId = policyId == null ? null : new DfId(policyId);
			this.getAuditTrailManager().unregisterEventForType(typeName, event, controllingApp, _policyId, stateName);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to unregister event " + event + " for type " +
					typeName + "\r\n" + e.toString());
		}
	}

	private boolean isEventAuditedForType(String typeName,
										  String event,
										  String controllingApp,
										  String policyId,
										  String stateName)
			throws BuildException
	{
		try
		{
			return this.getAuditTrailManager().isEventAuditedForType(typeName, event, controllingApp, new DfId(policyId), stateName);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to verify if event " + event +
					" is already audited for type " + typeName + "\r\n" + e.toString());
		}
	}

	private IDfAuditTrailManager getAuditTrailManager()
			throws BuildException
	{
		try
		{
			return this.getSession().getDfSession().getAuditTrailManager();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get AuditTrailManager\r\n" + e.toString());
		}
	}

	private String m_typeName = null;
	private String m_eventName = null;
	private String m_application = null;
	private String m_policy = null;
	private String m_stateName = null;
}
