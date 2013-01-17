package com.anttoolkit.documentum.tasks.bof;

import com.anttoolkit.documentum.common.*;

import java.util.*;

import org.apache.tools.ant.*;

import com.documentum.fc.common.*;

public class Module
		extends BasicDocAppObject
{
	private static final String INTERFACES_ATTRIBUTE = "a_interfaces";
	private static final String PRIMARY_CLASS_ATTRIBUTE = "primary_class";

	public Module(String objectId,
				  String name,
				  String type,
				  String primaryClass,
				  DocbaseSession session,
				  IContentStorage storage)
	{
		super(objectId, name, type, session, storage);

		if (primaryClass != null && primaryClass.trim().length() > 0)
		{
			m_primaryClass = primaryClass ;
		}
	}

	public void checkOut()
			throws CheckOutException
	{
		populateClassInterfaces();
		populateModuleInterfaces();

		super.checkOut();
	}

	public void update()
			throws UpdateException
	{
		updateModule();
		super.update();
	}

	protected String getReadableTypeName()
	{
		return "Module";
	}

	protected String contentQueryTemplate()
	{
		return "select r_object_id, object_name, r_object_type " +
			"from dmc_jar where folder(ID(''{0}''))";
	}

	private void populateModuleInterfaces()
			throws BuildException
	{
		if (m_moduleInterfaces.size() != 0)
		{
			return;
		}

		try
		{
			int count = this.getDfObject().getValueCount(INTERFACES_ATTRIBUTE);
			for (int i = 0; i < count; i++)
			{
				m_moduleInterfaces.add(this.getDfObject().getRepeatingString(INTERFACES_ATTRIBUTE, i));
			}
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get DocApp interfaces for Module " + this.getName() +
				"\r\n" + e.toString());
		}
	}

	private void populateClassInterfaces()
			throws BuildException
	{
		if (m_classInterfaces.size() != 0)
		{
			return;
		}

		String primaryClassName = null;
		try
		{
			primaryClassName = this.getDfObject().getString(PRIMARY_CLASS_ATTRIBUTE);
			if (m_primaryClass != null && m_primaryClass.trim().length() != 0 &&
				!primaryClassName.equals(m_primaryClass))
			{
				m_isPrimaryClassChanged = true;
				primaryClassName = m_primaryClass;
			}

			Class primaryClass = Class.forName(primaryClassName, false, Thread.currentThread().getContextClassLoader());
			Class[] interfaces = getInterfaces(primaryClass);
			if (interfaces == null || interfaces.length == 0)
			{
				return;
			}

			String[] interfaceNames = new String[interfaces.length];
			for (int i = 0; i < interfaces.length; i++)
			{
				interfaceNames[i] = interfaces[i].getName();
			}

			m_classInterfaces.addAll(Arrays.asList(interfaceNames));
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to get primary class for Module \"" + this.getName() +
				"\"\r\n" + e.toString());
		}
		catch (ClassNotFoundException e)
		{
			throw new BuildException("Failed to load primary class " + primaryClassName +
					" for Module " + this.getName() + " using reflection\r\n" + e.toString());
		}
	}

	private boolean isInterfacesChanged()
	{
		if (m_classInterfaces.size() != m_moduleInterfaces.size())
		{
			return true;
		}

		for (Iterator iter = m_classInterfaces.iterator(); iter.hasNext();)
		{
			String classInterface = (String)iter.next();
			if (!m_moduleInterfaces.contains(classInterface))
			{
				return true;
			}
		}

		return false;
	}

	private void updateModule()
			throws BuildException
	{
		if (!isInterfacesChanged() && !m_isPrimaryClassChanged)
		{
			return;
		}

		try
		{
			if (m_isPrimaryClassChanged)
			{
				this.getDfObject().setString(PRIMARY_CLASS_ATTRIBUTE, m_primaryClass);
			}

			if (isInterfacesChanged())
			{
				this.getDfObject().removeAll(INTERFACES_ATTRIBUTE);
				for (Iterator iter = m_classInterfaces.iterator(); iter.hasNext();)
				{
					this.getDfObject().appendString(INTERFACES_ATTRIBUTE, (String)iter.next());
				}
			}

			this.getDfObject().save();
		}
		catch (DfException e)
		{
			throw new BuildException("Failed to update Module " + this.getName() +
				" interfaces\r\n" + e.toString());
		}
	}

	private static Class[] getInterfaces(Class _class)
	{
		if (_class == null)
		{
			return null;
		}

		ArrayList<Class> list = null;

		Class[] classInterfaces = _class.getInterfaces();
		if (classInterfaces != null && classInterfaces.length > 0)
		{
			list = new ArrayList<Class>(Arrays.asList(classInterfaces));

			for (Class _interface : classInterfaces)
			{
				Class[] interfaceInterfaces = _interface.getInterfaces();
				if (interfaceInterfaces == null || interfaceInterfaces.length == 0)
				{
					continue;
				}

				Class[] superInterfaces = getInterfaces(_interface);
				for (Class __interface : superInterfaces)
				{
					if (!list.contains(__interface))
					{
						list.add(__interface);
					}
				}
			}
		}

		Class superClass = _class.getSuperclass();
		if (superClass == null)
		{
			return list == null || list.size() == 0 ? null : list.toArray(new Class[]{});
		}

		Class[] superInterfaces = getInterfaces(superClass);
		if (superInterfaces == null || superInterfaces.length == 0)
		{
			return list == null || list.size() == 0 ? null : list.toArray(new Class[]{});
		}

		list = list != null ? list : new ArrayList<Class>();
		for (Class _interface : superInterfaces)
		{
			if (!list.contains(_interface))
			{
				list.add(_interface);
			}
		}

		return list == null || list.size() == 0 ? null : list.toArray(new Class[]{});
	}


	private List m_moduleInterfaces = new Vector();
	private List m_classInterfaces = new Vector();

	private String m_primaryClass = null;
	private boolean m_isPrimaryClassChanged = false;
}
