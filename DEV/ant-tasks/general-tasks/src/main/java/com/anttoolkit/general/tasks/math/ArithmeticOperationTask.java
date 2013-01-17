package com.anttoolkit.general.tasks.math;

import org.apache.tools.ant.*;

import com.anttoolkit.common.*;
import com.anttoolkit.general.tasks.math.operation.*;

public class ArithmeticOperationTask
		extends GenericTask
{
	private static IMathOperation[] m_supportedOperations = {DivideOperation.instance,
			MultiplyOperation.instance, SubstractOperation.instance, SumOperation.instance};

	private String m_arg1 = null;
	private String m_arg2 = null;
	private String m_operation = null;
	private String m_resultProperty = null;

	public void setArg1(String value)
	{
		m_arg1 = value;
	}

	public void setArg2(String value)
	{
		m_arg2 = value;
	}

	public void setOperation(String operation)
	{
		m_operation = operation;
	}

	public void setResultProperty(String property)
	{
		m_resultProperty = property;
	}

	public void doWork()
			throws BuildException
	{
		validate();

		for (IMathOperation operation : m_supportedOperations)
		{
			if (operation.isValidOperation(m_operation))
			{
				this.getProject().setProperty(m_resultProperty, operation.execute(m_arg1, m_arg2));
				return;
			}
		}

		throw new BuildException("Unsupported arithmetic operation specified: " + m_operation);
	}

	private void validate()
	{
		if (m_arg1 == null)
		{
			throw new BuildException("arg1 is not specified");
		}

		if (m_arg2 == null)
		{
			throw new BuildException("arg2 is not specified");
		}

		if (m_operation == null)
		{
			throw new BuildException("Operation is not specified");
		}

		if (m_resultProperty == null)
		{
			throw new BuildException("Result property is not specified");
		}
	}
}
