package com.anttoolkit.documentum.tasks.workflow.util;

public class StartActivityInfo
{
	StartActivityInfo(String activityName, String inputPortName,
					  String packageName, String packageType)
	{
		this.activityName = activityName;
		this.inputPortName = inputPortName;
		this.packageName = packageName;
		this.packageType = packageType;
	}

	public final String activityName;
	public final String inputPortName;
	public final String packageName;
	public final String packageType;
}
