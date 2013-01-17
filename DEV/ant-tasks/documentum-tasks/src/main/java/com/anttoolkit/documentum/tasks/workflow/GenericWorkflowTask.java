package com.anttoolkit.documentum.tasks.workflow;

import java.util.*;

import com.anttoolkit.general.tasks.arrays.util.ArrayManager;
import org.apache.tools.ant.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

import com.anttoolkit.documentum.common.*;
import com.anttoolkit.documentum.tasks.workflow.util.*;

public abstract class GenericWorkflowTask
		extends GenericDocbaseTask
{
	private List<Package> m_packages = new LinkedList<Package>();
	private List<Alias> m_aliases = new LinkedList<Alias>();
	private List<Variable> m_variables = new LinkedList<Variable>();
	private List<Attachment> m_attachments = new LinkedList<Attachment>();
	private List<Activity> m_activities = new LinkedList<Activity>();
	private List<CompleteWorkitem> m_workitemsToComplete = new LinkedList<CompleteWorkitem>();
	private List<PostEvent> m_events = new LinkedList<PostEvent>();

	private Supervisor m_supervisor = new Supervisor();

	public class Package
	{
		private String m_name = null;
		private String m_objectId = null;

		public void setName(String name)
		{
			m_name = name;
		}

		public String getName()
		{
			return m_name;
		}

		public void setObjectId(String id)
		{
			m_objectId = id;
		}

		public String getObjectId()
		{
			return m_objectId;
		}
	}

	public class Alias
	{
		private String m_name = null;
		private String m_value = null;

		public void setName(String name)
		{
			m_name = name;
		}

		public String getName()
		{
			return m_name;
		}

		public void setValue(String value)
		{
			m_value = value;
		}

		public String getValue()
		{
			return m_value;
		}
	}

	public class Variable
	{
		private String m_name = null;
		private String m_value = null;
		private String m_timeFormat = null;

		public void setName(String name)
		{
			m_name = name;
		}

		public String getName()
		{
			return m_name;
		}

		public void setValue(String value)
		{
			m_value = value;
		}

		public String getValue()
		{
			return m_value;
		}

		public void setFormat(String format)
		{
			m_timeFormat = format;
		}

		public String getFormat()
		{
			return m_timeFormat;
		}
	}

	public class Attachment
	{
		private String m_type = null;
		private String m_objectId = null;
		private boolean m_remove = false;

		public void setType(String type)
		{
			m_type = type;
		}

		public String getType()
		{
			return m_type;
		}

		public void setObjectId(String id)
		{
			m_objectId = id;
		}

		public String getObjectId()
		{
			return m_objectId;
		}

		public void setRemove(boolean remove)
		{
			m_remove = remove;
		}

		public boolean getRemove()
		{
			return m_remove;
		}
	}

	public class Activity
	{
		private String m_name = null;
		private String m_performer = null;
		private String m_performersArray = null;

		public void setName(String name)
		{
			m_name = name;
		}

		public String getName()
		{
			return m_name;
		}

		public void setPerformer(String performer)
		{
			m_performer = performer;
		}

		public String getPerformer()
		{
			return m_performer;
		}

		public void setPerformersArray(String performersArray)
		{
			m_performersArray = performersArray;
		}

		public String getPerformersArray()
		{
			return m_performersArray;
		}
	}

	public class CompleteWorkitem
	{
		private String m_activity = null;
		private String m_nextActivity = null;
		private String m_performer = null;

		public void setActivity(String activity)
		{
			m_activity = activity;
		}

		public void setNextActivity(String activity)
		{
			m_nextActivity = activity;
		}

		public void setForPerformer(String performer)
		{
			m_performer = performer;
		}

		public void complete(Workflow workflow)
		{
			String[] targetActivityNames = m_nextActivity == null ? null : m_nextActivity.split(",");
			DfList targetActivities = new DfList();

			List<IDfWorkitem> workitems = workflow.getQueuedWorkitems(GenericWorkflowTask.this.getSession(), m_activity, m_performer);

			if (workitems == null || workitems.isEmpty())
			{
				throw new BuildException("There are no workitems for workflow [" + workflow.getWorkflowId() +
					"] activity: " + m_activity);
			}

			if (targetActivityNames != null)
			{
				for (String activityName : targetActivityNames)
				{
					try
					{
						IDfActivity activity = Workflow.getNextActivity(workitems.get(0), activityName.trim());
						if (activity != null)
						{
							targetActivities.append(activity);
						}
					}
					catch (DfException e)
					{
						throw new BuildException("Failed to get next activity '" + activityName + "' for activity '" +
								m_activity + "' in workflow [" + workflow.getWorkflowId().getId() + "]", e);
					}
				}
			}

			if (targetActivityNames != null && targetActivities.getCount() == 0)
			{
				throw new BuildException("There are no next activities for activity '" + m_activity +
						"' in workflow [" + workflow.getWorkflowId().getId() + "]");
			}

			for (IDfWorkitem workitem : workitems)
			{
				try
				{
					if (workitem.getRuntimeState() == IDfWorkitem.DF_WI_STATE_DORMANT)
					{
						workitem.acquire();
					}
				}
				catch (DfException e)
				{
					throw new BuildException("Failed to acquire workflow [" + workflow.getWorkflowId().getId()
							+ "] workitem form activity: " + m_activity, e);
				}

				try
				{
					if (targetActivityNames != null)
					{
						workitem.setOutputByActivities(targetActivities);
					}
				}
				catch (DfException e)
				{
					throw new BuildException("Failed to set next activities for workflow [" + workflow.getWorkflowId().getId()
							+ "] activity: " + m_activity, e);
				}

				try
				{
					if (workitem.getRuntimeState() == IDfWorkitem.DF_WI_STATE_ACQUIRED)
					{
						workitem.complete();
					}
				}
				catch (DfException e)
				{
					throw new BuildException("Failed to complete workflow [" + workflow.getWorkflowId().getId()
							+ "] workitem form activity: " + m_activity, e);
				}
			}
		}
	}

	public class Supervisor
	{
		private String m_name = null;

		public void setName(String name)
		{
			m_name = name;
		}

		public String getName()
		{
			return m_name;
		}
	}

	public class PostEvent
	{
		private String m_event = null;
		private String m_message = null;
		private boolean m_sendMail = false;

		public void setEvent(String event)
		{
			m_event = event;
		}

		public void setMessage(String message)
		{
			m_message = message;
		}

		public void setSendMail(boolean send)
		{
			m_sendMail = send;
		}

		public void postEvent(Workflow workflow)
		{
			workflow.postEvent(m_event, 0, m_sendMail, new DfTime(), m_message);
		}
	}

	public Package createPackage()
	{
		Package pack = new Package();
		m_packages.add(pack);
		return pack;
	}

	public Alias createAlias()
	{
		Alias alias = new Alias();
		m_aliases.add(alias);
		return alias;
	}

	public Variable createVariable()
	{
		Variable variable = new Variable();
		m_variables.add(variable);
		return variable;
	}

	public Attachment createAttachment()
	{
		Attachment attachment = new Attachment();
		m_attachments.add(attachment);
		return attachment;
	}

	public Activity createActivity()
	{
		Activity activity = new Activity();
		m_activities.add(activity);
		return activity;
	}

	public Supervisor createSupervisor()
	{
		return m_supervisor;
	}

	public CompleteWorkitem createCompleteWorkitem()
	{
		CompleteWorkitem workitem = new CompleteWorkitem();
		m_workitemsToComplete.add(workitem);
		return workitem;
	}

	public PostEvent createPostEvent()
	{
		PostEvent event = new PostEvent();
		m_events.add(event);
		return event;
	}

	protected List<Package> getPackages()
	{
		return m_packages;
	}

	protected List<Alias> getAliases()
	{
		return m_aliases;
	}

	protected List<Variable> getVariables()
	{
		return m_variables;
	}

	protected List<Attachment> getAttachments()
	{
		return m_attachments;
	}

	protected List<Activity> getActivities()
	{
		return m_activities;
	}

	protected void initWorkflowSettings(Workflow workflow)
	{
		if (workflow == null)
		{
			return;
		}

		initAliases(workflow);
		initVariables(workflow);
		initAttachments(workflow);
		initActivities(workflow);
		initSupervisor(workflow);
	}

	protected void completeWorkitems(Workflow workflow)
	{
		for (CompleteWorkitem workitem : m_workitemsToComplete)
		{
			workitem.complete(workflow);
		}
	}

	protected void postEvents(Workflow workflow)
	{
		for (PostEvent event : m_events)
		{
			event.postEvent(workflow);
		}
	}

	private void initAliases(Workflow workflow)
	{
		List<Alias> aliases = getAliases();
		for (Alias alias : aliases)
		{
			workflow.setAliasValue(alias.getName(), alias.getValue());
		}
	}

	private void initVariables(Workflow workflow)
	{
		List<Variable> variables = getVariables();
		for (Variable variable : variables)
		{
			workflow.setVariable(variable.getName(), variable.getValue(), variable.getFormat());
		}
	}

	private void initAttachments(Workflow workflow)
	{
		List<Attachment> attachments = getAttachments();
		for (Attachment attachment : attachments)
		{
			if (attachment.getRemove())
			{
				workflow.removeAttachment(attachment.getObjectId());
			}
			else
			{
				workflow.addAttachment(attachment.getType(), attachment.getObjectId());
			}
		}
	}

	private void initActivities(Workflow workflow)
	{
		List<Activity> activities = getActivities();
		for (Activity activity : activities)
		{
			List<String> performerNames = new LinkedList<String>();

			if (activity.getPerformer() != null)
			{
				String[] names = activity.getPerformer().split(",");
				for (String name : names)
				{
					if (!performerNames.contains(name.trim()))
					{
						performerNames.add(name.trim());
					}
				}
			}
			else
			{
				List names = ArrayManager.getArrayData(activity.getPerformersArray());
				for (Object name : names)
				{
					if (!performerNames.contains(((String)name).trim()))
					{
						performerNames.add(((String)name).trim());
					}
				}
			}

			workflow.setActivityPerformers(activity.getName(), performerNames);
		}
	}

	private void initSupervisor(Workflow workflow)
	{
		workflow.setSupervisor(m_supervisor.getName());
	}
}