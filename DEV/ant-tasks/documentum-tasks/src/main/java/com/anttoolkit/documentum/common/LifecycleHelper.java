package com.anttoolkit.documentum.common;

import org.apache.tools.ant.*;

import com.documentum.fc.common.*;

import java.text.*;
import java.util.*;
import java.util.concurrent.*;

public class LifecycleHelper
{
	private static final String DQL_GET_POLICY_ID = "select r_object_id from dm_policy" +
			" where object_name=''{0}''";

	private static Map<String, IDfId> m_lifecycleIds = new ConcurrentHashMap<String, IDfId>();

	public static IDfId getLifecycleId(DocbaseSession session, String lifecycleName)
	{
		if (session == null || lifecycleName == null)
		{
			return null;
		}

		if (m_lifecycleIds.containsKey(lifecycleName))
		{
			return m_lifecycleIds.get(lifecycleName);
		}

		String query = MessageFormat.format(DQL_GET_POLICY_ID, lifecycleName);

		try
		{
			IDfId id = DqlHelper.getIdParamFromFirstString(session, query);
			m_lifecycleIds.put(lifecycleName, id);
			return id;
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get lifecycleId for lifecycle '" + lifecycleName + "'");
		}
		catch (DfEndOfCollectionException e)
		{
			throw new BuildException("Lifecycle '" + lifecycleName + "' doesn't exist in docbase");
		}
	}
}
