package bof.SimpleModules.Lifecycle;

import com.documentum.fc.client.*;
import com.documentum.fc.lifecycle.*;
import com.documentum.fc.common.DfException;

public class TestLifecycle
		implements IDfModule,
		IDfLifecycleUserAction
{
	public void userAction(IDfSysObject sysObj, String userName, String stateName)
			throws DfException
	{
		String name = sysObj.getObjectName() + "#" + userName + "#" + stateName;
		sysObj.setObjectName(name);
		sysObj.save();
	}
}
