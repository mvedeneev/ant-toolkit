package com.anttoolkit.general.props;

import org.apache.tools.ant.*;
import org.apache.tools.ant.property.*;

import java.text.*;

public class CompoundPropertyHelper
		extends ThreadLocalPropertyHelper
		implements PropertyExpander
{
	private static final CompoundPropertyHelper INSTANCE = new CompoundPropertyHelper();

 /**
  * Parse the next property name.
  * @param value the String to parse.
  * @param pos the ParsePosition in use.
  * @param parseNextProperty parse next property
  * @return parsed String if any, else <code>null</code>.
  */
	public String parsePropertyName(String value, ParsePosition pos, ParseNextProperty parseNextProperty)
	{
		int start = pos.getIndex();

		if (value.length() - start >= 3 && '$' == value.charAt(start) && '{' == value.charAt(start + 1))
		{
			parseNextProperty.getProject().log("Attempting nested property processing", Project.MSG_DEBUG);
			pos.setIndex(start + 2);

			StringBuffer sb = new StringBuffer();

			for (int c = pos.getIndex(); c < value.length(); c = pos.getIndex())
			{
				if (value.charAt(c) == '}')
				{
					pos.setIndex(c + 1);
					return sb.toString();
				}

				Object o = parseNextProperty.parseNextProperty(value, pos);

				if (o != null)
				{
					sb.append(o);
				}
				else
				{
					// be aware that the parse position may now have changed;
					// update:
					c = pos.getIndex();
					sb.append(value.charAt(c));
					pos.setIndex(c + 1);
				}
			}
		}

		pos.setIndex(start);

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		return obj == this || obj instanceof CompoundPropertyHelper && obj.hashCode() == hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode()
	{
		if (CompoundPropertyHelper.class.equals(getClass()))
		{
			return System.identityHashCode(INSTANCE);
		}

		throw new UnsupportedOperationException("Get your own hashCode implementation!");
	}
}
