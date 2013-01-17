package com.anttoolkit.svn.tasks.util;

import org.apache.tools.ant.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class LogParser
		extends DefaultHandler
{
	private static final String LOGENTRY_ELEMENT = "logentry";
	private static final String REVISION_ATTRIBUTE = "revision";
	private static final String AUTHOR_ELEMENT = "author";
	private static final String DATE_ELEMENT = "date";
	private static final String MSG_ELEMENT = "msg";
	private static final String PATH_ELEMENT = "path";
	private static final String ACTION_ATTRIBUTE = "action";

	private String m_startElement;

	private List<PathEntry> m_pathEntries = new LinkedList<PathEntry>();

	private String m_revision;
	private String m_author;
	private String m_date;
	private String m_action;
	private String m_message;
	private StringBuffer m_characters;

	private LogReport m_logReport;

	public LogReport parseLog(String logFile)
	{
		init();

		try
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new File(logFile), this);
		}
		catch (SAXException e)
		{
			throw new BuildException("Failed to parse file: " + logFile, e);
		}
		catch (ParserConfigurationException e)
		{
			throw new BuildException("Failed to parse file: " + logFile, e);
		}
		catch (IOException e)
		{
			throw new BuildException("Failed to parse file: " + logFile, e);
		}

		return m_logReport;
	}

	public void startElement(String namespaceURI,
							 String localName,
							 String qName,
							 Attributes attributes)
			throws SAXException
	{
		this.m_startElement = qName;

		if (m_startElement.equals(LOGENTRY_ELEMENT))
		{
			m_revision = attributes.getValue(REVISION_ATTRIBUTE);
		}

		if (m_startElement.equals(PATH_ELEMENT))
		{
			m_action = attributes.getValue(ACTION_ATTRIBUTE);
		}
	}

	public void endElement(String namespaceURI,
						   String localName,
						   String qName)
               throws SAXException, BuildException
	{
		try
		{
			if (qName.equals(AUTHOR_ELEMENT))
			{
				m_author = m_characters.toString();
			}

			if (qName.equals(DATE_ELEMENT))
			{
				m_date = m_characters.toString();
			}

			if (qName.equals(PATH_ELEMENT))
			{
				m_pathEntries.add(new PathEntry(m_characters.toString(), m_action));
				m_action = null;
			}

			if (qName.equals(MSG_ELEMENT))
			{
				m_message = m_characters.toString().trim();
				m_message = m_message == null || m_message.length() == 0 ? "" : m_message;
			}

			if (qName.equals(LOGENTRY_ELEMENT))
			{
				m_logReport.addLogEntry(new LogEntry(m_author, m_date, m_message, m_revision, m_pathEntries));

				m_revision = null;
				m_author = null;
				m_date = null;
				m_message = null;
				m_pathEntries = new LinkedList<PathEntry>();
			}
		}
		finally
		{
			if (qName.equals(LOGENTRY_ELEMENT) || qName.equals(AUTHOR_ELEMENT) ||
				qName.equals(DATE_ELEMENT) || qName.equals(MSG_ELEMENT) ||
				qName.equals(PATH_ELEMENT))
			{
				m_startElement = "";
			}

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

	private void init()
	{
		m_startElement = null;

		m_pathEntries = new LinkedList<PathEntry>();

		m_revision = null;
		m_author = null;
		m_date = null;
		m_action = null;
		m_message = null;
		m_characters = new StringBuffer();

		m_logReport = new LogReport();
	}
}
