package com.anttoolkit.documentum.tasks.irm;

import java.text.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.tools.ant.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import com.documentum.services.irm.*;

import com.anttoolkit.documentum.common.*;

public abstract class GenericIRMTask
		extends GenericDocbaseTask
{
	private static final String IRM_SERVICE = "com.documentum.services.irm.IIRMService";

	private static final String DQL_GET_IRM_PROFILE_ID = "select r_object_id from irm_profile where object_name=''{0}'' enable(return_top 1)";

	private static ThreadLocal<IIRMService> m_irmService = new ThreadLocal<IIRMService>()
	{
		protected IIRMService initialValue()
		{
			return null;
		}
	};

	private static Map<String, IDfSysObject> m_irmProfiles = new ConcurrentHashMap<String, IDfSysObject>();

	protected IIRMService getIRMService()
	{
		if (m_irmService.get() != null)
		{
			return m_irmService.get();
		}

		IDfSession session = getSession().getDfSession();

		try
		{
			m_irmService.set((IIRMService)session.getClient().newService(IRM_SERVICE, session.getSessionManager()));
			return m_irmService.get();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get IRM service SBO from docbase", e);
		}
	}

	protected IDfSysObject getIRMProfileByName(String profileName)
	{
		if (m_irmProfiles.containsKey(profileName))
		{
			return m_irmProfiles.get(profileName);
		}

		try
		{
			String query = MessageFormat.format(DQL_GET_IRM_PROFILE_ID, profileName);
			String profileId = DqlHelper.getStringParamFromFirstString(getSession(), query);
			IDfSysObject irmProfile = (IDfSysObject)getDfObject(profileId);

			m_irmProfiles.put(profileName, irmProfile);

			return irmProfile;
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get IRM profile '" + profileName + "'", e);
		}
		catch (DfEndOfCollectionException e)
		{
			throw new BuildException("IRM profile with name '" + profileName + "' doesn't exist in docbase");
		}
	}
}
