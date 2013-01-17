package com.anttoolkit.documentum.tasks.workflow;

import com.anttoolkit.documentum.tasks.workflow.util.*;

import com.documentum.fc.common.*;

import org.apache.tools.ant.*;

public class UpdateWorkflowTask
		extends GenericWorkflowTask
{
	private String m_workflowId = null;

	public void setWorkflowId(String id)
	{
		m_workflowId = id;
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
		Workflow workflow = Workflow.getInstance(getSession(), new DfId(m_workflowId));

		initWorkflowSettings(workflow);

		workflow.save(getSession());

		completeWorkitems(workflow);
		postEvents(workflow);
	}
}
