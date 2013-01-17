package com.anttoolkit.documentum.tasks.audit;

import org.apache.tools.ant.*;

public class UnauditTask
		extends GenericAuditTask
{
	public void doWork()
			throws BuildException
	{
		super.doWork();

		String policyId = this.getPolicyId();
		String[] types = this.getTypeNames();
		String[] events = this.getEventNames();

		for (int i = 0; i < types.length; i++)
		{
			String type = types[i].trim();

			for (int j = 0; j < events.length; j++)
			{
				String event = events[j].trim();
				this.unregisterEventForType(type, event, this.getApplication(), policyId, this.getStateName());
			}
		}
	}
}
