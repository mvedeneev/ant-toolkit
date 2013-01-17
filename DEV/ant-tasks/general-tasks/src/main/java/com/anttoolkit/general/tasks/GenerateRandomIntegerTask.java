package com.anttoolkit.general.tasks;

import java.util.*;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;


public class GenerateRandomIntegerTask
	extends GenericTask
{
	private static final Random RANDOM = new Random(System.currentTimeMillis());

	private int m_min = 0;
	private int m_max = Integer.MAX_VALUE;
	private String m_property = null;

	public static int getRandomInt(int min, int max)
	{
		if (min < 0)
		{
			throw new BuildException("Min boundary can't be less than 0");
		}

		if (max <= min)
		{
			throw new BuildException("Max boundary can't be less or equal to min range");
		}

		int range = max - min + 1;

		return RANDOM.nextInt(range) + min;
	}

	public void setMin(int min)
	{
		m_min = min;
	}

	public void setMax(int max)
	{
		m_max = max;
	}

	public void setPropertyName(String property)
	{
		m_property = property;
	}

	public void doWork() throws BuildException
	{
		if (m_property == null)
		{
			throw new BuildException("Property name is not specified");
		}

		int randomValue = getRandomInt(m_min, m_max);

		//getProject().setProperty(m_property, Integer.toString(randomValue));

		PropertyHelper helper = PropertyHelper.getPropertyHelper(getProject());
		helper.setProperty(m_property, Integer.toString(randomValue), false);

	}
}
