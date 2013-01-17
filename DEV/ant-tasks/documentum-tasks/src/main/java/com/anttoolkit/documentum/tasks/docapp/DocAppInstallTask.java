package com.anttoolkit.documentum.tasks.docapp;

import com.anttoolkit.documentum.common.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;

import com.documentum.ApplicationInstall.*;
import com.documentum.fc.common.*;

import java.io.*;

public class DocAppInstallTask
		extends GenericDocbaseTask
{
	private static final String LOG_FILE_SUFFIX = "_installerLog.html"; 

	public void setDocAppName(String name)
	{
		m_docAppName = name;
	}

	public void setZipFile(String file)
	{
		m_zipFile = file;
	}

	public void setFolder(String folder)
	{
		m_folder = folder;
	}

	public void setLogFile(String file)
	{
		m_logFile = file;
	}

	public void doWork()
			throws BuildException
	{
		if (m_docAppName == null)
		{
			throw new BuildException("docAppName is mandatory attribute");
		}

		if (m_zipFile == null && m_folder == null)
		{
			throw new BuildException("zipFile or folder attribute should be specified");
		}

		if (m_zipFile != null && m_folder != null)
		{
			throw new BuildException("zipFile and folder attributes couldn't be specified " +
					"simultaneously, only one of them should be specified");
		}

		//calculate paths
		String fullLogFileName = getFileFullPath(m_logFile == null ? m_docAppName : m_logFile);
		String docAppDirectory = m_folder;

		Delete deleteTask = new Delete();
		if (m_zipFile != null)
		{
			String fullZipFileName = getFileFullPath(m_zipFile);
			int index = fullZipFileName.lastIndexOf(File.separatorChar);
			String fullZipFilePath = fullZipFileName.substring(0, index);
			docAppDirectory = fullZipFilePath + File.separatorChar + m_docAppName;

			//delete DocApp old local directory if exists
			this.initDelegateTask(deleteTask);
			deleteTask.setDir(new File(docAppDirectory));
			deleteTask.perform();

			//create new local directory
			Mkdir mkdirTask = new Mkdir();
			this.initDelegateTask(mkdirTask);
			mkdirTask.setDir(new File(docAppDirectory));
			mkdirTask.perform();

			//unzip archive to local directory
			Expand expandTask = new Expand();
			this.initDelegateTask(expandTask);
			expandTask.setSrc(new File(fullZipFileName));
			expandTask.setDest(new File(docAppDirectory));
			expandTask.setOverwrite(true);
			expandTask.perform();
		}

		LoginInfo loginInfo = DocbaseSessionManager.getCurrentSessionContext();

		//install DocApp to repository
		DfAppInstaller appInstaller = new DfAppInstaller();
		String propertyFile = null;
		String args[] = {"-d", loginInfo.getDocbase(), "-n", loginInfo.getLogin(), "-p", loginInfo.getPassword(),
			"-m", loginInfo.getDomain(), "-a", docAppDirectory, "-l", fullLogFileName,
			"-f", propertyFile};

		DfAppInstallerCommandLine commandLineAppInstaller = new DfAppInstallerCommandLine(args);
		if(commandLineAppInstaller.processArgumentList())
		{
			appInstaller.connectionInfo(loginInfo.getDocbase(), loginInfo.getDomain(), loginInfo.getLogin(), loginInfo.getPassword());
			appInstaller.logFile(commandLineAppInstaller.getLogFileName(), commandLineAppInstaller.getLogFileLocation());
			appInstaller.appFile(commandLineAppInstaller.getApplicationFileName());
			appInstaller.setPropertiesFile(commandLineAppInstaller.getPropertiesFile());

			try
			{
				appInstaller.startInstall(false);
			}
			catch (DfException e)
			{
				throw new BuildException("Documentum server exception occured\r\n" + e.toString());
			}
		}
		else
		{
			throw new BuildException(DfAppInstaller.getUsageString());
		}

		//rename log file
		if (m_logFile != null)
		{
			Move moveTask = new Move();
			this.initDelegateTask(moveTask);
			moveTask.setFile(new File(fullLogFileName + LOG_FILE_SUFFIX));
			moveTask.setTofile(new File(fullLogFileName));
			moveTask.perform();
		}

		//delete local directory
		if (m_zipFile != null)
		{
			deleteTask.perform();
		}
	}

	private String m_docAppName = null;
	private String m_zipFile = null;
	private String m_folder = null;
	private String m_logFile = null;
}
