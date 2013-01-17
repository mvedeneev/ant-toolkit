package com.anttoolkit.general.tasks.concurrent;

import org.apache.tools.ant.*;

import com.anttoolkit.general.tasks.concurrent.util.*;
import com.anttoolkit.common.*;

public class CreateCyclicBarrierTask
		extends GenericTask
{
	private String m_barrierName = null;
	private int m_parties = -1;

	public void setName(String name)
	{
		m_barrierName = name;
	}

	public void setParties(int parties)
	{
		m_parties = parties;
	}

	public void doWork() throws BuildException
	{
		if (m_barrierName == null)
		{
			throw new BuildException("Barrier name should be specified");
		}

		if (m_parties <= 0)
		{
			throw new BuildException("Parties value should be specified greater than zero");
		}

		ThreadManager.createCyclicBarrier(m_barrierName, m_parties);
	}
}
