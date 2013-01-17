package com.anttoolkit.documentum.tasks.docapp;

import com.anttoolkit.documentum.common.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;

import com.documentum.fc.common.*;
import com.documentum.ApplicationManager.*;

import java.io.*;

public class DocAppArchiveTask
		extends GenericDocbaseTask
{
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

		try
		{
			//calculate paths
			String fullZipFileName = null;
			String docAppDirectory;
			String docAppParentDirectory;

			if (m_zipFile != null)
			{
				fullZipFileName = getFileFullPath(m_zipFile);
				int index = fullZipFileName.lastIndexOf(File.separatorChar);
				docAppParentDirectory = fullZipFileName.substring(0, index);
				docAppDirectory = docAppParentDirectory + File.separatorChar + m_docAppName;
			}
			else
			{
				docAppParentDirectory = m_folder;
				docAppDirectory = m_folder  + File.separatorChar + m_docAppName;
			}

			//delete DocApp old local directory if exists
			Delete deleteTask = new Delete();
			this.initDelegateTask(deleteTask);
			deleteTask.setDir(new File(docAppDirectory));
			deleteTask.perform();

			//create DocApp archive into the local directory
			IDfApplication application = DfAppUtils.findFromApplicationName(this.getSession().getDfSession(), m_docAppName);
			application.makeExternalApplication(docAppParentDirectory);

			if (m_zipFile == null)
			{
				return;
			}

			//create zip archive
			Zip zipTask = new Zip();
			this.initDelegateTask(zipTask);
			zipTask.setBasedir(new File(docAppDirectory));
			zipTask.setDestFile(new File(fullZipFileName));
			zipTask.setUpdate(true);
			zipTask.perform();

			//delete DocApp local directory
			deleteTask.perform();
		}
		catch (DfException e)
		{
			throw new BuildException("Documentum server exception occured\r\n" + e.toString());
		}
	}

	private String m_docAppName = null;
	private String m_zipFile = null;
	private String m_folder = null;
}
