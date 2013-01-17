package com.anttoolkit.documentum.tasks.workflow.util;

import org.apache.tools.ant.*;

import java.util.*;
import java.text.*;

import com.anttoolkit.documentum.common.*;

import com.documentum.fc.common.*;
import com.documentum.fc.client.*;
import com.documentum.bpm.*;
import com.documentum.bpm.sdt.*;

public class Workflow
{
	private static final String DQL_GET_WORKFLOW_PROCESS_ID = "select process_id" +
			" from dm_workflow" +
			" where r_object_id=''{0}''";

	private static final String DQL_GET_CHILD_WORKFLOWS = "select r_object_id" +
			" from dm_workflow" +
			" where parent_id=''{0}''";

	private static final String DQL_GET_SPECIFIC_CHILD_WORKFLOWS = "select r_object_id" +
			" from dm_workflow" +
			" where parent_id=''{0}'' and process_id=''{1}''";

	private static final String DQL_SET_WORKFLOW_STRING_VARIABLE = "update dmc_wfsd_element_string object " +
			"set string_value=''{0}'' " +
			"where workflow_id=''{1}'' and object_name=''{2}''";

	private static final String DQL_SET_WORKFLOW_BOOLEAN_VARIABLE = "update dmc_wfsd_element_boolean object " +
			"set boolean_value={0} " +
			"where workflow_id=''{1}'' and object_name=''{2}''";

	private static final String DQL_SET_WORKFLOW_INT_VARIABLE = "update dmc_wfsd_element_integer object " +
			"set int_value={0} " +
			"where workflow_id=''{1}'' and object_name=''{2}''";

	private static final String DQL_SET_WORKFLOW_DOUBLE_VARIABLE = "update dmc_wfsd_element_double object " +
			"set double_value={0} " +
			"where workflow_id=''{1}'' and object_name=''{2}''";

	private static final String DQL_SET_WORKFLOW_DATE_TIME_VARIABLE = "update dmc_wfsd_element_date object " +
			"set date_value={0} " +
			"where workflow_id=''{1}'' and object_name=''{2}''";

	private static final String DQL_GET_ALL_WORKITEMS = "select wi.r_object_id " +
			"from dmi_queue_item qi, dmi_workitem wi, dm_activity a " +
			"where qi.item_id = wi.r_object_id and wi.r_workflow_id = ''{0}'' and " +
			"wi.r_act_def_id = a.r_object_id and a.object_name = ''{1}'' and " +
			"wi.r_runtime_state in (0, 1)";

	private static final String DQL_GET_PERFORMER_WORKITEMS = "select wi.r_object_id " +
			"from dmi_queue_item qi, dmi_workitem wi, dm_activity a " +
			"where qi.item_id = wi.r_object_id and wi.r_workflow_id = ''{0}'' and " +
			"wi.r_act_def_id = a.r_object_id and a.object_name = ''{1}'' and " +
			"wi.r_runtime_state in (0, 1) and " +
			"(wi.r_performer_name=''{2}'' or " +
			"wi.r_performer_name in (select i_all_users_names from dm_group where group_name=''{2}'') or " +
			"exists (select r_object_id from dm_group where group_name=wi.r_performer_name and any i_all_users_names=''{2}''))";

	private static final String DATE_FORMAT_TEMPLATE = "DATE(''{0}'',''{1}'')";

	private static final String WORKFLOW_NAME_TEMPLATE = "{0};{1};{2}";

	private static final String WORKFLOW_VARIABLE_LOCK_TEMPLATE = "{0}:{1}";

	private ProcessAdapter m_processAdapter = null;

	private IDfId m_workflowId = null;
	private IDfWorkflow m_workflow = null;

	private IDfId m_parentWorkflowId = null;

	private boolean m_isVariablesInitialized = false;
	private Map<String, Object> m_primitiveVariables = new HashMap<String, Object>();
	private Map<String, Object> m_changedPrimitiveVariables = new HashMap<String, Object>();

	private boolean m_isAliasValuesInitialized = false;
	private Map<String, String> m_aliasValues = new HashMap<String, String>();
	private Map<String, String> m_changedAliasValues = new HashMap<String, String>();

	private List<String[]> m_addedAttachments = new LinkedList<String[]>();
	private List<String> m_removedAttachments = new LinkedList<String>();

	private Map<String, List<String>> m_performers = new HashMap<String, List<String>>();

	private String m_supervisor = null;

	public static List<IDfWorkitem> getQueuedWorkitems(DocbaseSession session, String workflowId, String activityName, String performer)
	{
		List<IDfWorkitem> workitems = new LinkedList<IDfWorkitem>();

		IDfCollection coll = null;

		String query = performer == null ?
				MessageFormat.format(DQL_GET_ALL_WORKITEMS, workflowId, activityName) :
				MessageFormat.format(DQL_GET_PERFORMER_WORKITEMS, workflowId, activityName, performer);

		try
		{
			coll = DqlHelper.executeReadQuery(session, query);
			while (coll != null && coll.next())
			{
				IDfId workitemId = coll.getTypedObject().getValueAt(0).asId();
				IDfWorkitem workitem = (IDfWorkitem)session.getDfObject(workitemId);
				workitems.add(workitem);
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get workitem for workflow [" + workflowId
					+ "] activity: " + activityName, e);
		}
		finally
		{
			DqlHelper.closeCollection(coll);
		}

		return workitems;
	}

	public static IDfActivity getNextActivity(IDfWorkitem workitem, String activityName)
			throws DfException
	{
		IDfList activities = workitem.getForwardActivities();
		int count = activities.getCount();
		for (int i = 0; i < count; i++)
		{
			IDfActivity activity = (IDfActivity)activities.get(i);
			if (activity.getObjectName().equals(activityName))
			{
				return activity;
			}
		}

		activities = workitem.getRejectActivities();
		count = activities.getCount();
		for (int i = 0; i < count; i++)
		{
			IDfActivity activity = (IDfActivity)activities.get(i);
			if (activity.getObjectName().equals(activityName))
			{
				return activity;
			}
		}

		return null;
	}

	public static Workflow newInstance(DocbaseSession session, IDfId processId)
	{
		if (ConversionHelper.isNullId(processId))
		{
			throw new IllegalArgumentException("Process id couldn't be empty");
		}

		return new Workflow(ProcessAdapter.getInstance(session, processId));
	}

	public static Workflow newInstance(DocbaseSession session, IDfId processId, IDfId parentWorkflowId)
	{
		Workflow subWorkflow = newInstance(session, processId);
		subWorkflow.m_parentWorkflowId = parentWorkflowId;

		return subWorkflow;
	}

	public static Workflow newInstance(DocbaseSession session, String processName)
	{
		if (ConversionHelper.isEmptyString(processName))
		{
			throw new IllegalArgumentException("Process name couldn't be empty");
		}

		return new Workflow(ProcessAdapter.getInstance(session, processName));
	}

	public static Workflow newInstance(DocbaseSession session, String processName, IDfId parentWorkflowId)
	{
		Workflow subWorkflow = newInstance(session, processName);
		subWorkflow.m_parentWorkflowId = parentWorkflowId;

		return subWorkflow;
	}

	public static Workflow getInstance(DocbaseSession session, IDfId workflowId)
	{
		if (ConversionHelper.isNullId(workflowId))
		{
			throw new IllegalArgumentException("Workflow id couldn't be empty");
		}

		IDfId processId = null;

		try
		{
			processId = DqlHelper.getIdParamFromFirstString(session, MessageFormat.format(DQL_GET_WORKFLOW_PROCESS_ID, workflowId.toString()));
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get processId for workflowId=" + workflowId, e);
		}

		return new Workflow(session, ProcessAdapter.getInstance(session, processId), workflowId);
	}

	private Workflow(ProcessAdapter processAdapter)
	{
		m_processAdapter = processAdapter;
	}

	private Workflow(DocbaseSession session, ProcessAdapter processAdapter, IDfId workflowId)
	{
		m_workflowId = workflowId;
		m_processAdapter = processAdapter;

		try
		{
			m_workflow = (IDfWorkflow)session.getDfObject(m_workflowId);
		}
		catch (BuildException e)
		{
			throw new BuildException("Failed to load workflow object by its id=" + m_workflowId, e);
		}
	}

	public IDfId getWorkflowId()
	{
		return m_workflowId;
	}

	public IDfId startWorkflow(DocbaseSession session, String workflowName, Map<String, List<String>> packageDocuments)
			throws BuildException
	{
		try
		{
			if (m_workflow != null && m_workflow.getRuntimeState() != IDfWorkflow.DF_WF_STATE_DORMANT)
			{
				throw new BuildException("Workflow is already started");
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get workflow runtime state", e);
		}

		if (m_workflow == null)
		{
			try
			{
				workflowName = workflowName != null ? workflowName : MessageFormat.format(WORKFLOW_NAME_TEMPLATE,
						m_processAdapter.getProcessName(session), session.getUserName(), (new DfTime()).asString(IDfTime.DF_TIME_PATTERN18));

				m_workflow = (IDfWorkflow)session.createDfObject("dm_workflow");
				m_workflow.setObjectName(workflowName);
				m_workflow.setProcessId(m_processAdapter.getProcessId());
				m_workflow.setParentId(m_parentWorkflowId);

				if (m_supervisor  != null)
				{
					m_workflow.setSupervisorName(m_supervisor);
				}

				m_workflow.save();
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to create workflow", e);
			}

			createVariablesForNewWorkflow();
		}
		else
		{
			saveChangedWorkflowVariables(session);
		}

		saveChangedAliasValues(session);
		saveAttachments();
		saveActivityPerformers();

		try
		{
			if (m_workflow.isDirty())
			{
				m_workflow.save();
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to save workflow after adding attachments and/or adding performers", e);
		}

		try
		{
			m_workflow.execute();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to execute workflow", e);
		}

		try
		{
			initializeStartActivities(packageDocuments);
			return m_workflowId = m_workflow.getObjectId();
		}
		catch (DfException e)
		{
			try
			{
				m_workflow.abort();
			}
			catch (DfException ex)
			{
			}

			try
			{
				m_workflow.destroy();
			}
			catch (DfException ex)
			{
			}

			throw new BuildException("Failed to initialize start activities", e);
		}
	}

	public void setAliasValue(String name, String value)
	{
		if (!ConversionHelper.isEmptyString(name) &&
			!ConversionHelper.isEmptyString(value))
		{
			m_aliasValues.put(name, value);
			m_changedAliasValues.put(name, value);
		}
	}

	public String getAliasValue(DocbaseSession session, String name)
	{
		if (m_aliasValues.containsKey(name))
		{
			return m_aliasValues.get(name);
		}

		if (!m_isAliasValuesInitialized)
		{
			initAliasValues(session);
		}

		return m_aliasValues.get(name);
	}

	public void addAttachment(String componentType, String componentId)
	{
		if (m_removedAttachments.contains(componentId))
		{
			m_removedAttachments.remove(componentId);
			return;
		}

		m_addedAttachments.add(new String[] {componentType, componentId});
	}

	public void removeAttachment(String componentId)
	{
		for (String[] attachmentInfo : m_addedAttachments)
		{
			if (attachmentInfo[1].equals(componentId))
			{
				m_addedAttachments.remove(attachmentInfo);
				return;
			}
		}

		m_removedAttachments.add(componentId);
	}

	public IDfPrimitiveType getVariableType(String variableName)
	{
		return m_processAdapter.getPrimitiveVariableType(variableName);
	}

	public void setStringVariable(String name, String value)
	{
		if (!m_processAdapter.supportPrimitiveVariable(name, IDfPrimitiveType.STRING))
		{
			throw new IllegalArgumentException("String variable \"" + name + "\" is not supported in process template");
		}

		m_primitiveVariables.put(name, value);
		m_changedPrimitiveVariables.put(name, value);
	}

	public String getStringVariable(String name)
	{
		if (m_primitiveVariables.containsKey(name))
		{
			return (String)m_primitiveVariables.get(name);
		}

		if (!m_processAdapter.supportPrimitiveVariable(name, IDfPrimitiveType.STRING))
		{
			throw new IllegalArgumentException("String variable \"" + name + "\" is not supported in process template");
		}

		if (!m_isVariablesInitialized)
		{
			initWorkflowVariables();
		}

		return (String)m_primitiveVariables.get(name);
	}

	public void setIntVariable(String name, int value)
	{
		if (!m_processAdapter.supportPrimitiveVariable(name, IDfPrimitiveType.INT))
		{
			throw new IllegalArgumentException("Integer variable \"" + name + "\" is not supported in process template");
		}

		m_primitiveVariables.put(name, value);
		m_changedPrimitiveVariables.put(name, value);
	}

	public int getIntVariable(String name)
	{
		if (m_primitiveVariables.containsKey(name))
		{
			Integer value = (Integer)m_primitiveVariables.get(name);
			return value == null ? 0 : value;
		}

		if (!m_isVariablesInitialized)
		{
			initWorkflowVariables();
		}

		if (!m_processAdapter.supportPrimitiveVariable(name, IDfPrimitiveType.INT))
		{
			throw new IllegalArgumentException("Integer variable \"" + name + "\" is not supported in process template");
		}

		Integer value = (Integer)m_primitiveVariables.get(name);

		return value == null ? 0 : value;
	}

	public void setDoubleVariable(String name, double value)
	{
		if (!m_processAdapter.supportPrimitiveVariable(name, IDfPrimitiveType.FLOAT))
		{
			throw new IllegalArgumentException("Double variable \"" + name + "\" is not supported in process template");
		}

		m_primitiveVariables.put(name, value);
		m_changedPrimitiveVariables.put(name, value);
	}

	public double getDoubleVariable(String name)
	{
		if (m_primitiveVariables.containsKey(name))
		{
			Double value = (Double)m_primitiveVariables.get(name);
			return value == null ? 0 : value;
		}

		if (!m_processAdapter.supportPrimitiveVariable(name, IDfPrimitiveType.FLOAT))
		{
			throw new IllegalArgumentException("Double variable \"" + name + "\" is not supported in process template");
		}

		if (!m_isVariablesInitialized)
		{
			initWorkflowVariables();
		}

		Double value = (Double) m_primitiveVariables.get(name);

		return value == null ? Double.NaN : value;
	}

	public void setBooleanVariable(String name, boolean value)
	{
		if (!m_processAdapter.supportPrimitiveVariable(name, IDfPrimitiveType.BOOLEAN))
		{
			throw new IllegalArgumentException("Boolean variable \"" + name + "\" is not supported in process template");
		}

		m_primitiveVariables.put(name, value);
		m_changedPrimitiveVariables.put(name, value);
	}

	public boolean getBooleanVariable(String name)
	{
		if (m_primitiveVariables.containsKey(name))
		{
			Boolean value = (Boolean)m_primitiveVariables.get(name);
			return value == null ? false : value;
		}

		if (!m_processAdapter.supportPrimitiveVariable(name, IDfPrimitiveType.BOOLEAN))
		{
			throw new IllegalArgumentException("Boolean variable \"" + name + "\" is not supported in process template");
		}

		if (!m_isVariablesInitialized)
		{
			initWorkflowVariables();
		}

		Boolean value = (Boolean) m_primitiveVariables.get(name);

		return value == null ? false : value;
	}

	public void setTimeVariable(String name, IDfTime value)
	{
		if (!m_processAdapter.supportPrimitiveVariable(name, IDfPrimitiveType.DATE))
		{
			throw new IllegalArgumentException("DateTime variable \"" + name + "\" is not supported in process template");
		}

		m_primitiveVariables.put(name, value);
		m_changedPrimitiveVariables.put(name, value);
	}

	public IDfTime getTimeVariable(String name)
	{
		if (m_primitiveVariables.containsKey(name))
		{
			return (IDfTime)m_primitiveVariables.get(name);
		}

		if (!m_processAdapter.supportPrimitiveVariable(name, IDfPrimitiveType.DATE))
		{
			throw new IllegalArgumentException("DateTime variable \"" + name + "\" is not supported in process template");
		}

		if (!m_isVariablesInitialized)
		{
			initWorkflowVariables();
		}

		return (IDfTime)m_primitiveVariables.get(name);
	}

	public void setVariable(String name, String value, String format)
	{
		IDfPrimitiveType variableType = m_processAdapter.getPrimitiveVariableType(name);

		if (IDfPrimitiveType.BOOLEAN.equals(variableType))
		{
			setBooleanVariable(name, ConversionHelper.convertToBoolean(value));
		}
		else if (IDfPrimitiveType.STRING.equals(variableType))
		{
			setStringVariable(name, value);
		}
		else if (IDfPrimitiveType.INT.equals(variableType))
		{
			setIntVariable(name, ConversionHelper.convertToInt(value));
		}
		else if (IDfPrimitiveType.FLOAT.equals(variableType))
		{
			setDoubleVariable(name, ConversionHelper.convertToDouble(value));
		}
		else if (IDfPrimitiveType.DATE.equals(variableType))
		{
			setTimeVariable(name, new DfTime(value, format == null ? "dd.MM.yyyy hh:mm" : format));
		}
	}

	public void setSupervisor(String supervisor)
	{
		m_supervisor = supervisor;
	}

	public Workflow getParent(DocbaseSession session)
			throws DfException
	{
		if (m_workflow == null || m_workflow.isNew() || ConversionHelper.isNullId(m_workflow.getParentId()))
		{
			return null;
		}

		return getInstance(session, m_workflow.getParentId());
	}

	public boolean hasChildren(DocbaseSession session)
			throws DfException
	{
		return m_workflow != null && !m_workflow.isNew() &&
				DqlHelper.exist(session, MessageFormat.format(DQL_GET_CHILD_WORKFLOWS, m_workflow.getObjectId().toString()));
	}

	public Workflow[] getChildren(DocbaseSession session)
			throws DfException
	{
		if (m_workflow == null || m_workflow.isNew())
		{
			return null;
		}

		ArrayList<Workflow> children = new ArrayList<Workflow>();

		IDfCollection coll = null;

		try
		{
			coll = DqlHelper.executeReadQuery(session, MessageFormat.format(DQL_GET_CHILD_WORKFLOWS, m_workflow.getObjectId().toString()));
			if (coll == null || !coll.next())
			{
				return null;
			}

			do
			{
				children.add(getInstance(session, coll.getId("r_object_id")));
			} while (coll.next());
		}
		finally
		{
			DqlHelper.closeCollection(coll);
		}

		return children.size() == 0 ? null : children.toArray(new Workflow[]{});
	}

	public Workflow[] getChildren(DocbaseSession session, IDfId processId)
			throws DfException
	{
		if (ConversionHelper.isNullId(processId) || m_workflow == null || m_workflow.isNew())
		{
			return null;
		}

		ArrayList<Workflow> children = new ArrayList<Workflow>();

		IDfCollection coll = null;

		try
		{
			coll = DqlHelper.executeReadQuery(session, MessageFormat.format(DQL_GET_SPECIFIC_CHILD_WORKFLOWS, m_workflow.getObjectId().toString(), processId.toString()));

			if (coll == null || !coll.next())
			{
				return null;
			}

			do
			{
				children.add(getInstance(session, coll.getId("r_object_id")));
			} while (coll.next());
		}
		finally
		{
			DqlHelper.closeCollection(coll);
		}

		return children.size() == 0 ? null : children.toArray(new Workflow[]{});
	}

	public Workflow[] getChildren(DocbaseSession session, String processName)
			throws DfException
	{
		if (ConversionHelper.isEmptyString(processName))
		{
			return null;
		}

		IDfId processId = null;


		try
		{
			processId = DocbaseObjectsHelper.getObjectIdByObjectName(session, "dm_process", processName);
		}
		catch (DfEndOfCollectionException e)
		{
			return null;
		}

		return getChildren(session, processId);
	}

	public List<IDfWorkitem> getQueuedWorkitems(DocbaseSession session, String activityName, String performer)
	{
		try
		{
			if (m_workflow == null || m_workflow.isNew())
			{
				return null;
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to verify if workflow is new", e);
		}

		return getQueuedWorkitems(session, getWorkflowId().getId(), activityName, performer);
	}


	public void fetch(DocbaseSession session)
			throws DfException
	{
		if (m_workflow == null || m_workflow.isNew())
		{
			return;
		}

		m_workflow.fetch(null);

		m_isAliasValuesInitialized = false;
		m_aliasValues.clear();
		m_changedAliasValues.clear();

		m_isVariablesInitialized = false;
		m_primitiveVariables.clear();
		m_changedPrimitiveVariables.clear();

		initAliasValues(session);
		initWorkflowVariables();
	}

	public void save(DocbaseSession session)
	{
		try
		{
			if (m_workflow == null || m_workflow.isNew())
			{
				return;
			}
		}
		catch (DfException e)
		{
				throw new BuildException("Failed to verify if workflow is new", e);
		}

		saveChangedAliasValues(session);
		saveChangedWorkflowVariables(session);

		saveAttachments();
		saveActivityPerformers();

		if (m_supervisor != null)
		{
			try
			{
				m_workflow.updateSupervisorName(m_supervisor);
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to update supervisor of running workflow " + m_workflowId, e);
			}
		}

		try
		{
			if (m_workflow.isDirty())
			{
				m_workflow.save();
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to save workflow after adding attachments and/or adding performers", e);
		}
	}

	public void lock()
	{
		if (ConversionHelper.isNullId(m_workflowId))
		{
			return;
		}

		SynchronizationManager.lock(m_workflowId);
	}

	public void unlock()
	{
		if (ConversionHelper.isNullId(m_workflowId))
		{
			return;
		}

		SynchronizationManager.unlock(m_workflowId);
	}

	public void lockVariable(String variableName)
	{
		if (m_workflow == null || ConversionHelper.isEmptyString(variableName))
		{
			return;
		}

		SynchronizationManager.lock(MessageFormat.format(WORKFLOW_VARIABLE_LOCK_TEMPLATE, m_workflowId.toString(), variableName));
	}

	public void unlockVariable(String variableName)
	{
		if (m_workflow == null || ConversionHelper.isEmptyString(variableName))
		{
			return;
		}

		SynchronizationManager.unlock(MessageFormat.format(WORKFLOW_VARIABLE_LOCK_TEMPLATE, m_workflowId.toString(), variableName));
	}

	public void setActivityPerformers(String activityName, List<String> performerNames)
	{
		m_performers.put(activityName, performerNames);
	}

	public void postEvent(String eventName)
			throws DfException
	{
		postEvent(eventName, 0, false, new DfTime(), "");
	}

	public void postEvent(String eventName, int priority, boolean sendMail, IDfTime dueDate, String message)
	{
		try
		{
			m_workflow.queue(null, eventName, priority, sendMail, dueDate, sendMail ? message : "");
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to post event '" + eventName + "' to workflow '" + m_workflowId + "'", e);
		}
	}

	public void postEventToParent(DocbaseSession session, String eventName)
			throws DfException
	{
		postEventToParent(session, eventName, 0, false, new DfTime(), "");
	}

	public void postEventToParent(DocbaseSession session, String eventName, int priority, boolean sendMail, IDfTime dueDate, String message)
			throws DfException
	{
		Workflow parent = getParent(session);
		if (parent == null)
		{
			return;
		}

		parent.postEvent(eventName, priority, sendMail, dueDate, message);
	}

	public void destroy(DocbaseSession session)
			throws DfException
	{
		if (m_workflow == null || m_workflow.isNew())
		{
			return;
		}

		destroyChildren(session);

		try
		{
			// stop process
			m_workflow.fetch(null);
			m_workflow.abort();

			// delete process
			m_workflow.fetch(null);
			m_workflow.destroy();
		}
		catch (DfException e)
		{
			throw new DfException("Failed to destroy workflow with r_object_id=" + m_workflowId, e);
		}
	}

	public void destroyChildren(DocbaseSession session)
			throws DfException
	{
		destroyChildren(session, (IDfId)null);
	}

	public void destroyChildren(DocbaseSession session, String processName)
			throws DfException
	{
		if (ConversionHelper.isEmptyString(processName))
		{
			return;
		}

		IDfId processId = null;

		try
		{
			processId = DocbaseObjectsHelper.getObjectIdByObjectName(session, "dm_process", processName);
		}
		catch (DfEndOfCollectionException e)
		{
			return;
		}

		destroyChildren(session, processId);
	}

	public void destroyChildren(DocbaseSession session, IDfId processId)
			throws DfException
	{
		Workflow[] children = ConversionHelper.isNullId(processId) ? getChildren(session) : getChildren(session, processId);
		if (children == null || children.length == 0)
		{
			return;
		}

		try
		{
			for (Workflow child : children)
			{
				child.destroyChildren(session, (IDfId)null);
				child.destroy(session);
			}
		}
		catch (DfException e)
		{
			throw new DfException("Failed to destroy child workflows", e);
		}
	}

	private void initializeStartActivities(Map<String, List<String>> packageDocuments)
			throws DfException
	{
		if (packageDocuments == null || packageDocuments.isEmpty())
		{
			return;
		}

		StartActivityInfo[] startActivities = m_processAdapter.getStartActivities();
		if (startActivities == null || startActivities.length == 0)
		{
			return;
		}

		for (StartActivityInfo activity : startActivities)
		{
			List<String> documentIds = packageDocuments.get(activity.packageName);
			if (documentIds == null || documentIds.isEmpty())
			{
				throw new BuildException("No documents specified for package '" + activity.packageName + "'");
			}

			IDfList dfList = new DfList();
			for (String documentId : documentIds)
			{
				dfList.appendId(new DfId(documentId));
			}

			m_workflow.addPackage(activity.activityName, activity.inputPortName, activity.packageName,
					activity.packageType, null, false, dfList);
		}
	}

	private void initAliasValues(DocbaseSession session)
	{
		try
		{
			if (m_workflow == null || m_workflow.isNew())
			{
				return;
			}
		}
		catch (DfException e)
		{
			throw new RuntimeException("Failed to initialize workflow alias set values", e);
		}

		Map<String, String> nameValuePairs = null;
		try
		{
			nameValuePairs = AliasHelper.getWorkflowAliasSetNameValuePairs(session, m_workflow.getObjectId());
		}
		catch (DfEndOfCollectionException e)
		{
			m_isAliasValuesInitialized = true;
			return;
		}
		catch (DfException e)
		{
			throw new RuntimeException("Failed to initialize workflow alias set values", e);
		}

		if (nameValuePairs == null || nameValuePairs.size() == 0)
		{
			m_isAliasValuesInitialized = true;
			return;
		}

		Set<String> names = nameValuePairs.keySet();
		for (String name : names)
		{
			if (!m_aliasValues.containsKey(name))
			{
				m_aliasValues.put(name, nameValuePairs.get(name));
			}
		}

		m_isAliasValuesInitialized = true;
	}

	private void saveChangedAliasValues(DocbaseSession session)
			throws BuildException
	{
		try
		{
			if (m_changedAliasValues == null || m_changedAliasValues.size() == 0 ||
				m_workflow == null || m_workflow.isNew())
			{
				return;
			}

			AliasHelper.setWorkflowAliasSetNameValuePairs(session, m_workflow.getObjectId(), m_changedAliasValues);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to save changed alias values", e);
		}

		m_changedAliasValues.clear();
	}

	private void initWorkflowVariables()
	{
		String[] names = m_processAdapter.getPrimitiveVariableNames();
		if (names == null || !(m_workflow instanceof IDfWorkflowEx))
		{
			m_isVariablesInitialized = true;
			return;
		}

		for (String name : names)
		{
			if (m_primitiveVariables.containsKey(name))
			{
				continue;
			}

			Object value = null;

			try
			{
				value = m_workflow != null && !m_workflow.isNew() ?
					((IDfWorkflowEx)m_workflow).getPrimitiveVariableValue(name) :
					m_processAdapter.getPrimitiveVariableDefaultValue(name);
			}
			catch (DfException e)
			{
				throw new RuntimeException("Failed to initialize workflow variables", e);
			}

			IDfPrimitiveType type = m_processAdapter.getPrimitiveVariableType(name);

			if (type.equals(IDfPrimitiveType.DATE))
			{
				value = value == null ? DfTime.DF_NULLDATE : new DfTime((Date)value);
			}
			else if (type.equals(IDfPrimitiveType.BOOLEAN))
			{
				value = value == null ? Boolean.FALSE : value;
			}
			else if (type.equals(IDfPrimitiveType.INT))
			{
				value = value == null ? 0 : value;
			}
			else if (type.equals(IDfPrimitiveType.FLOAT))
			{
				value = value == null ? 0 : value;
			}

			m_primitiveVariables.put(name, value);
		}

		m_isVariablesInitialized = true;
	}

	private void createVariablesForNewWorkflow()
			throws BuildException
	{
		if (m_primitiveVariables.size() == 0 || m_workflow == null ||
			!(m_workflow instanceof IDfWorkflowEx))
		{
			return;
		}

		Set<String> names = m_primitiveVariables.keySet();
		for (String name : names)
		{
			Object value = m_primitiveVariables.get(name);
			IDfPrimitiveType type = m_processAdapter.getPrimitiveVariableType(name);

			try
			{
				if (type.equals(IDfPrimitiveType.DATE))
				{
					Date dateValue = value == null ? null : ((IDfTime)value).getDate();
					((IDfWorkflowEx)m_workflow).setPrimitiveObjectValue(name, dateValue);
					continue;
				}

				((IDfWorkflowEx)m_workflow).setPrimitiveObjectValue(name, value);
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to create variables for new workflow", e);
			}
		}
	}

	private void saveChangedWorkflowVariables(DocbaseSession session)
			throws BuildException
	{
		try
		{
			if (m_changedPrimitiveVariables.size() == 0 ||
				m_workflow == null || m_workflow.isNew() ||
				m_workflow.getRuntimeState() == IDfWorkflow.DF_WF_STATE_DORMANT ||
				m_workflow.getRuntimeState() == IDfWorkflow.DF_WF_STATE_TERMINATED ||
				m_workflow.getRuntimeState() == IDfWorkflow.DF_WF_STATE_FINISHED)
			{
				return;
			}

			Set<String> names = m_changedPrimitiveVariables.keySet();
			for (String name : names)
			{
				Object value = m_changedPrimitiveVariables.get(name);
				IDfPrimitiveType type = m_processAdapter.getPrimitiveVariableType(name);

				if (type.equals(IDfPrimitiveType.STRING))
				{
					saveStringVariable(session, name, value == null ? null : value.toString());
				}
				else if (type.equals(IDfPrimitiveType.BOOLEAN))
				{
					saveBooleanVariable(session, name, (Boolean)value);
				}
				else if (type.equals(IDfPrimitiveType.INT))
				{
					saveIntVariable(session, name, (Integer)value);
				}
				else if (type.equals(IDfPrimitiveType.FLOAT))
				{
					saveDoubleVariable(session, name, (Double)value);
				}
				else if (type.equals(IDfPrimitiveType.DATE))
				{
					saveDateTimeVariable(session, name, (IDfTime)value);
				}
				else
				{
					throw new DfException("Undefined primitive type for workflow variable \"" + name + "\"");
				}
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to save changed workflow variables", e);
		}
	}

	private void saveStringVariable(DocbaseSession session, String name, String value)
			throws DfException
	{
		String _value = value == null ? "" : value.replaceAll("'", "''");

		DqlHelper.executeQuery(session, MessageFormat.format(DQL_SET_WORKFLOW_STRING_VARIABLE, _value, m_workflowId.toString(), name));
	}

	private void saveBooleanVariable(DocbaseSession session, String name, boolean value)
			throws DfException
	{
		DqlHelper.executeQuery(session, MessageFormat.format(DQL_SET_WORKFLOW_BOOLEAN_VARIABLE, Boolean.toString(value), m_workflowId.toString(), name));
	}

	private void saveIntVariable(DocbaseSession session, String name, int value)
			throws DfException
	{
		DqlHelper.executeQuery(session, MessageFormat.format(DQL_SET_WORKFLOW_INT_VARIABLE, Integer.toString(value), m_workflowId.toString(), name));
	}

	private void saveDoubleVariable(DocbaseSession session, String name, double value)
			throws DfException
	{
		DqlHelper.executeQuery(session, MessageFormat.format(DQL_SET_WORKFLOW_DOUBLE_VARIABLE, Double.toString(value), m_workflowId.toString(), name));
	}

	private void saveDateTimeVariable(DocbaseSession session, String name, IDfTime value)
			throws DfException
	{
		String _value = value == null || value.isNullDate() ?
				"DATE('NULLDATE')" :
				MessageFormat.format(DATE_FORMAT_TEMPLATE, value.asString(IDfTime.DF_TIME_PATTERN18), IDfTime.DF_TIME_PATTERN18);

		DqlHelper.executeQuery(session, MessageFormat.format(DQL_SET_WORKFLOW_DATE_TIME_VARIABLE, _value, m_workflowId.toString(), name));
	}

	private void saveActivityPerformers()
			throws BuildException
	{
		try
		{
			if (m_workflow == null || m_workflow.isNew() || m_performers.isEmpty())
			{
				return;
			}

			for (String activity : m_performers.keySet())
			{
				List<String> performers = m_performers.get(activity);

				IDfList dfList = new DfList();
				for (String name : performers)
				{
					dfList.appendString(name);
				}

				m_workflow.setPerformers(activity, dfList);
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to save activity performers", e);
		}
	}

	private void saveAttachments()
			throws BuildException
	{
		try
		{
			if (m_workflow == null || m_workflow.isNew() ||
				(m_addedAttachments.isEmpty() && m_removedAttachments.isEmpty()))
			{
				return;
			}

			for (String attachmentId : m_removedAttachments)
			{
				m_workflow.removeAttachment(new DfId(attachmentId));
			}

			for (String[] attachmentInfo : m_addedAttachments)
			{
				m_workflow.addAttachment(attachmentInfo[0], new DfId(attachmentInfo[1]));
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to save workflow attachments", e);
		}
	}

}
