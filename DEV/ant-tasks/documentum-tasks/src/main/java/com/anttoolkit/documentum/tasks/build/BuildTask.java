package com.anttoolkit.documentum.tasks.build;

import com.anttoolkit.documentum.common.*;

import com.anttoolkit.documentum.tasks.build.util.BuildVersionManager;
import org.apache.tools.ant.*;

import java.util.*;

public class BuildTask
		extends GenericDocbaseTask
		implements TaskContainer
{
	public static final String VERSION_PROVIDER_PROJECT_PROPERTY = "docbase.version.provider";

	private String m_version = null;
	private String m_versionProvider = DocbaseVersionProvider.class.getCanonicalName();
	private IVersionProvider m_provider = null;
	private String m_qualifier = null;
	private List<Task> m_tasks = new LinkedList<Task>();

	public void setNumber(String number)
	{
		m_version = number;

		try
		{
			Float.parseFloat(m_version);
		}
		catch (NumberFormatException e)
		{
			throw new BuildException("Invalid version number format " + number);
		}
	}

	public void setVersionProvider(String versionProvider)
	{
		m_versionProvider = versionProvider;
	}

	public void setQualifier(String qualifier)
	{
		m_qualifier = qualifier;
	}

	public void addTask(Task task)
	{
		if (task == null)
		{
			return;
		}

		m_tasks.add(task);
	}

	public void doWork()
			throws BuildException
	{
		if (m_version == null)
		{
			throw new BuildException("Version number should be specified");
		}

		if (m_qualifier != null && m_qualifier.trim().length() != 0)
		{
			BuildVersionManager.setContextQualifier(m_qualifier);
		}

		m_versionProvider = m_versionProvider == null ?
				this.getProject().getProperty(VERSION_PROVIDER_PROJECT_PROPERTY) :
				m_versionProvider;

		if (m_versionProvider == null)
		{
			throw new BuildException("Version provider was't specified");
		}

		if (!provider().canUpdateToVersion(m_version))
		{
			return;
		}

		String currentVersion = provider().getVersion();

		boolean outOfTurnUpdate = provider().outOfTurnUpdate(m_version, currentVersion);

		String versionTitle = BuildVersionManager.getContextQualifier() == null ||
				BuildVersionManager.getContextQualifier().trim().length() == 0 ?
				m_version :
				m_version + " [" + BuildVersionManager.getContextQualifier() + "]";

		if (outOfTurnUpdate)
		{
			versionTitle = versionTitle + " <<OUT OF TURN UPDATE>>";
		}

		this.log("Updating docbase from version " + currentVersion + " to " + versionTitle);

		for (Task task : m_tasks)
		{
			task.perform();
		}

		provider().updateToVersion(currentVersion, m_version);

		if (m_qualifier != null && m_qualifier.trim().length() != 0)
		{
			BuildVersionManager.releaseContextQualifier();
		}

		this.log("Docbase version was updated from " + currentVersion + " to " + versionTitle);
	}

	private IVersionProvider provider()
			throws BuildException
	{
		if (m_provider != null)
		{
			return m_provider;
		}

		try
		{
			Class providerClass = Class.forName(m_versionProvider, true, this.getClass().getClassLoader());
			m_provider = (IVersionProvider)providerClass.newInstance();
			return m_provider;
		}
		catch (ClassNotFoundException e)
		{
			throw new BuildException("Failed to get instance of version provider class \"" +
					m_versionProvider, e);
		}
		catch (InstantiationException e)
		{
			throw new BuildException("Failed to get instance of version provider class \"" +
					m_versionProvider, e);
		}
		catch (IllegalAccessException e)
		{
			throw new BuildException("Failed to get instance of version provider class \"" +
					m_versionProvider, e);
		}
	}
}
