package com.anttoolkit.documentum.common;

public class LoginInfo
{
	private String m_login = null;
	private String m_domain = null;
	private String m_password = null;
	private String m_docbase = null;

	private String m_stringPresentation = null;

	public LoginInfo(String login,
					 String domain,
					 String password,
					 String docbase)
	{
		m_login = login == null || login.trim().length() == 0 ? null : login.trim();
		m_domain = domain == null || domain.trim().length() == 0 ? null : domain.trim();
		m_password = password == null || password.trim().length() == 0 ? null : password.trim();
		m_docbase = docbase == null || docbase.trim().length() == 0 ? null : docbase.trim();

		StringBuffer buffer = new StringBuffer();
		buffer.append(m_login == null ? "" : m_login).append("\r\n");
		buffer.append(m_domain == null ? "" : m_domain).append("\r\n");
		buffer.append(m_password == null ? "" : m_password).append("\r\n");
		buffer.append(m_docbase == null ? "" : m_docbase);

		m_stringPresentation = buffer.toString();
	}

	public LoginInfo(String stringPresentation)
	{
		String[] parts = stringPresentation.split("\r\n");
		if (parts.length != 4)
		{
			throw new IllegalArgumentException("Incorrect login info string specified");
		}

		m_login = parts[0];
		m_domain = parts[1];
		m_password = parts[2];
		m_docbase = parts[3];

		m_stringPresentation = stringPresentation;
	}

	public boolean isInvalid()
	{
		return m_login == null || m_password == null || m_docbase == null;
	}

	public String getLogin()
	{
		return m_login;
	}

	public String getDomain()
	{
		return m_domain;
	}

	public String getPassword()
	{
		return m_password;
	}

	public String getDocbase()
	{
		return m_docbase;
	}

	public boolean equals(Object obj)
	{
		return !(obj == null || !(obj instanceof LoginInfo)) &&
				obj.toString().equals(this.toString());
	}

	public String toString()
	{
		return m_stringPresentation;
	}
}
