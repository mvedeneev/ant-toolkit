package com.anttoolkit.svn.tasks.util;

import java.util.*;

public class PathEntry
		implements Comparable
{
	public static final String ADD_ACTION = "A";
	public static final String DELETE_ACTION = "D";
	public static final String MODIFY_ACTION = "M";

	private String m_action;
	private String m_path;
	private int m_revision = -1;

	public static PathEntry getFirstOccurenceOfPath(String path, List<PathEntry> pathEntries)
	{
		for (PathEntry pathEntry : pathEntries)
		{
			if (path.equals(pathEntry.getPath()))
			{
				return pathEntry;
			}
		}

		return null;
	}

	public static PathEntry getLastOccurenceOfPath(String path, List<PathEntry> pathEntries)
	{
		PathEntry _pathEntry = null;

		for (PathEntry pathEntry : pathEntries)
		{
			if (path.equals(pathEntry.getPath()))
			{
				_pathEntry = pathEntry;
			}
		}

		return _pathEntry;

	}

	public PathEntry(String path,
	                 String action)
	{
		m_path = path;
		m_action = action;
	}

	public String toString()
	{
		return m_path;
	}

	public int compareTo(Object path)
	{
		if (!(path instanceof PathEntry))
		{
			throw new IllegalArgumentException("Can't compare PathEntry with " + path.getClass().getName());
		}

		PathEntry pathEntry = (PathEntry)path;

		return m_revision < pathEntry.getRevision() ? -1 :
				m_revision > pathEntry.getRevision() ? 1 : m_path.compareTo(pathEntry.getPath());
	}

	public String getPath()
	{
		return m_path;
	}

	public String getAction()
	{
		return m_action;
	}

	public boolean isDeleteAction()
	{
		return DELETE_ACTION.equals(m_action);
	}

	public boolean isAddAction()
	{
		return ADD_ACTION.equals(m_action);
	}

	public boolean isModifyAction()
	{
		return !isDeleteAction() && !isAddAction();
	}

	public void setRevision(int revision)
	{
		m_revision = revision;
	}

	public int getRevision()
	{
		return m_revision;
	}
}
