package com.anttoolkit.svn.tasks;

import java.util.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;

import com.anttoolkit.svn.tasks.util.*;
import com.anttoolkit.common.*;
import com.anttoolkit.general.tasks.arrays.util.*;

public class UnmergedChangesReportTask
		extends GenericTask
{
	private String m_trunkFile = null;
	private String m_branchFile = null;
	private String m_trunkRoot = null;
	private String m_branchRoot = null;
	private String m_summaryReportFile = null;
	private String m_userReportsDir = null;
	private String m_usersArray = null;
	private String m_usersCountProperty = null;
	private String m_summaryReportXslt = null;
	private String m_userReportXslt = null;

	public void setTrunkLogFile(String file)
	{
		m_trunkFile = file;
	}

	public void setBranchLogFile(String file)
	{
		m_branchFile = file;
	}

	public void setTrunkRoot(String root)
	{
		m_trunkRoot = root;
	}

	public void setBranchRoot(String root)
	{
		m_branchRoot = root;
	}

	public void setSummaryReport(String file)
	{
		m_summaryReportFile = file;
	}

	public void setUserReportsDir(String dir)
	{
		m_userReportsDir = dir;
	}

	public void setUsersArray(String array)
	{
		m_usersArray = array;
	}

	public void setSummaryReportXsltStyle(String file)
	{
		m_summaryReportXslt = file;
	}

	public void setUserReportXsltStyle(String file)
	{
		m_userReportXslt = file;
	}

	public void setUsersCountProperty(String property)
	{
		m_usersCountProperty = property;
	}

	public void doWork() throws BuildException
	{
		validate();
		initialize();

		LogParser parser = new LogParser();

		LogReport branchReport = parser.parseLog(getFileFullPath(m_branchFile));
		branchReport.removeEntriesNotStartedWithPath(m_branchRoot);
		branchReport.removeEntriesWithPath(m_branchRoot);
		branchReport.removeEntriesStartedWithPath(m_trunkRoot);
		MergeHelper.removeMutuallyExclusivePathEntriesFromBranchReport(branchReport);
		branchReport.removeNotLastModifications();

		if (branchReport.isEmpty())
		{
			this.log("No changes in branch log");
			return;
		}

		LogReport trunkReport = parser.parseLog(getFileFullPath(m_trunkFile));
		trunkReport.removeLogEntriesWithRevisionLowerThan(branchReport.getMinLogEntryRevision());

		List<String> authors = branchReport.getAuthors();

		LogReport summaryReport = new LogReport();

		int usersCount = 0;

		for (String author : authors)
		{
			LogReport authorReport = MergeHelper.getUnmergedChangesReport(trunkReport, branchReport, m_trunkRoot, m_branchRoot, author);
			if (authorReport == null || authorReport.isEmpty())
			{
				continue;
			}

			String authorReportFile = m_userReportsDir != null ? m_userReportsDir.trim().replace("\\", "/") : "";
			authorReportFile = authorReportFile.endsWith("/") ? authorReportFile : authorReportFile + "/";
			authorReportFile = authorReportFile + author + ".html";
			authorReportFile = getFileFullPath(authorReportFile);

			XSLTProcess xsltTask = new XSLTProcess();
			this.initDelegateTask(xsltTask);
			xsltTask.setStyle(m_userReportXslt);

			authorReport.writeReport(authorReportFile, xsltTask);

			summaryReport.union(authorReport);

			if (m_usersArray != null)
			{
				ArrayManager.addArrayElement(m_usersArray, author, true);
			}

			usersCount++;
		}

		if (m_usersCountProperty != null)
		{
			this.getProject().setProperty(m_usersCountProperty, Integer.toString(usersCount));
		}

		if (summaryReport.isEmpty())
		{
			return;
		}

		XSLTProcess xsltTask = new XSLTProcess();
		this.initDelegateTask(xsltTask);
		xsltTask.setStyle(m_summaryReportXslt);

		summaryReport.writeReport(getFileFullPath(m_summaryReportFile), xsltTask);
	}

	private void validate()
	{
		if (m_trunkFile == null)
		{
			throw new BuildException("Trunk log file should be specified");
		}

		if (m_branchFile == null)
		{
			throw new BuildException("Branch log file should be specified");
		}

		if (m_trunkRoot == null)
		{
			throw new BuildException("Trunk root path is not specified");
		}

		if (m_branchRoot == null)
		{
			throw new BuildException("Branch root path is not specified");
		}

		if (m_summaryReportFile == null)
		{
			throw new BuildException("Summary report file should be specified");
		}

		if (m_summaryReportXslt == null)
		{
			throw new BuildException("Summary report XSLT file should be specified");
		}

		if (m_userReportXslt == null)
		{
			throw new BuildException("User report XSLT file should be specified");
		}
	}

	private void initialize()
	{
		if (m_usersArray != null)
		{
			ArrayManager.initArray(m_usersArray, new LinkedList());
		}

		if (m_usersCountProperty != null)
		{
			this.getProject().setProperty(m_usersCountProperty, "0");
		}
	}
}
