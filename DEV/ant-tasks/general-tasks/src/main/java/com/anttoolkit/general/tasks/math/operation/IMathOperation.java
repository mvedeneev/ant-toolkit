package com.anttoolkit.general.tasks.math.operation;

public interface IMathOperation
{
	public boolean isValidOperation(String operation);

	public String execute(String arg1, String arg2);
}
