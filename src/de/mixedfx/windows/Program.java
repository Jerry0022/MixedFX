package de.mixedfx.windows;

import de.mixedfx.file.FileObject;

public class Program
{
	public final FileObject	fullPath;
	public final String		processName;
	public final String		serviceName;

	public Program(final FileObject fullPath, final String processName, final String serviceName)
	{
		this.fullPath = fullPath;
		this.processName = processName;
		this.serviceName = serviceName;
	}
}
