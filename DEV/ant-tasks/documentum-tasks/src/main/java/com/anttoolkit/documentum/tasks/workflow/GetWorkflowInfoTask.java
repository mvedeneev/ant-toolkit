package com.anttoolkit.documentum.tasks.workflow;

import java.text.MessageFormat;
import java.util.*;

import org.apache.tools.ant.*;

import com.documentum.bpm.sdt.*;
import com.documentum.fc.common.*;

import com.anttoolkit.documentum.tasks.workflow.util.*;

import com.anttoolkit.documentum.common.*;

public class GetWorkflowInfoTask
	extends GenericDocbaseTask
{
	private List<Variable> m_variables = new LinkedList<Variable>();
	private List<Alias> m_aliases = new LinkedList<Alias>();
	private List<Package> m_packages = new LinkedList<Package>();

	private String m_workflowId = null;

	public class Variable
	{
		private String m_name = null;
		private String m_property = null;
		private String m_format = null;

		public void setName(String name)
		{
			m_name = name;
		}

		public void setPropertyName(String property)
		{
			m_property = property;
		}

		public void setFormat(String format)
		{
			m_format = format;
		}

		public void setProperty(Workflow workflow)
		{
			IDfPrimitiveType type = workflow.getVariableType(m_name);

			if (IDfPrimitiveType.BOOLEAN.equals(type))
			{
				setProperty(Boolean.toString(workflow.getBooleanVariable(m_name)));
			}
			else if (IDfPrimitiveType.STRING.equals(type))
			{
				setProperty(workflow.getStringVariable(m_name));
			}
			else if (IDfPrimitiveType.INT.equals(type))
			{
				setProperty(Integer.toString(workflow.getIntVariable(m_name)));
			}
			else if (IDfPrimitiveType.FLOAT.equals(type))
			{
				setProperty(Double.toString(workflow.getDoubleVariable(m_name)));
			}
			else if (IDfPrimitiveType.DATE.equals(type))
			{
				IDfTime time = workflow.getTimeVariable(m_name);
				setProperty(time.asString(m_format != null ? m_format : "dd.MM.yyyy hh:mm:ss"));
			}
		}

		private void setProperty(String value)
		{
			GetWorkflowInfoTask.this.getProject().setProperty(m_property, value);
		}
	}

	public class Alias
	{
		private String m_name = null;
		private String m_property = null;

		public void setName(String name)
		{
			m_name = name;
		}

		public void setPropertyName(String property)
		{
			m_property = property;
		}

		public void setProperty(Workflow workflow)
		{
			String value = workflow.getAliasValue(GetWorkflowInfoTask.this.getSession(), m_name);
			GetWorkflowInfoTask.this.getProject().setProperty(m_property, value);
		}
	}

	public class Package
	{
		private static final String DQL_GET_WORKFLOW_PACKAGE_OBJECT = "select r_component_id " +
				"from dmi_package " +
				"where r_workflow_id = ''{0}'' and r_package_name=''{1}'' " +
				"order by i_acceptance_date desc enable(return_top 1)";

		private String m_name = null;
		private String m_objectIdProperty = null;

		public void setName(String name)
		{
			m_name = name;
		}

		public void setObjectIdProperty(String property)
		{
			m_objectIdProperty = property;
		}

		public void setProperty(Workflow workflow)
		{
			if (m_name == null || m_objectIdProperty == null)
			{
				throw new BuildException("Package name and objectId property name should be specified");
			}

			String query = MessageFormat.format(DQL_GET_WORKFLOW_PACKAGE_OBJECT, workflow.getWorkflowId().getId(), m_name);

			try
			{
				String objectId = DqlHelper.getStringParamFromFirstString(GetWorkflowInfoTask.this.getSession(), query);
				GetWorkflowInfoTask.this.getProject().setProperty(m_objectIdProperty, objectId);
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to get objectId for workflow [" +
						workflow.getWorkflowId().getId() + "] package: " + m_name, e);
			}
			catch (DfEndOfCollectionException e)
			{
				throw new BuildException("No package '" + m_name + "' found for workflow [" +
						workflow.getWorkflowId().getId() + "]", e);
			}
		}
	}

	public Variable createVariable()
	{
		Variable variable = new Variable();
		m_variables.add(variable);
		return variable;
	}

	public Alias createAlias()
	{
		Alias alias = new Alias();
		m_aliases.add(alias);
		return alias;
	}

	public Package createPackage()
	{
		Package _package = new Package();
		m_packages.add(_package);
		return _package;
	}

	public void setWorkflowId(String id)
	{
		m_workflowId = id;
	}

	public void doWork() throws BuildException
	{
		if (m_variables.isEmpty() && m_aliases.isEmpty() && m_packages.isEmpty())
		{
			return;
		}

		if (m_workflowId == null)
		{
			throw new BuildException("Workflow id should be specified");
		}

		Workflow workflow = Workflow.getInstance(getSession(), new DfId(m_workflowId));

		for (Variable variable : m_variables)
		{
			variable.setProperty(workflow);
		}

		for (Alias alias : m_aliases)
		{
			alias.setProperty(workflow);
		}

		for (Package _package : m_packages)
		{
			_package.setProperty(workflow);
		}
	}
}
