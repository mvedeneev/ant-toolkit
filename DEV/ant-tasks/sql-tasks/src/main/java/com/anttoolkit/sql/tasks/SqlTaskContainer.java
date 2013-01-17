package com.anttoolkit.sql.tasks;

import java.sql.*;
import java.util.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;

import com.anttoolkit.sql.common.*;
import com.anttoolkit.common.*;

public class SqlTaskContainer
		extends GenericTask
		implements TaskContainer
{
	private static ThreadLocal<Map<String, Driver>> m_drivers = new ThreadLocal<Map<String, Driver>>()
	{
		protected Map<String, Driver> initialValue()
		{
			return new HashMap<String, Driver>();
		}
	};

	private String m_user;
	private String m_password;
	private String m_url;
	private String m_driver;
	private Path m_classpath;
	private List<Property> connectionProperties = new LinkedList<Property>();
	private List<Task> m_tasks = new LinkedList<Task>();

	public void setUser(String user)
	{
    	m_user= user;
 	}

	public void setPassword(String password)
	{
		m_password = password;
	}

	public void setUrl(String url)
	{
		m_url = url;
	}

	public void setDriver(String driver)
	{
		m_driver = driver.trim();
	}

	public void setClasspath(Path classpath)
	{
		m_classpath = classpath;
	}

	public Path createClasspath()
	{
		if (m_classpath == null)
		{
			m_classpath = new Path(getProject());
		}

	 	return m_classpath.createPath();
	}

	public void setClasspathRef(Reference r)
	{
		createClasspath().setRefid(r);
	}

	public void addConnectionProperty(Property var)
	{
		connectionProperties.add(var);
	}


	public void doWork() throws BuildException
	{
		validate();

		LoginInfo loginInfo = new LoginInfo(m_url, m_user, m_password, getDriver(), getConnectionProperties());

		SqlSessionManager.setCurrentSessionContext(loginInfo);

		try
		{
			for (Task task : m_tasks)
			{
				task.perform();
			}
		}
		catch (Throwable e)
		{
			SqlSessionManager.getSession().rollbackTransaction();

			if (e instanceof BuildException)
			{
				throw (BuildException)e;
			}

			throw new BuildException("Exception occured", e);
		}
		finally
		{
			SqlSessionManager.releaseCurrentSessionContext();
		}
	}

	public void addTask(Task task)
	{
		m_tasks.add(task);
	}

	private void validate()
	{
		if (m_user == null)
		{
			throw new BuildException("User name is not specified");
		}

		if (m_password == null)
		{
			throw new BuildException("Password name is not specified");
		}

		if (m_url == null)
		{
			throw new BuildException("Connection URL is not specified");
		}

		if (m_driver == null)
		{
			throw new BuildException("Driver class is not specified");
		}
	}

	private Driver getDriver()
	{
		if (m_driver == null)
		{
      		throw new BuildException("Driver attribute is not specified");
  		}

		Driver driverInstance = m_drivers.get().get(m_driver);
		if (driverInstance != null)
		{
			return driverInstance;
		}

  		try
		{
      		Class clazz;
      		if (m_classpath != null)
			{
            	AntClassLoader loader = getProject().createClassLoader(m_classpath);
				clazz = loader.loadClass(m_driver);
      		}
			else
			{
          		clazz = Class.forName(m_driver);
			}

      		return (Driver)clazz.newInstance();
  		}
		catch (ClassNotFoundException e)
		{
      		throw new BuildException("Class Not Found: JDBC driver " + m_driver + " could not be loaded", e, getLocation());
		}
		catch (IllegalAccessException e)
		{
			throw new BuildException("Illegal Access: JDBC driver " + m_driver + " could not be loaded", e, getLocation());
		}
		catch (InstantiationException e)
		{
			throw new BuildException("Instantiation Exception: JDBC driver " + m_driver + " could not be loaded", e, getLocation());
		}
	}

	private Properties getConnectionProperties()
	{
		Properties props = new Properties();
		props.put("user", m_user);
		props.put("password", m_password);
		props.put("autocommit", "false");

		for (Property p : connectionProperties)
		{
			String name = p.getName();
			String value = p.getValue();
			if (name != null && value != null)
			{
				props.put(name, value);
			}
		}

		return props;
	}
}
