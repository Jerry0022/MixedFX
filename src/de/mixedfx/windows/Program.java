package de.mixedfx.windows;

import de.mixedfx.file.FileObject;

public class Program
{
	public final FileObject	fullPath;
	public final String		processName;
	public final String		serviceName;

	/**
	 * @param fullPath
	 *            The path of the executable including the file!
	 * @param processName
	 *            The process name (see also taskmgr.exe)!
	 * @param serviceName
	 *            The service name (see services.msc)!
	 */
	public Program(final FileObject fullPath, final String processName, final String serviceName)
	{
		this.fullPath = fullPath;
		this.processName = processName;
		this.serviceName = serviceName;
	}
}
