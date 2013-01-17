package com.anttoolkit.documentum.tasks.usergroup;

import org.apache.tools.ant.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

public class AddToGroupTask
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
				if (!group.isUserInGroup(users[i]))
				{
					group.addUser(users[i]);
				}
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to add user=" + users[i] + " to group=" +
						this.getRootGroupName()  + "\r\n" + e.toString());
			}
		}

		String[] groups = this.getGroups();
		for (int i = 0; i < groups.length; i++)
		{
			try
			{
				if (!group.isGroupInGroup(groups[i]))
				{
					group.addGroup(groups[i]);
				}
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to add group=" + groups[i] + " to group=" +
						this.getRootGroupName()  + "\r\n" + e.toString());
			}
		}

		this.saveRootGroup(group);
	}
}
