package com.anttoolkit.common;

public class ExceptionHelper
{
	public static String stackTraceToString(Throwable exception)
	{
		StringBuffer buffer = new StringBuffer();

		if (exception == null)
		{
			return buffer.toString();
		}

		buffer.append("\r\n");

		Throwable throwable = exception;
		do
		{
			buffer.append("\t").append(throwable.toString()).append("\r\n");
			StackTraceElement[] elements = throwable.getStackTrace();

			for (int i = 0; i < elements.length; i++)
			{
				buffer.append("\t\t").append(elements[i].toString()).append("\r\n");
			}

			throwable = throwable.getCause();
		}
		while (throwable != null);

		return buffer.toString();
	}
}
