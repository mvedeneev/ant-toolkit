package com.anttoolkit.svn.tasks.util;

import java.util.*;

public class LogEntry
{
	private String m_author;
	private String m_date;
	private String m_message;
	private int m_revision;

	private List<PathEntry> m_pathEntries;

	public LogEntry(String author, String date, String message, String revision, List<PathEntry> pathEntries)
	{
		m_author = author;
		m_date = parseDateTime(date);
		m_message = message;
		m_revision = Integer.parseInt(revision);
		m_pathEntries = pathEntries;
		Collections.sort(m_pathEntries);

		for (PathEntry entry : m_pathEntries)
		{
			entry.setRevision(m_revision);
		}
	}

	public String getAuthor()
	{
		return m_author;
	}

	public String getDate()
	{
		return m_date;
	}

	public String getMessage()
	{
		return m_message;
	}

	public int getRevision()
	{
		return m_revision;
	}

	public int getEntriesCount()
	{
		return m_pathEntries.size();
	}

	public boolean isEmpty()
	{
		return getEntriesCount() == 0;
	}

	public List<PathEntry> getPathEntries()
	{
		return m_pathEntries;
	}

	public void union(LogEntry logEntry)
	{
		if (getRevision() != logEntry.getRevision())
		{
			throw new IllegalStateException("Failed to union log entries for two different revisions " +
					getRevision() + " and " + logEntry.getRevision());
		}

		if (!getAuthor().equals(logEntry.getAuthor()))
		{
			throw new IllegalStateException("Failed to union log entries for revision " +
					getRevision() + ", for two different authors " +
					getAuthor() + " and " + logEntry.getAuthor());
		}

		List<PathEntry> pathEntries = logEntry.getPathEntries();
		for (PathEntry pathEntry : pathEntries)
		{
			if (!hasPathEntry(pathEntry))
			{
				m_pathEntries.add(pathEntry);
			}
		}

		Collections.sort(m_pathEntries);
	}

	public void removeEntriesNotStartedWithPath(String path)
	{
		if (m_pathEntries == null || m_pathEntries.isEmpty())
		{
			return;
		}

		int count = m_pathEntries.size();
		int i = 0;

		while (i < count)
		{
			PathEntry entry = m_pathEntries.get(i);
			if (!entry.getPath().startsWith(path))
			{
				m_pathEntries.remove(entry);
				count--;
				continue;
			}

			i++;
		}

		Collections.sort(m_pathEntries);
	}

	public void removeEntriesStartedWithPath(String path)
	{
		if (m_pathEntries == null || m_pathEntries.isEmpty())
		{
			return;
		}

		int count = m_pathEntries.size();
		int i = 0;

		while (i < count)
		{
			PathEntry entry = m_pathEntries.get(i);
			if (entry.getPath().startsWith(path))
			{
				m_pathEntries.remove(entry);
				count--;
				continue;
			}

			i++;
		}

		Collections.sort(m_pathEntries);
	}

	public void removeEntriesWithPath(String path)
	{
		if (m_pathEntries == null || m_pathEntries.isEmpty())
		{
			return;
		}

		int count = m_pathEntries.size();
		int i = 0;

		while (i < count)
		{
			PathEntry entry = m_pathEntries.get(i);
			if (entry.getPath().equals(path))
			{
				m_pathEntries.remove(entry);
				count--;
				continue;
			}

			i++;
		}

		Collections.sort(m_pathEntries);
	}

	private boolean hasPathEntry(PathEntry pathEntry)
	{
		for (PathEntry _pathEntry : m_pathEntries)
		{
			if (_pathEntry.getPath().equals(pathEntry.getPath()) &&
				_pathEntry.getAction().equals(pathEntry.getAction()))
			{
				return true;
			}
		}

		return false;
	}

	private String parseDateTime(String date)
	{
		String[] dateTimeParts = date.split("T");
		if (dateTimeParts.length != 2)
		{
			return date;
		}

		String datePart = parseDate(dateTimeParts[0]);
		String timePart = parseTime(dateTimeParts[1]);

		if (datePart == null || timePart == null)
		{
			return date;
		}

		return datePart + " " + timePart;
	}

	private String parseDate(String date)
	{
		String[] parts = date.split("-");
		if (parts.length != 3)
		{
			return null;
		}

		return parts[2] + "." + parts[1] + "." + parts[0];
	}

	private String parseTime(String time)
	{
		String[] parts = time.split(":");
		if (parts.length != 3)
		{
			return null;
		}

		return time.replace("Z", "");
	}
}
