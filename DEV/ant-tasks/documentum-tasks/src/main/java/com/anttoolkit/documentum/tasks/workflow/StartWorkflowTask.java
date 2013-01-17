package com.anttoolkit.documentum.tasks.workflow;

import com.anttoolkit.documentum.tasks.workflow.util.Workflow;
import com.documentum.fc.common.DfId;
import org.apache.tools.ant.BuildException;

import java.text.SimpleDateFormat;
import java.util.*;

public class StartWorkflowTask
	extends GenericWorkflowTask
{
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy");

	private String m_template = null;
	private String m_parentId = null;
	private String m_workflowName = null;
	private String m_newWorkflowIdProperty = null;

	public void setTemplate(String template)
	{
		m_template = template;
	}

	public void setParentId(String id)
	{
		m_parentId = id;
	}

	public void setWorkflowName(String name)
	{
		m_workflowName = name;
	}

	public void setNewWorkflowIdProperty(String property)
	{
		m_newWorkflowIdProperty = property;
	}


	public void doWork() throws BuildException
	{
		if (m_template == null)
		{
			throw new BuildException("Workflow template should be specified");
		}

		if (getPackages().isEmpty())
		{
			throw new BuildException("No packages specified");
		}

		if (m_workflowName == null)
		{
			m_workflowName = m_template + ", " + DATE_FORMATTER.format(new Date());
		}

		List<Package> packages = getPackages();
		Map<String, List<String>> packageDocuments = new HashMap<String, List<String>>();

		for (Package _package : packages)
		{
			List<String> ids = new LinkedList<String>();
			ids.add(_package.getObjectId());
			packageDocuments.put(_package.getName(), ids);
		}

		Workflow workflow = m_parentId == null ?
				Workflow.newInstance(getSession(), m_template) :
				Workflow.newInstance(getSession(), m_template, new DfId(m_parentId));

		initWorkflowSettings(workflow);

		workflow.startWorkflow(getSession(), m_workflowName, packageDocuments);

		if (m_newWorkflowIdProperty != null)
		{
			this.getProject().setProperty(m_newWorkflowIdProperty, workflow.getWorkflowId().getId());
		}
	}
}
