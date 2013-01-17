package com.anttoolkit.documentum.tasks.workflow;

import com.anttoolkit.documentum.tasks.workflow.util.*;
import org.apache.tools.ant.*;

import com.anttoolkit.documentum.common.*;

import com.documentum.fc.common.*;

public class KillWorkflowTask
		extends GenericDocbaseTask
{
	private String m_workflowId = null;
	private boolean m_killChildren = false;

	public void setWorkflowId(String workflowId)
	{
		m_workflowId = workflowId;
	}

	public void setKillChildren(boolean kill)
	{
		m_killChildren = kill;
	}

	public void doWork() throws BuildException
	{
		if (m_workflowId == null)
		{
			throw new BuildException("Workflow id should be specified");
		}

		processObjectsBatch(m_workflowId);
	}

	protected void processSingleObjectFromBatch(int iteration, IDfId workflowId)
			throws BuildException
	{
		Workflow workflow = Workflow.getInstance(getSession(), workflowId);

		try
		{
			if (m_killChildren)
			{
				workflow.destroyChildren(getSession(), (IDfId)null);
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to kill children for workflow " + workflowId, e);
		}

		try
		{
			workflow.destroy(getSession());
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to kill workflow " + workflowId, e);
		}
	}
}
