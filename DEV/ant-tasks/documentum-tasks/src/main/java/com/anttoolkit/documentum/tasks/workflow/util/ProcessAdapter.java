package com.anttoolkit.documentum.tasks.workflow.util;

import java.util.*;

import com.documentum.bpm.*;
import com.documentum.bpm.sdt.*;
import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

import com.anttoolkit.documentum.common.*;
import org.apache.tools.ant.*;

public class ProcessAdapter
{
	private static volatile Map<String, IDfId> m_processIdsMap = new HashMap<String, IDfId>();
	private static volatile Map<IDfId, String> m_processNamesMap = new HashMap<IDfId, String>();
	private static volatile Map<IDfId, ProcessAdapter> m_processMap = new HashMap<IDfId, ProcessAdapter>();

	private IDfId m_processId = null;
	private String m_processName = null;
	private StartActivityInfo[] m_startActivities = null;
	private Map<String, IDfProcessVariableMetaData> m_primitiveVariables = new HashMap<String, IDfProcessVariableMetaData>();
	private String[] m_primitiveVariableNames = null;

	public static ProcessAdapter getInstance(DocbaseSession session, String processName)
	{
		if (ConversionHelper.isEmptyString(processName))
		{
			throw new IllegalArgumentException("Process name couldn't be empty");
		}


		IDfId processId = null;

		synchronized (m_processIdsMap)
		{
			if (m_processIdsMap.containsKey(processName))
			{
				return getInstance(session, m_processIdsMap.get(processName));
			}

			try
			{
				processId = DocbaseObjectsHelper.getObjectIdByObjectName(session, "dm_process", processName);
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to get processId by object_name=" + processName, e);
			}
			catch (DfEndOfCollectionException e)
			{
				throw new BuildException("Process with name \"" + processName + "\" doesn't exist in docbase", e);
			}

			m_processIdsMap.put(processName, processId);
		}

		return getInstance(session, processId);
	}

	public static ProcessAdapter getInstance(DocbaseSession session, IDfId processId)
	{
		if (ConversionHelper.isNullId(processId))
		{
			throw new IllegalArgumentException("Process id couldn't be empty");
		}

		synchronized (m_processMap)
		{
			if (m_processMap.containsKey(processId))
			{
				return m_processMap.get(processId);
			}

			ProcessAdapter processInfo = new ProcessAdapter(processId);
			processInfo.init(session);

			m_processMap.put(processId, processInfo);

			return processInfo;
		}
	}

	private ProcessAdapter(IDfId processId)
	{
		m_processId = processId;
	}

	public IDfId getProcessId()
	{
		return m_processId;
	}

	public String getProcessName(DocbaseSession session)
	{
		if (m_processName != null)
		{
			return m_processName;
		}

		synchronized (m_processNamesMap)
		{
			if (m_processNamesMap.containsKey(m_processId))
			{
				return m_processName = m_processNamesMap.get(m_processId);
			}

			try
			{
				m_processName = DocbaseObjectsHelper.getObjectNameByObjectId(session, "dm_process", m_processId);
			}
			catch (DfException e)
			{
				throw new RuntimeException("Failed to get process name by id=" + m_processId, e);
			}

			m_processNamesMap.put(m_processId, m_processName);
		}

		return m_processName;
	}

	public StartActivityInfo[] getStartActivities()
	{
		return m_startActivities;
	}

	public String[] getPrimitiveVariableNames()
	{
		return m_primitiveVariableNames;
	}

	public Object getPrimitiveVariableDefaultValue(String name)
	{
		if (!m_primitiveVariables.containsKey(name))
		{
			throw new IllegalArgumentException("Process variable with name \"" + name + "\" doesn't exist");
		}

		return m_primitiveVariables.get(name).getDefaultValue();
	}

	public IDfPrimitiveType getPrimitiveVariableType(String name)
	{
		if (!m_primitiveVariables.containsKey(name))
		{
			throw new IllegalArgumentException("Process variable with name \"" + name + "\" doesn't exist");
		}

		return m_primitiveVariables.get(name).getPrimitiveType();
	}

	public boolean supportPrimitiveVariable(String name, IDfPrimitiveType type)
	{
		if (ConversionHelper.isEmptyString(name) || type == null)
		{
			return false;
		}

		IDfProcessVariableMetaData metadata = m_primitiveVariables.get(name);

		return metadata != null && metadata.getPrimitiveType().equals(type);
	}

	private void init(DocbaseSession session)
	{
		IDfProcess process = (IDfProcess)session.getDfObject(m_processId);

		try
		{
			ArrayList<StartActivityInfo> startActivities = new ArrayList<StartActivityInfo>();

			int activityCount = process.getActivityCount();
			for (int i = 0; i < activityCount; i++)
			{
				if (process.getActivityType(i) != 1)
				{
					continue;
				}

				String activityName = process.getActivityName(i);
				IDfId activityId = process.getActivityDefId(i);
				IDfActivity activity = (IDfActivity)session.getDfObject(activityId);

				int packageCount = activity.getPackageCount();
				for(int j = 0; j < packageCount; j++)
				{
					if(activity.getPortType(j).equals("INPUT"))
					{
						startActivities.add(new StartActivityInfo(activityName,
								activity.getPortName(j), activity.getPackageName(j), activity.getPackageType(j)));
					}
				}
			}

			if (startActivities.size() != 0)
			{
				m_startActivities = startActivities.toArray(new StartActivityInfo[]{});
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get start activities for process " + m_processId.toString());
		}

		if (!(process instanceof IDfProcessEx))
		{
			return;
		}

		try
		{
			String[] names = ((IDfProcessEx)process).getVariableNames();
			if (names == null || names.length == 0)
			{
				return;
			}

			ArrayList<String> primitiveVariablesNames = new ArrayList<String>();

			for (String name : names)
			{
				IDfProcessVariableMetaData metadata = ((IDfProcessEx)process).getVariableMetaData(name);
				if (metadata.isPrimitiveType())
				{
					m_primitiveVariables.put(name, metadata);
					primitiveVariablesNames.add(name);
				}
			}

			m_primitiveVariableNames = primitiveVariablesNames.size() == 0 ? null :
					primitiveVariablesNames.toArray(new String[]{});
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get process variables information for process " + m_processId.toString());
		}
	}
}
