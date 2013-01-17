package com.anttoolkit.documentum.tasks.build;

import org.apache.tools.ant.*;

public interface IVersionProvider
{
	public String getVersion();
	public boolean outOfTurnUpdate(String newVersion, String currentVersion);
	public boolean canUpdateToVersion(String version);
	public void updateToVersion(String currentVersion, String newVersion);
}
