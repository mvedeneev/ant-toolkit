package com.anttoolkit.documentum.tasks.object;

import com.anttoolkit.documentum.common.*;

import org.apache.tools.ant.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import java.io.*;

public class DocbaseObjectTask
		extends GenericDocbaseTask
{
	private class BatchHandler
			extends DefaultHandler

	{
		private static final String OBJECTS_ELEMENT = "objects";
		private static final String OBJECT_ELEMENT = "docbaseObject";
		private static final String PROPERTY_ELEMENT = "property";

		private static final String TYPE_ATTRIBUTE = "type";
		private static final String OBJECT_ID_ATTRIBUTE = "objectId";
		private static final String FOLDER_ATTRIBUTE = "folder";

		private static final String NAME_ATTRIBUTE = "name";
		private static final String VALUE_ATTRIBUTE = "value";
		private static final String QUERY_ATTRIBUTE = "query";
		private static final String FORMAT_ATTRIBUTE = "format";

		public void startElement(String namespaceURI,
								 String localName,
								 String qName,
								 Attributes attributes)
				throws SAXException
		{
			if (!qName.equals(OBJECTS_ELEMENT) &&
				!qName.equals(OBJECT_ELEMENT) &&
				!qName.equals(PROPERTY_ELEMENT))
			{
				throw new SAXException("Element \"" + qName + "\" doesn't allowed in objects xml file");
			}

			if (qName.equals(OBJECT_ELEMENT))
			{
				clearDocbaseObject();
				setDocbaseObjectAttributes(attributes);
				return;
			}

			if (qName.equals(PROPERTY_ELEMENT))
			{
				addDocbaseObjectProperty(attributes);
			}
		}

		public void endElement(String namespaceURI,
							   String localName,
							   String qName)
				throws SAXException, BuildException
		{
			if (qName.equals(OBJECT_ELEMENT))
			{
				executeSingleObject();
				clearDocbaseObject();
			}
		}

		private void setDocbaseObjectAttributes(Attributes attributes)
				throws SAXException
		{
			int count = attributes.getLength();
			for (int i = 0; i < count; i++)
			{
				String name = attributes.getQName(i);
				String value = DocbaseObjectTask.this.substituteProperties(attributes.getValue(i));

				if (name.equals(TYPE_ATTRIBUTE))
				{
					setType(value);
				}
				else if (name.equals(OBJECT_ID_ATTRIBUTE))
				{
					setObjectId(value);
				}
				else if (name.equals(FOLDER_ATTRIBUTE))
				{
					setFolder(value);
				}
				else
				{
					throw new SAXException("Attribute \"" + name + "\" doesn't allowed for object element in objects xml file");
				}
			}
		}

		private void addDocbaseObjectProperty(Attributes attributes)
				throws SAXException
		{
			DocbaseObjectProperty property = new DocbaseObjectProperty();

			int count = attributes.getLength();
			for (int i = 0; i < count; i++)
			{
				String name = attributes.getQName(i);
				String value = DocbaseObjectTask.this.substituteProperties(attributes.getValue(i));

				if (name.equals(NAME_ATTRIBUTE))
				{
					property.setName(value);
				}
				else if (name.equals(VALUE_ATTRIBUTE))
				{
					property.setValue(value);
				}
				else if (name.equals(QUERY_ATTRIBUTE))
				{
					property.setQuery(value);
				}
				else if (name.equals(FORMAT_ATTRIBUTE))
				{
					property.setFormat(value);
				}
				else
				{
					throw new SAXException("Attribute \"" + name + "\" doesn't allowed for property element in objects xml file");
				}
			}

			addConfiguredProperty(property);
		}
	}

	public void setType(String type)
	{
		m_docbaseObject.setType(type);
	}

	public String getType()
	{
		return m_docbaseObject.getType();	
	}

	public void setObjectId(String objectId)
	{
		m_docbaseObject.setObjectId(objectId);
	}

	public void setFolder(String folder)
	{
		m_docbaseObject.setFolder(folder);
	}

	public void setContentFile(String file)
	{
		if (file != null && !file.trim().toLowerCase().equals("null"))
		{
			m_docbaseObject.setContentFile(getFileFullPath(file));
		}
		else
		{
			m_docbaseObject.setContentFile(file);
		}
	}

	public void setContentType(String type)
	{
		if (type != null)
		{
			m_docbaseObject.setContentType(type);
		}
	}

	public void setNewObjectIdProperty(String propertyName)
	{
		m_newObjectIdProperty = propertyName;
	}

	public void setFile(String file)
	{
		m_file = file;
	}

	public void addConfiguredProperty(DocbaseObjectProperty property)
	{
		m_docbaseObject.addProperty(property);
	}

	public DocbaseObjectProperty getProperty(String name)
	{
		return m_docbaseObject.getProperty(name);
	}

	public void doWork()
			throws BuildException
	{
		if (m_docbaseObject.getType() == null &&
			m_docbaseObject.getObjectId() == null &&
			m_file == null)
		{
			throw new BuildException("objectId or type or file attribute should be specified");
		}

		m_docbaseObject.setSession(this.getSession());

		executeSingleObject();

		if (m_file != null)
		{
			executeObjectsFromFile();
		}
	}

	protected void setCheckinOperationFlag()
	{
		m_docbaseObject.setCheckinOperationFlag();
	}

	private void executeSingleObject()
			throws BuildException
	{
		if (m_docbaseObject.getType() == null &&
			m_docbaseObject.getObjectId() == null &&
			m_file != null)
		{
			return;
		}

		m_docbaseObject.doWork();

		if (m_docbaseObject.getType() != null &&
			m_newObjectIdProperty != null)
		{
			this.getProject().setProperty(m_newObjectIdProperty, m_docbaseObject.getLastCommitedObjectId());
		}
	}

	private void executeObjectsFromFile()
			throws BuildException
	{
		try
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new File(getFileFullPath(m_file)), new BatchHandler());
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
	}

	private void clearDocbaseObject()
	{
		m_docbaseObject.clear();
	}

	private String m_file = null;
	private DocbaseObject m_docbaseObject = new DocbaseObject();
	private String m_newObjectIdProperty = null;
}
