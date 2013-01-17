package com.anttoolkit.documentum.tasks.lifecycle;

import com.anttoolkit.documentum.common.*;

import org.apache.tools.ant.*;

import com.documentum.fc.common.*;
import com.documentum.fc.client.*;

import java.text.*;

public class SetLifecycleActionsModuleTask
		extends GenericDocbaseTask
{
	private static final String GET_POLICY_ID = "select r_object_id from dm_policy " +
			"where object_name=''{0}''";

	public void setLifecycle(String lifecycle)
	{
		m_lifecycle = lifecycle;
	}

	public void setModule(String module)
	{
		m_module = module;
	}

	public void doWork()
			throws BuildException
	{
		if (m_lifecycle == null)
		{
			throw new BuildException("Lifecycle doesn't specified");
		}

		if (m_module == null)
		{
			throw new BuildException("Module doesn't specified");
		}

		IDfPolicy policy = getPolicy();

		try
		{
			policy.unInstall(false);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to uninstall policy " + m_lifecycle + "\r\n" + e.toString());
		}

		int count;

		try
		{
			count = policy.getStateNoCount();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get policy states count\r\n" + e.toString());
		}

		try
		{
			for (int i = 0; i < count; i++)
			{
				policy.setUserActionService(i, m_module);
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to set state action module\r\n" + e.toString());
		}

		try
		{
			policy.save();
		}
		catch (Exception e)
		{
			throw new BuildException("Failed to save modified policy " + m_lifecycle + "\r\n" + e.toString());
		}

		try
		{
			policy.validate();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to validate policy " + m_lifecycle + "\r\n" + e.toString());
		}

		try
		{
			policy.install(false);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to install policy " + m_lifecycle + "\r\n" + e.toString());
		}
	}

	private IDfPolicy getPolicy()
			throws BuildException
	{
		try
		{
			String dqlQuery = MessageFormat.format(GET_POLICY_ID, new String[] {m_lifecycle});
			String policyId = DqlHelper.getStringParamFromFirstString(this.getSession(), dqlQuery);

			IDfSysObject policyObj = (IDfSysObject)this.getSession().getDfObject(policyId);

			return (IDfPolicy)policyObj;
		}
		catch (Exception e)
		{
			throw new BuildException("Failed to get IDfPolicy for policy " + m_lifecycle +
					"\r\n" + e.toString());
		}
	}

	private String m_lifecycle = null;
	private String m_module = null;
}
