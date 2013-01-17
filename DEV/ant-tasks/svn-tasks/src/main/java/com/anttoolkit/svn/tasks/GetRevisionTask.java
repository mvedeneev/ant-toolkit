package com.anttoolkit.svn.tasks;

import com.anttoolkit.common.*;

import org.apache.tools.ant.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import java.io.*;

public class GetRevisionTask
		extends GenericTask
{
	private static final String REVISION_TYPE_HEAD = "HEAD";
	private static final String REVISION_TYPE_COMMITTED = "COMMITTED";

	private class InfoHandler
			extends DefaultHandler
	{
		private static final String ENTRY_NODE = "entry";
		private static final String COMMIT_NODE = "commit";
		private static final String REVISION_ATTRIBUTE = "revision";

		public void startElement(String namespaceURI,
								 String localName,
								 String qName,
								 Attributes attributes)
				throws SAXException
		{
			if (qName.equals(COMMIT_NODE))
			{
				 m_commitRevision = attributes.getValue(REVISION_ATTRIBUTE);
			}

			if (qName.equals(ENTRY_NODE))
			{
				m_headRevision = attributes.getValue(REVISION_ATTRIBUTE);
			}
		}

		public String getCommitRevision()
		{
			return m_commitRevision;
		}

		public String getHeadRevision()
		{
			return m_headRevision;	
		}

		private String m_commitRevision;
		private String m_headRevision;
	}

	public void setRevisionType(String type)
	{
		m_revisionType = type.toUpperCase();
	}

	public void setInfoFile(String file)
	{
		m_infoFile = file;
	}

	public void setPropertyName(String name)
	{
		m_propertyName = name;
	}

	public void doWork()
			throws BuildException
	{
		verify();

		InfoHandler handler = new InfoHandler();

		try
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new File(getFileFullPath(m_infoFile)), handler);
		}
		catch (SAXException e)
		{
			throw new BuildException(e);
		}
		catch (ParserConfigurationException e)
		{
			throw new BuildException(e);
		}
		catch (IOException e)
		{
			throw new BuildException(e);
		}
		
		this.getProject().setProperty(m_propertyName,
				REVISION_TYPE_HEAD.equals(m_revisionType) ? handler.getHeadRevision() : handler.getCommitRevision());
	}

	private void verify()
	{
		if (m_infoFile == null || m_infoFile.trim().length() == 0)
		{
			throw new BuildException("Info file doesn't specified");
		}

		if (m_propertyName == null || m_propertyName.trim().length() == 0)
		{
			throw new BuildException("Property name doesn't specified");	
		}

		if (m_revisionType == null || m_revisionType.trim().length() == 0)
		{
			throw new BuildException("Revision type doesn't specified");	
		}

		if (!REVISION_TYPE_HEAD.equals(m_revisionType) &&
			!REVISION_TYPE_COMMITTED.equals(m_revisionType))
		{
			throw new BuildException("Invalid revision type: " + m_revisionType);	
		}
	}
	
	private String m_infoFile;
	private String m_propertyName;
	private String m_revisionType = REVISION_TYPE_HEAD;
}
