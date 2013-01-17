package com.anttoolkit.svn.tasks;

import com.anttoolkit.common.*;

import org.apache.tools.ant.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.xml.parsers.*;
import java.io.*;
import java.util.regex.*;

public class GetLatestMessageRevisionTask
		extends GenericTask
{
	private class LogHandler
			extends DefaultHandler
	{
		public static final int UNDEFINED_REVISION = -1;

		private static final String LOGENTRY_ELEMENT = "logentry";
		private static final String REVISION_ATTRIBUTE = "revision";
		private static final String MSG_ELEMENT = "msg";

		public LogHandler(String pattern)
		{
			m_matcher = Pattern.compile(pattern).matcher("");
		}

		public void startElement(String namespaceURI,
								 String localName,
								 String qName,
								 Attributes attributes)
				throws SAXException
		{
			if (qName.equals(LOGENTRY_ELEMENT))
			{
				m_currentRevision = parseRevisionNumber(attributes.getValue(REVISION_ATTRIBUTE));
			}
		}

		public void endElement(String namespaceURI,
							   String localName,
							   String qName)
                throws SAXException, BuildException
		{
			try
			{
				if (qName.equals(MSG_ELEMENT))
				{
					m_message = m_characters.toString().trim();
				}

				if (qName.equals(LOGENTRY_ELEMENT))
				{
					m_matcher.reset(m_message);
					if (m_matcher.find() && m_currentRevision > m_revision)
					{
						m_revision = m_currentRevision;
						return;
					}

					m_currentRevision = UNDEFINED_REVISION;
					m_message = null;
				}
			}
			finally
			{
				m_characters.delete(0, m_characters.length());
			}
		}

		public void characters(char[] buffer,
							   int offset,
							   int length)
                throws SAXException
		{
			String text = (new String(buffer, offset, length)).trim();
			if (text.length() == 0)
			{
				return;
			}

			m_characters.append(text);
		}

		public int getRevision()
		{
			return m_revision;
		}

		private int parseRevisionNumber(String number)
		{
			try
			{
				return Integer.parseInt(number);
			}
			catch (NumberFormatException e)
			{
				throw new BuildException("Incorrect revision number in log file: " + number);	
			}
		}

		private Matcher m_matcher;
		
		private int m_currentRevision = UNDEFINED_REVISION;
		private int m_revision = UNDEFINED_REVISION;
		private String m_message;
		private StringBuffer m_characters = new StringBuffer();
	}
	
	public void setLogFile(String file)
	{
		m_logFile = file;
	}

	public void setMessagePattern(String pattern)
	{
		m_pattern = pattern;
	}

	public void setPropertyName(String name)
	{
		m_propertyName = name;
	}

	public void doWork()
			throws BuildException
	{
		verify();

		LogHandler handler = new LogHandler(m_pattern);

		try
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new File(getFileFullPath(m_logFile)), handler);
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

		if (handler.getRevision() != LogHandler.UNDEFINED_REVISION)
		{
			this.getProject().setProperty(m_propertyName, Integer.toString(handler.getRevision()));
		}
	}

	private void verify()
	{
		if (m_logFile == null || m_logFile.trim().length() == 0)
		{
			throw new BuildException("Log file should be specified");
		}

		if (m_pattern == null || m_pattern.trim().length() == 0)
		{
			throw new BuildException("Message pattern should be specified");	
		}

		if (m_propertyName == null || m_propertyName.trim().length() == 0)
		{
			throw new BuildException("Property name should be specified");	
		}
	}

	private String m_logFile;
	private String m_pattern;
	private String m_propertyName;
}
