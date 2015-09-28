package de.mixedfx.windows.ahk;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;
import de.mixedfx.java.ComplexString;
import de.mixedfx.java.StreamUtil;
import de.mixedfx.windows.Executor;
import javafx.util.Duration;

public class AHKManager
{
	public static final String	AHKExecutable	= "AutoHotkeyU32.exe";
	public static final String	AHKExtension	= ".ahk";

	/**
	 * @param ahkScript
	 *            An AHK script
	 * @return Returns a ProcessBuilder which can be used to execute the AHK file. If scriptName is wrong returns null.
	 * @throws IOException
	 *             If something went wrong copying the files to temp direction
	 */
	public static ProcessBuilder checkExistance(final File ahkScript) throws IOException
	{
		final FileObject ahkTempExeFile = FileObject.create().setPath(DataHandler.getTempFolder().toString()).setFullName(AHKManager.AHKExecutable);
		final FileObject ahkTempScript = FileObject.create().setPath(DataHandler.getTempFolder().toString()).setFullName(FileObject.create(ahkScript).getFullName())
				.setExtension(AHKManager.AHKExtension);

		// Copy AutoHotKey.exe
		final File ahkExeFile = StreamUtil.stream2file(AHKManager.class.getResourceAsStream(AHKManager.AHKExecutable));
		if (!ahkTempExeFile.toFile().exists() || !ahkTempExeFile.equalsNameSize(FileObject.create(ahkExeFile)))
			FileUtils.copyFile(ahkExeFile, ahkTempExeFile.toFile());
		// Copy Script
		if (!ahkTempScript.toFile().exists() || !ahkTempScript.equalsNameSize(FileObject.create(ahkScript)))
			FileUtils.copyFile(ahkScript, ahkTempScript.toFile());
		return new ProcessBuilder(ahkTempExeFile.getFullPath(), ahkTempScript.getFullPath());
	}

	/**
	 * @param ahkScript
	 *            An AHK script
	 * @return Returns a ProcessBuilder which can be used to execute the AHK file. If scriptName is wrong returns null.
	 * @throws IOException
	 *             If something went wrong copying the files to temp direction
	 */
	public static ProcessBuilder checkExistance(final FileObject ahkScript) throws IOException
	{
		return AHKManager.checkExistance(ahkScript.setExtension(AHKManager.AHKExtension).toFile());
	}

	/**
	 * Removes all AHK scripts from temp directory.
	 *
	 * @throws IOException
	 *             If something went wrong.
	 */
	public static void cleanTempDirectory() throws IOException
	{
		AHKManager.cleanTempDirectory(false);
	}

	/**
	 * @param onlyScript
	 *            If true only AHK scripts are removed. Otherwise also the AutoHotKey executable is removed.
	 * @throws IOException
	 *             If something went wrong.
	 */
	public static void cleanTempDirectory(final boolean onlyScript) throws IOException
	{
		if (!onlyScript)
			DataHandler.deleteFile(FileObject.create().setPath(DataHandler.getTempFolder().getFullPath()).setFullName(AHKManager.AHKExecutable));
		DataHandler.listFiles(DataHandler.getTempFolder()).stream().filter(file -> FileObject.create(file).getFullExtension().equalsIgnoreCase(AHKManager.AHKExtension))
				.forEach(file -> DataHandler.deleteFile(FileObject.create(file)));
	}

	/**
	 * @param commandBlock
	 *            AutoHotKey commands. Each command as one element of the ComplexString
	 * @param blocking
	 *            If this method should wait for the completion of the AHK script it is executed NOT AS ADMIN otherwise it is executed as ADMIN!
	 * @throws IOException
	 *             If file(s) could not be created in temp directory
	 */
	public static void runAHKCommands(final ComplexString commandBlock, final boolean blocking) throws IOException
	{
		AHKManager.runAHKCommands(commandBlock.toString(), blocking);
	}

	/**
	 * @param commands
	 *            AutoHotKey commands separated by line breaks
	 * @param blocking
	 *            If this method should wait for the completion of the AHK script it is executed NOT AS ADMIN otherwise it is executed as ADMIN!
	 * @throws IOException
	 *             If file(s) could not be created in temp directory
	 */
	public static void runAHKCommands(final String commands, final boolean blocking) throws IOException
	{
		// Create random not existing file
		FileObject script = null;
		for (int i = 0; i < 5; i++)
		{
			final String id = UUID.randomUUID().toString();
			final FileObject tmpFile = FileObject.create().setPath(DataHandler.getTempFolder().toString()).setName(id).setExtension(AHKManager.AHKExtension);
			if (!tmpFile.toFile().exists())
			{
				script = tmpFile;
				FileUtils.writeStringToFile(script.toFile(), commands);
				break;
			}
		}

		// Check if file could be created, then execute and delete it
		if (script == null)
			throw new IOException("Three random file names were already available. Therefore could not create script file.");
		else
		{
			// Remove file after 3 seconds
			final String toDelete = script.toString();
			final Duration delay = Duration.seconds(3);
			new Thread(() ->
			{
				try
				{
					Thread.sleep((long) delay.toMillis());
				}
				catch (final Exception e)
				{}
				DataHandler.deleteFile(FileObject.create(new File(toDelete)));
			}).start();

			// Start script
			AHKManager.startScript(script.setExtension(AHKManager.AHKExtension).toFile(), blocking);
		}
	}

	/**
	 *
	 * @param scriptFile
	 *            The ahk script to execute with or without extension.
	 * @param blocking
	 *            If this method should wait for the completion of the AHK script it is executed NOT AS ADMIN otherwise it is executed as ADMIN!
	 * @throws IOException
	 *             If file(s) could not be created in temp directory
	 */
	public static void runAHKFile(final FileObject scriptFile, final boolean blocking) throws IOException
	{
		// Set the AHK extension and start script
		AHKManager.startScript(scriptFile.setExtension(AHKManager.AHKExtension).toFile(), blocking);
	}

	/**
	 * Executes an AHK script which is in the src directory in the same package as the referenceClass.
	 *
	 * @param referenceClass
	 *            The class which is in the same package of the script
	 * @param scriptName
	 *            The name of the script with or without extension
	 * @param blocking
	 *            If this method should wait for the completion of the AHK script it is executed NOT AS ADMIN otherwise it is executed as ADMIN!
	 * @throws IOException
	 *             If file(s) could not be created in temp directory
	 */
	public static void runEmbeddedAHKFile(final Class<?> referenceClass, String scriptName, final boolean blocking) throws IOException
	{
		// Is extension available?
		if (!scriptName.substring(scriptName.length() - 4).equalsIgnoreCase(".ahk"))
			scriptName += ".ahk";

		// Get resource and start script
		final File scriptFile = StreamUtil.stream2file(referenceClass.getResourceAsStream(scriptName));
		AHKManager.startScript(scriptFile, blocking);

	}

	/**
	 * @param script
	 *            The script file to start
	 * @param blocking
	 *            If this method should wait for the completion of the AHK script it is executed NOT AS ADMIN otherwise it is executed as ADMIN!
	 * @throws IOException
	 *             If something went wrong!
	 */
	private static void startScript(final File script, final boolean blocking) throws IOException
	{
		try
		{
			// Start AHK script
			final ProcessBuilder process = AHKManager.checkExistance(script);
			if (blocking)
				process.start().waitFor();
			else
				Executor.runAsAdmin(process.command().get(0), process.command().get(1), true);
		}
		catch (final InterruptedException e)
		{}
	}
}
