package com.anttoolkit.svn.tasks;

import com.anttoolkit.common.*;

import com.anttoolkit.svn.tasks.util.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class LogReportTask
		extends GenericTask
{
	private String m_logFile;
	private String m_reportFile;
	private String m_xsltStyle;

	public void setLogFile(String file)
	{
		m_logFile = file;
	}

	public void setReportFile(String file)
	{
		m_reportFile = file;
	}

	public void setXsltStyle(String file)
	{
		m_xsltStyle = file;
	}

	public void doWork()
			throws BuildException
	{
		if (m_xsltStyle == null)
		{
			throw new BuildException("XSLT style sheet is not specified");
		}

		if (m_reportFile == null)
		{
			throw new BuildException("Report file is not specified");
		}

		if (m_logFile == null)
		{
			throw new BuildException("SVN xml log file is not specified");
		}

		LogParser parser = new LogParser();
		LogReport report = parser.parseLog(getFileFullPath(m_logFile));

		XSLTProcess xsltTask = new XSLTProcess();
		this.initDelegateTask(xsltTask);
		xsltTask.setStyle(m_xsltStyle);

		report.writeReport(m_reportFile, xsltTask);
	}
}
