package com.anttoolkit.documentum.tasks.build;

import com.anttoolkit.documentum.common.*;

import com.anttoolkit.documentum.tasks.build.util.BuildVersionManager;
import org.apache.tools.ant.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

import java.text.*;

public class DocbaseVersionProvider
		implements IVersionProvider
{
	private static final String TYPE_NOT_EXIST_ERROR = "DM_QUERY_E_REG_TABLE_QUAL";
	private static final String TABLE_NOT_FOUND_ERROR = "DM_QUERY2_E_TABLE_NOT_FOUND";

	private static final String VERSION_TABLE = "dt_docbase_version";
	private static final String HISTORY_TABLE = "dt_docbase_version_history";

	//common attributes
	private static final String MAJOR_ATTR = "si_major";
	private static final String MINOR_ATTR = "si_minor";
	private static final String DATE_ATTR = "sdt_date";
	private static final String QUALIFIER_ATTR = "ss_qualifier";
	private static final String DOCBASE_USER_ATTR = "ss_docbase_user";
	private static final String OS_USER_ATTR = "ss_os_user";
	private static final String HOSTNAME_ATTR = "ss_hostname";
	private static final String IP_ATTR = "ss_ip";

	//history attributes
	private static final String PREV_MAJOR_ATTR = "si_major_prev";
	private static final String PREV_MINOR_ATTR = "si_minor_prev";

	private static final String DQL_CHECK_VERSION_TABLE = "select " + MAJOR_ATTR + ", " +
			MINOR_ATTR + ", " + QUALIFIER_ATTR + ", " + DATE_ATTR + ", " + DOCBASE_USER_ATTR + ", " +
			OS_USER_ATTR + ", " + HOSTNAME_ATTR + ", " + IP_ATTR +
			" from " + VERSION_TABLE + " enable(return_top 1)";

	private static final String DQL_CHECK_HISTORY_TABLE = "select " + MAJOR_ATTR + ", " +
			MINOR_ATTR + ", " + QUALIFIER_ATTR + ", " + DATE_ATTR + ", " + DOCBASE_USER_ATTR + ", " +
			OS_USER_ATTR + ", " + HOSTNAME_ATTR + ", " + IP_ATTR + ", " +  PREV_MAJOR_ATTR + ", " +
			PREV_MINOR_ATTR + " from " + HISTORY_TABLE + " enable(return_top 1)";

	private static final String DQL_CREATE_VERSION_TABLE = "create type \"" + VERSION_TABLE + "\" " +
			"(\"" + MAJOR_ATTR + "\" integer, \"" + MINOR_ATTR + "\" integer, " +
			"\"" + QUALIFIER_ATTR + "\" string(64), \"" + DATE_ATTR + "\" date, " +
			"\"" + DOCBASE_USER_ATTR + "\" string(64), \"" + OS_USER_ATTR + "\" string(64), " +
			"\"" + HOSTNAME_ATTR + "\" string(64), \"" + IP_ATTR + "\" string(64)) " +
			"with supertype null publish";

	private static final String DQL_CREATE_HISTORY_TABLE = "create type \"" + HISTORY_TABLE + "\" " +
			"(\"" + MAJOR_ATTR + "\" integer, \"" + MINOR_ATTR + "\" integer, " +
			"\"" + PREV_MAJOR_ATTR + "\" integer, \"" + PREV_MINOR_ATTR + "\" integer, " +
			"\"" + QUALIFIER_ATTR + "\" string(64), \"" + DATE_ATTR + "\" date, " +
			"\"" + DOCBASE_USER_ATTR + "\" string(64), \"" + OS_USER_ATTR + "\" string(64), " +
			"\"" + HOSTNAME_ATTR + "\" string(64), \"" + IP_ATTR + "\" string(64)) " +
			"with supertype null publish";

	private static final String DQL_CREATE_VERSION_RECORD = "create " + VERSION_TABLE + " object " +
			"set " + MAJOR_ATTR + "=0, " +
			"set " + MINOR_ATTR + "=0, " +
			"set " + QUALIFIER_ATTR + "=''{0}'', " +
			"set " + DATE_ATTR + "=DATE(NOW), " +
			"set " + DOCBASE_USER_ATTR + "=''{1}'', " +
			"set " + OS_USER_ATTR + "=''{2}'', " +
			"set " + HOSTNAME_ATTR + "=''{3}'', " +
			"set " + IP_ATTR + "=''{4}''";

	private static final String DQL_CREATE_HISTORY_RECORD = "create " + HISTORY_TABLE + " object " +
			"set " + MAJOR_ATTR + "={0}, " +
			"set " + MINOR_ATTR + "={1}, " +
			"set " + PREV_MAJOR_ATTR + "={2}, " +
			"set " + PREV_MINOR_ATTR + "={3}, " +
			"set " + QUALIFIER_ATTR + "=''{4}'', " +
			"set " + DATE_ATTR + "=DATE(NOW), " +
			"set " + DOCBASE_USER_ATTR + "=''{5}'', " +
			"set " + OS_USER_ATTR + "=''{6}'', " +
			"set " + HOSTNAME_ATTR + "=''{7}'', " +
			"set " + IP_ATTR + "=''{8}''";

/////////////////////////////////////

	private static final String DQL_UPDATE_VERSION_RECORD = "update " + VERSION_TABLE + " object " +
			"set " + MAJOR_ATTR + "={0}, " +
			"set " + MINOR_ATTR + "={1}, " +
			"set " + DATE_ATTR + "=DATE(NOW), " +
			"set " + DOCBASE_USER_ATTR + "=''{2}'', " +
			"set " + OS_USER_ATTR + "=''{3}'', " +
			"set " + HOSTNAME_ATTR + "=''{4}'', " +
			"set " + IP_ATTR + "=''{5}'' " +
			"where " + MAJOR_ATTR + "=''{6}'' and " +
			MINOR_ATTR + "=''{7}'' and " +
			QUALIFIER_ATTR + " {8}";

	private static final String DQL_GET_DOCBASE_VERSION = "select " + MAJOR_ATTR + ", " + MINOR_ATTR +
			" from " + VERSION_TABLE + " where " + QUALIFIER_ATTR + " {0}";

	private static final String DQL_CHECK_VERSION_HISTORY = "select * from " + HISTORY_TABLE +
			" where " + MAJOR_ATTR + "=''{0}'' and " + MINOR_ATTR + "=''{1}'' and " +
			QUALIFIER_ATTR + " {2}";

	private static final String DQL_CHECK_CAN_UPDATE_TO_VERSION = "select *" +
			" from " + VERSION_TABLE +
			" where (" + MAJOR_ATTR + " < {0} or (" + MAJOR_ATTR + " = {0} and " +
			MINOR_ATTR + " < {1})) and " + QUALIFIER_ATTR + " {2}";

	public boolean outOfTurnUpdate(String newVersion, String currentVersion)
	{
		return compareVersions(newVersion, currentVersion) < 0;
	}

	public boolean canUpdateToVersion(String version)
	{
		//workaround for the situation when version tables or records is not already created
		getVersion();

		int[] parts = parseVersion(version);

		String query = MessageFormat.format(DQL_CHECK_CAN_UPDATE_TO_VERSION,
				Integer.toString(parts[0]),
				Integer.toString(parts[1]),
				getQualifierDqlCondition());

		try
		{
			boolean exist = DqlHelper.exist(DocbaseSessionManager.getSession(), query);
			return exist || !isVersionUpdateHistoryExist(parts[0], parts[1]);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to check possibility to update to version " + version, e);
		}
	}

	public void updateToVersion(String currentVersion, String newVersion)
	{
		//workaround for the situation when version tables or records is not already created
		getVersion();

		boolean outOfTurnUpdate = outOfTurnUpdate(newVersion, currentVersion);

		int[] newNumbers = parseVersion(newVersion);
		int[] currentNumbers = parseVersion(currentVersion);

		if (!outOfTurnUpdate)
		{
			updateVersionRecord(newNumbers[0], newNumbers[1], currentNumbers[0], currentNumbers[1]);
		}

		createHistoryRecord(newNumbers[0], newNumbers[1], currentNumbers[0], currentNumbers[1]);
	}

	private boolean isVersionUpdateHistoryExist(int major, int minor)
	{
		try
		{
			String query = MessageFormat.format(DQL_CHECK_VERSION_HISTORY, Integer.toString(major), Integer.toString(minor), getQualifierDqlCondition());
			return DqlHelper.exist(DocbaseSessionManager.getSession(), query);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to check version history", e);
		}
	}

	public String getVersion()
	{
		try
		{
			String query = MessageFormat.format(DQL_GET_DOCBASE_VERSION, getQualifierDqlCondition());

			IDfTypedObject obj = DqlHelper.getFirstString(DocbaseSessionManager.getSession(), query);
			String major = obj.getString(MAJOR_ATTR);
			String minor = obj.getString(MINOR_ATTR);
			return major + "." + minor;
		}
		catch (DfEndOfCollectionException e)
		{
			createVersionRecord();
			return "0.0";
		}
		catch (DfException e)
		{
			if (e.getMessageId().equals(TYPE_NOT_EXIST_ERROR) ||
				e.getMessageId().equals(TABLE_NOT_FOUND_ERROR))
			{
				createNecessaryTables();
				createVersionRecord();
				return "0.0";
			}

			throw new BuildException("Error occured while trying to get current Docbase version", e);
		}
	}

	private void createVersionRecord()
	{
		String query = MessageFormat.format(DQL_CREATE_VERSION_RECORD,
				BuildVersionManager.getContextQualifier(),
				DocbaseSessionManager.getCurrentSessionContext().getLogin(),
				BuildVersionManager.getCurrentOsUser(),
				BuildVersionManager.getHostName(),
				BuildVersionManager.getHostIp());

		try
		{
			DqlHelper.executeQuery(DocbaseSessionManager.getSession(), query);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to create docbase version record, qualifier=" +
					BuildVersionManager.getContextQualifier(), e);
		}
	}

	private int[] parseVersion(String version)
	{
		String[] numbers = version.split("\\.");

		if (numbers.length != 2)
		{
			throw new BuildException("Invalid version number format " + version);
		}

		try
		{
			return new int[] {Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1])};
		}
		catch (NumberFormatException e)
		{
			throw new BuildException("Invalid version number format " + version);
		}
	}

	private void createNecessaryTables()
	{
		boolean isCreated = false;

		if (!isTableExist(DQL_CHECK_VERSION_TABLE))
		{
			try
			{
				DqlHelper.executeQuery(DocbaseSessionManager.getSession(), DQL_CREATE_VERSION_TABLE);
				isCreated = true;
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to create version table", e);
			}
		}

		if (!isTableExist(DQL_CHECK_HISTORY_TABLE))
		{
			try
			{
				DqlHelper.executeQuery(DocbaseSessionManager.getSession(), DQL_CREATE_HISTORY_TABLE);
				isCreated = true;
			}
			catch (DfException e)
			{
				throw new BuildException("Failed to create history table", e);
			}
		}

		if (isCreated)
		{
			DocbaseSessionManager.getSession().flushCaches();
		}
	}

	private boolean isTableExist(String query)
	{
		try
		{
			DqlHelper.exist(DocbaseSessionManager.getSession(), query);
			return true;
		}
		catch (DfException e)
		{
			if (e.getMessageId().equals(TYPE_NOT_EXIST_ERROR) ||
				e.getMessageId().equals(TABLE_NOT_FOUND_ERROR))
			{
				return false;
			}

			throw new BuildException("Failed to get current docbase version", e);
		}
	}

	private int compareVersions(String version1, String version2)
	{
		String part1[] = version1.split("\\.");
		String part2[] = version2.split("\\.");

		if (part1 == null || part1.length != 2)
		{
			throw new BuildException("Incorrect version format " + version1);

		}

		if (part2 == null || part2.length != 2)
		{
			throw new BuildException("Incorrect version format " + version2);

		}

		int majorNumber1;
		int minorNumber1;

		try
		{
			majorNumber1 = Integer.parseInt(part1[0]);
			minorNumber1 = Integer.parseInt(part1[1]);
		}
		catch (NumberFormatException e)
		{
			throw new BuildException("Incorrect version format " + version1);
		}

		int majorNumber2;
		int minorNumber2;

		try
		{
			majorNumber2 = Integer.parseInt(part2[0]);
			minorNumber2 = Integer.parseInt(part2[1]);
		}
		catch (NumberFormatException e)
		{
			throw new BuildException("Incorrect version format " + version2);
		}

		if (majorNumber1 < majorNumber2)
		{
			return -1;
		}

		if (majorNumber1 > majorNumber2)
		{
			return 1;
		}

		if (minorNumber1 < minorNumber2)
		{
			return -1;
		}

		if (minorNumber1 > minorNumber2)
		{
			return 1;
		}

		return 0;
	}

	private void updateVersionRecord(int newMajor, int newMinor, int currentMajor, int currentMinor)
	{
		String query = MessageFormat.format(DQL_UPDATE_VERSION_RECORD,
				Integer.toString(newMajor),
				Integer.toString(newMinor),
				DocbaseSessionManager.getCurrentSessionContext().getLogin(),
				BuildVersionManager.getCurrentOsUser(),
				BuildVersionManager.getHostName(),
				BuildVersionManager.getHostIp(),
				Integer.toString(currentMajor),
				Integer.toString(currentMinor),
				getQualifierDqlCondition());

		try
		{
			DqlHelper.executeQuery(DocbaseSessionManager.getSession(), query);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to update docbase version to " +
					newMajor + "." + newMinor + " [" + BuildVersionManager.getContextQualifier() + "]", e);
		}
	}

	private void createHistoryRecord(int major, int minor, int prevMajor, int prevMinor)
	{
		String query = MessageFormat.format(DQL_CREATE_HISTORY_RECORD,
				Integer.toString(major),
				Integer.toString(minor),
				Integer.toString(prevMajor),
				Integer.toString(prevMinor),
				BuildVersionManager.getContextQualifier(),
				DocbaseSessionManager.getCurrentSessionContext().getLogin(),
				BuildVersionManager.getCurrentOsUser(),
				BuildVersionManager.getHostName(),
				BuildVersionManager.getHostIp());

		try
		{
			DqlHelper.executeQuery(DocbaseSessionManager.getSession(), query);
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to create docbase version history for " +
					prevMajor + "." + prevMinor + "->"  + major + "." + minor +
					" [" + BuildVersionManager.getContextQualifier() + "]", e);
		}
	}

	private String getQualifierDqlCondition()
	{
		String qualifier = BuildVersionManager.getContextQualifier();
		return qualifier == null || qualifier.trim().length() == 0 ?
				"is nullstring" : "='" + qualifier + "'";
	}
}
