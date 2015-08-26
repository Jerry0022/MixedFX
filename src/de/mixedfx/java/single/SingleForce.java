package de.mixedfx.java.single;

import de.mixedfx.file.FileObject;
import de.mixedfx.logging.Log;

public class SingleForce
{
	private static final String				DIRECTORY	= System.getProperty("java.io.tmpdir");
	private static final String				FILENAME	= "LockProject.file";
	private static SingleInstanceController	controller;

	/**
	 * @return Returns true if an instance is already running otherwise false.
	 */
	public static boolean check(final String applicationName)
	{
		if (SingleForce.getController(applicationName).isOtherInstanceRunning())
		{
			Log.DEFAULT.info("Register Application for SingleInstance was " + (SingleForce.getController(applicationName).registerApplication() ? "successful!" : "unsuccessful!"));
			return true;
		} else
			return false;
	}

	public static SingleInstanceController getController(final String applicationName)
	{
		System.out.println(SingleForce.controller);
		return SingleForce.controller != null ? SingleForce.controller : (SingleForce.controller = new SingleInstanceController(FileObject.create().setPath(SingleForce.DIRECTORY).setFullName(SingleForce.FILENAME).toFile(), applicationName));
	}
}
