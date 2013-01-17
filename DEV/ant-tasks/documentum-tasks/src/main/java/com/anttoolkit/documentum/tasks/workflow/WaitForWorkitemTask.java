package com.anttoolkit.documentum.tasks.workflow;

import java.util.*;

import com.anttoolkit.general.tasks.arrays.util.ArrayManager;
import org.apache.tools.ant.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

import com.anttoolkit.documentum.common.*;
import com.anttoolkit.documentum.tasks.workflow.util.*;

public class WaitForWorkitemTask
		extends GenericDocbaseTask
{
	private String m_workflowId = null;
	private String m_activity = null;
	private String m_performer = null;
	private String m_performersArray = null;
	private String m_sleepTimeout = null;
	private String m_waitTime = null;
	private String m_workitemIdProperty = null;
	private String m_workitemIdArray = null;
	private String m_workitemPerformerProperty = null;
	private String m_workitemPerformerArray = null;

	public void setWorkflowId(String workflowId)
	{
		m_workflowId = workflowId;
	}

	public void setActivity(String activity)
	{
		m_activity = activity;
	}

	public void setPerformer(String performer)
	{
		m_performer = performer;
	}

	public void setPerformersArray(String performersArray)
	{
		m_performersArray = performersArray;
	}

	public void setSleepTimeout(String sleepTimeout)
	{
		m_sleepTimeout = sleepTimeout;
	}

	public void setWaitTime(String waitTime)
	{
		m_waitTime = waitTime;
	}

	public void setWorkitemIdProperty(String workitemIdProperty)
	{
		m_workitemIdProperty = workitemIdProperty;
	}

	public void setWorkitemIdArray(String workitemIdArray)
	{
		m_workitemIdArray = workitemIdArray;
	}

	public void setWorkitemPerformerProperty(String property)
	{
		m_workitemPerformerProperty = property;
	}

	public void setWorkitemPerformerArray(String performerArray)
	{
		m_workitemPerformerArray = performerArray;
	}

	public void doWork() throws BuildException
	{
		if (m_workflowId == null)
		{
			throw new BuildException("Workflow id should be specified");
		}

		if (m_activity == null)
		{
			throw new BuildException("Activity name should be specified");
		}

		long startTime = System.currentTimeMillis();
		long currentTime = startTime;
		long waitTime = getWaitTime();
		long sleepTimeout = getSleepTimeout();

		List<String> performersToWaitFor = getPerformersToWaitItemsFor();
		Map<String, List<String>> detectedPerformerWorkitems = new HashMap<String, List<String>>();

		while (currentTime - startTime < waitTime)
		{
			//try to find existing workitems
			if (performersToWaitFor.isEmpty())
			{
				List<IDfWorkitem> workitems = Workflow.getQueuedWorkitems(getSession(), m_workflowId, m_activity, null);
				appendDetectedPerformerWorkitems(workitems, detectedPerformerWorkitems);
			}
			else
			{
				List<String> detectedPerformers = new LinkedList<String>();

				for (String performer : performersToWaitFor)
				{
					List<IDfWorkitem> workitems = Workflow.getQueuedWorkitems(getSession(), m_workflowId, m_activity, performer);
					if (workitems != null && !workitems.isEmpty())
					{
						appendDetectedPerformerWorkitems(workitems, detectedPerformerWorkitems);
						detectedPerformers.add(performer);
					}
				}

				performersToWaitFor.removeAll(detectedPerformers);
			}

			//check if all workitems were found
			if (performersToWaitFor.isEmpty() && !detectedPerformerWorkitems.isEmpty())
			{
				break;
			}

			//wait before starting next cycle
			try
			{
				Thread.sleep(sleepTimeout);
			}
			catch (InterruptedException e)
			{
				throw new BuildException("Wait cycle was interrupted", e);
			}

			currentTime = System.currentTimeMillis();
		}

		//all workitems were found
		if (performersToWaitFor.isEmpty() && !detectedPerformerWorkitems.isEmpty())
		{
			initVariables(detectedPerformerWorkitems);
			return;
		}

		//throw exceptions
		if (performersToWaitFor.isEmpty())
		{
			throw new BuildException("Wait time exceed " + waitTime + "ms, but no workitems were detected");
		}

		StringBuilder notDetectedPerformers = new StringBuilder();
		for (String performer : performersToWaitFor)
		{
			if (notDetectedPerformers.length() != 0)
			{
				notDetectedPerformers.append(", ");
			}

			notDetectedPerformers.append(performer);
		}

		StringBuilder detectedPerformers = new StringBuilder();
		for (String performer : detectedPerformerWorkitems.keySet())
		{
			if (detectedPerformers.length() != 0)
			{
				detectedPerformers.append(", ");
			}

			detectedPerformers.append(performer);
		}

		throw new BuildException("Wait time exceed " + waitTime + "ms, " +
				"but no workitems were detected for performers: " + notDetectedPerformers.toString() +
				". Only workitems for performers detected: " + detectedPerformers.toString());
	}

	private void initVariables(Map<String, List<String>> detectedPerformerWorkitems)
	{
		if (m_workitemIdProperty == null && m_workitemIdArray == null &&
			m_workitemPerformerProperty == null && m_workitemPerformerArray == null)
		{
			return;
		}

		int iteration = 0;
		Set<String> performers = detectedPerformerWorkitems.keySet();

		for (String performer : performers)
		{
			List<String> workitems = detectedPerformerWorkitems.get(performer);

			if (iteration == 0 && m_workitemIdProperty != null)
			{
				getProject().setProperty(m_workitemIdProperty, workitems.get(0));
			}

			if (iteration == 0 && m_workitemPerformerProperty != null)
			{
				getProject().setProperty(m_workitemPerformerProperty, performer);
			}

			for (String workitemId : workitems)
			{
				if (m_workitemIdArray != null)
				{
					List data = ArrayManager.getArrayData(m_workitemIdArray, true);
					if (!data.contains(workitemId))
					{
						data.add(workitemId);
					}
					else
					{
						continue;
					}
				}

				if (m_workitemPerformerArray != null)
				{
					List data = ArrayManager.getArrayData(m_workitemPerformerArray, true);
					data.add(performer);
				}
			}

			iteration++;
		}
	}

	private void appendDetectedPerformerWorkitems(List<IDfWorkitem> workitems, Map<String, List<String>> detectedPerformerWorkitems)
	{
		if (workitems == null || workitems.isEmpty())
		{
			return;
		}

		for (IDfWorkitem workitem : workitems)
		{
			String performer = null;
			String workitemId = null;

			try
			{
				int state = workitem.getRuntimeState();
				if (state != IDfWorkitem.DF_WI_STATE_DORMANT &&
					state != IDfWorkitem.DF_WI_STATE_ACQUIRED)
				{
					continue;
				}

				performer = workitem.getPerformerName();
				workitemId = workitem.getObjectId().getId();
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to get workitem info", e);
			}

			if (!detectedPerformerWorkitems.containsKey(performer))
			{
				List<String> list = new LinkedList<String>();
				list.add(workitemId);
				detectedPerformerWorkitems.put(performer, list);
				continue;
			}

			List<String> list = detectedPerformerWorkitems.get(performer);
			if (!list.contains(workitemId))
			{
				list.add(workitemId);
			}
		}
	}

	private long getSleepTimeout()
	{
		if (m_sleepTimeout == null)
		{
			return 1000;	// 1 second by default
		}

		return parseTimeout(m_sleepTimeout);
	}

	private long getWaitTime()
	{
		if (m_waitTime == null)
		{
			return 300000;	// 5 minutes by default
		}

		return parseTimeout(m_waitTime);
	}

	private long parseTimeout(String timeout)
	{
		try
		{
			if (timeout.endsWith("s"))
			{
				return Long.parseLong(timeout.substring(0, timeout.length() - 1)) * 1000;
			}
			else if (timeout.endsWith("m"))
			{
				return Long.parseLong(timeout.substring(0, timeout.length() - 1)) * 60000;
			}
			else if (timeout.endsWith("h"))
			{
				return Long.parseLong(timeout.substring(0, timeout.length() - 1)) * 3600000;
			}
			else if (timeout.endsWith("d"))
			{
				return Long.parseLong(timeout.substring(0, timeout.length() - 1)) * 86400000;
			}

			return Long.parseLong(timeout.substring(0, timeout.length()));
		}
		catch (NumberFormatException e)
		{
			throw new BuildException("Invalid timeout specification: " + timeout, e);
		}
	}

	private List<String> getPerformersToWaitItemsFor()
	{
		List<String> performers = new LinkedList<String>();

		if (m_performer != null)
		{
			String[] performerNames = m_performer.split(",");
			for (String name : performerNames)
			{
				performers.add(name.trim());
			}
		}

		if (m_performersArray != null)
		{
			List performersArray = ArrayManager.getArrayData(m_performersArray);
			for (Object item : performersArray)
			{
				performers.add(item.toString());
			}
		}

		return performers;
	}
}
