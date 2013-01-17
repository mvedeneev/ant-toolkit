package com.anttoolkit.documentum.tasks.build;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.xml.sax.*;
import org.w3c.dom.*;
import org.apache.tools.ant.*;

import com.anttoolkit.common.*;


public class BuildFileValidatorTask extends GenericTask
{
	private static final String NUMBER_ATTR = "number";

	private String m_file;
	private String m_xPath;

	public void setFile(String file)
	{
		m_file = file;
	}

	public void setXPath(String xpath)
	{
		m_xPath = xpath;
	}

	public void doWork() throws BuildException
	{
		validate();

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.parse(getFileFullPath(m_file));

			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile(m_xPath);

			NodeList list = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
			if (list == null || list.getLength() == 0)
			{
				return;
			}

			Set<String> processedNumbers = new HashSet<String>();
			int[] prevBuildNumberParts = null;
			int count = list.getLength();

			for (int i = 0; i < count; i++)
			{
				Element node = (Element)list.item(i);
				String buildNumber = node.getAttribute(NUMBER_ATTR).trim();

				if (processedNumbers.contains(buildNumber))
				{
					throw new BuildException("There are several build containers with the same build number: " + buildNumber);
				}

				int[] buildNumberParts = getBuildNumberParts(buildNumber);

				if (i != 0 &&
					(prevBuildNumberParts[0] > buildNumberParts[0] ||
					(prevBuildNumberParts[0] == buildNumberParts[0] &&
					prevBuildNumberParts[1] > buildNumberParts[1])))
				{
					throw new BuildException("Incorrect build containers sequence, build with number " +
							buildNumberParts[0] + "." + buildNumberParts[1] + " should follow before build " +
							prevBuildNumberParts[0] + "." + prevBuildNumberParts[1]);
				}

				processedNumbers.add(buildNumber);
				prevBuildNumberParts = buildNumberParts;
			}
		}
		catch (ParserConfigurationException e)
		{
			throw new BuildException("Failed to configure XML DOM parser", e);
		}
		catch (SAXException e)
		{
			throw new BuildException("Failed to parse specified xml file: " + m_file, e);
		}
		catch (IOException e)
		{
			throw new BuildException("Failed to parse specified xml file: " + m_file, e);
		}
		catch (XPathExpressionException e)
		{
			throw new BuildException("Failed to compile specified XPath expression: " + m_xPath, e);
		}
	}

	private void validate()
	{
		if (m_file == null)
		{
			throw new BuildException("File name should be specified");
		}

		File file = new File(getFileFullPath(m_file));
		if (!file.exists() || !file.isFile())
		{
			throw new BuildException("Specified file \"" + m_file + "\" doesn't exist");
		}

		if (m_xPath == null)
		{
			throw new BuildException("XPath should be specified");
		}
	}

	private int[] getBuildNumberParts(String buildNumber)
	{
		if (buildNumber == null || buildNumber.trim().length() == 0)
		{
			throw new BuildException("Build number couldn't be empty");
		}

		String[] parts = buildNumber.split("\\.");
		if (parts.length != 2)
		{
			throw new BuildException("Incorrect build number specified: " + buildNumber);
		}

		try
		{
			return new int[] {Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
		}
		catch (NumberFormatException e)
		{
			throw new BuildException("Incorrect build number specified: " + buildNumber);
		}
	}

}
