package com.anttoolkit.documentum.tasks.usergroup;

import org.apache.tools.ant.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

public class RemoveFromGroupTask
		extends GenericGroupTask
{
	public void doWork()
			throws BuildException
	{
		super.doWork();

		IDfGroup group =  this.getRootGroup();

		String[] users = this.getUsers();
		for (int i = 0; i < users.length; i++)
		{
			try
			{
				if (group.isUserInGroup(users[i]))
				{
					group.removeUser(users[i]);
				}
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to remove user=" + users[i] + " from group=" +
						this.getRootGroupName()  + "\r\n" + e.toString());
			}
		}

		String[] groups = this.getGroups();
		for (int i = 0; i < groups.length; i++)
		{
			try
			{
				if (group.isGroupInGroup(groups[i]))
				{
					group.removeGroup(groups[i]);
				}
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to remove group=" + groups[i] + " from group=" +
						this.getRootGroupName()  + "\r\n" + e.toString());
			}
		}

		this.saveRootGroup(group);
	}
}
