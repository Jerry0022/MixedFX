package de.mixedfx.windows;

import de.mixedfx.file.FileObject;

public class Program
{
	/**
	 * The name of the program! (Can be everything)
	 */
	public final String		programName;
	/**
	 * The path of the executable including the file!
	 */
	public final FileObject	fullPath;
	/**
	 * The process name (see also taskmgr.exe)!
	 */
	public final String		processName;
	/**
	 * The service name (see services.msc)!
	 */
	public final String		serviceName;

	/**
	 * @param programName
	 *            The name of the program! (Can be everything)
	 * @param fullPath
	 *            The path of the executable including the file!
	 * @param processName
	 *            The process name (see also taskmgr.exe)!
	 * @param serviceName
	 *            The service name (see services.msc)!
	 */
	public Program(final String programName, final FileObject fullPath, final String processName, final String serviceName)
	{
		this.programName = programName;
		this.fullPath = fullPath;
		this.processName = processName;
		this.serviceName = serviceName;
	}
}
