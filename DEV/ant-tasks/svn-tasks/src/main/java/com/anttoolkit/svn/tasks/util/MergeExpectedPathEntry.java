package com.anttoolkit.svn.tasks.util;

import java.util.*;

public class MergeExpectedPathEntry
{
	private String m_path;
	private int m_allowedRevision = -1;
	private String[] m_allowedActions;
	private String m_pathSuffixIgnoredInComparison;

	public MergeExpectedPathEntry(String path, String pathSuffixIgnoredInComparison, int allowedRevision, String[] allowedActions)
	{
		m_path = path;
		m_pathSuffixIgnoredInComparison = pathSuffixIgnoredInComparison;
		m_allowedRevision = allowedRevision;
		m_allowedActions = allowedActions;
	}

	public int getAllowedRevision()
	{
		return m_allowedRevision;
	}

	public boolean isExpectedPathEntry(PathEntry pathEntry)
	{
		String path = pathEntry.getPath();
		if (m_pathSuffixIgnoredInComparison != null && path.startsWith(m_pathSuffixIgnoredInComparison))
		{
			path = path.substring(m_pathSuffixIgnoredInComparison.length());
		}

		if (!m_path.equals(path))
		{
			return false;
		}

		if (m_allowedRevision > pathEntry.getRevision())
		{
			return false;
		}

		for (String action : m_allowedActions)
		{
			if (action.equals(pathEntry.getAction()))
			{
				return true;
			}
		}

		return false;
	}

	public boolean isExpectedPathEntryExists(List<PathEntry> pathEntries)
	{
		for (PathEntry pathEntry : pathEntries)
		{
			if (isExpectedPathEntry(pathEntry))
			{
				return true;
			}
		}

		return false;
	}
}
