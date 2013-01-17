package bof.tbo.implementation;

import bof.tbo.interfaces.*;
import bof.library.interfaces.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

public class Test_2
		extends DfPersistentObject
		implements ITest_2,
		IDfBusinessObject,
		IDfModule,
		IDfDynamicInheritance
{
	protected void doSave (boolean saveLock, String versionLabel, Object[] extendedArgs)
			throws DfException
	{
		registrate("A");
		super.doSave(saveLock, versionLabel, extendedArgs);
	}

	public IStringGenerator getGenerator()
			throws DfException
	{
		try
		{
			Class genClass = Class.forName("bof.library.implementation.StringGenerator_2");
			return (IStringGenerator)genClass.newInstance();
		}
		catch (Exception e)
		{
			throw new DfException(e);
		}
	}

	public void registrate(String number)
			throws DfException
	{
		String name = this.getString("object_name");
		name = name == null ? "" : name;
		setString("object_name", name + "_REG_" + number + "_" + this.getGenerator().getSampleText());
	}

	public String getVersion()
	{
		return "1.0";
	}

	public String getVendorString()
	{
		return "IRUDYAK Test_2";
	}

	public boolean isCompatible(String string)
	{
		return false;
	}

	public boolean supportsFeature(String string)
	{
		return false;
	}
}
