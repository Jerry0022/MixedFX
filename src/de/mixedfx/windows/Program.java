package de.mixedfx.windows;

import de.mixedfx.file.FileObject;

public class Program
{
	public final FileObject	path;
	public final String		processName;
	public final String		serviceName;

	/**
	 * @param path
	 *            The path of the executable!
	 * @param processName
	 *            The process name (see also taskmgr.exe)!
	 * @param serviceName
	 *            The service name (see services.msc)!
	 */
	public Program(final FileObject path, final String processName, final String serviceName)
	{
		this.path = path;
		this.processName = processName;
		this.serviceName = serviceName;
	}
}
