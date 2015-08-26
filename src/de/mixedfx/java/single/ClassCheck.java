package de.mixedfx.java.single;

import java.io.Serializable;

/**
 * A helper-class to compare the application names
 */
public class ClassCheck implements Serializable
{

	private static final long serialVersionUID = 1L;

	private String className = null;

	public ClassCheck()
	{
	}

	public ClassCheck(final String className)
	{
		this.setClassName(className);
	}

	public String getClassName()
	{
		return this.className;
	}

	public void setClassName(final String className)
	{
		this.className = className;
	}

	@Override
	public String toString()
	{
		return this.className;
	}
}
