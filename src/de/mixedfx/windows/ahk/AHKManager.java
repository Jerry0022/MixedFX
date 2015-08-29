package de.mixedfx.windows.ahk;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;
import de.mixedfx.java.ComplexString;
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
		final FileObject ahkTempExeFile = FileObject.create().setPath(AHKManager.getTempFolder().toString()).setFullName(AHKManager.AHKExecutable);
		final FileObject ahkTempScript = FileObject.create().setPath(AHKManager.getTempFolder().toString()).setFullName(FileObject.create(ahkScript).getFullName()).setExtension(AHKManager.AHKExtension);

		try
		{
			// Copy AutoHotKey.exe
			final File ahkExeFile = new File(AHKManager.class.getResource(AHKManager.AHKExecutable).toURI());
			if (!ahkTempExeFile.toFile().exists() || !ahkTempExeFile.equalsNameSize(FileObject.create(ahkExeFile)))
				FileUtils.copyFile(ahkExeFile, ahkTempExeFile.toFile());
			// Copy Script
			if (!ahkTempScript.toFile().exists() || !ahkTempScript.equalsNameSize(FileObject.create(ahkScript)))
				FileUtils.copyFile(ahkScript, ahkTempScript.toFile());
			return new ProcessBuilder(ahkTempExeFile.getFullPath(), ahkTempScript.getFullPath());
		} catch (final URISyntaxException e)
		{
			return null;
		}
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
			DataHandler.deleteFile(FileObject.create().setPath(AHKManager.getTempFolder().getFullPath()).setFullName(AHKManager.AHKExecutable));
		DataHandler.listFiles(AHKManager.getTempFolder()).stream().filter(file -> FileObject.create(file).getFullExtension().equalsIgnoreCase(AHKManager.AHKExtension)).forEach(file -> DataHandler.deleteFile(FileObject.create(file)));
	}

	/**
	 * @return Returns the tmp folder where the autohotkey.exe and scripts will be written to.
	 * @throws IOException
	 *             If temp directory is not accessible
	 */
	public static FileObject getTempFolder() throws IOException
	{
		// Retrieve real temp folder
		return FileObject.create().setPath(File.createTempFile("temp-file", "tmp").getParent());
	}

	/**
	 * @param commandBlock
	 *            AutoHotKey commands. Each command as one element of the ComplexString
	 * @param blocking
	 *            If this method should wait for the completion of the AHK script
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
	 *            If this method should wait for the completion of the AHK script
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
			final FileObject tmpFile = FileObject.create().setPath(AHKManager.getTempFolder().toString()).setName(id).setExtension(AHKManager.AHKExtension);
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
			final Process process = AHKManager.checkExistance(script).start();

			// Remove file after 3 seconds
			final String toDelete = script.toString();
			final Duration delay = Duration.seconds(3);
			new Thread(() ->
			{
				try
				{
					Thread.sleep((long) delay.toMillis());
				} catch (final Exception e)
				{
				}
				DataHandler.deleteFile(FileObject.create(new File(toDelete)));
			}).start();

			// Shall this method block until AHK script is done?
			if (blocking)
			{
				try
				{
					process.waitFor();
				} catch (final InterruptedException e)
				{
				}
			}
		}
	}

	/**
	 *
	 * @param scriptFile
	 *            The ahk script to execute with or without extension.
	 * @param blocking
	 *            If this method should wait for the completion of the AHK script
	 * @throws IOException
	 *             If file(s) could not be created in temp directory
	 */
	public static void runAHKFile(final FileObject scriptFile, final boolean blocking) throws IOException
	{
		final Process process = AHKManager.checkExistance(scriptFile).start();
		if (blocking)
		{
			try
			{
				process.waitFor();
			} catch (final InterruptedException e)
			{
			}
		}
	}

	/**
	 * Executes an AHK script which is in the src directory in the same package as the referenceClass.
	 *
	 * @param referenceClass
	 *            The class which is in the same package of the script
	 * @param scriptName
	 *            The name of the script with or without extension
	 * @param blocking
	 *            If this method should wait for the completion of the AHK script
	 * @throws IOException
	 *             If file(s) could not be created in temp directory
	 */
	public static void runEmbeddedAHKFile(final Class<?> referenceClass, String scriptName, final boolean blocking) throws IOException
	{
		// Is extension available?
		if (!scriptName.substring(scriptName.length() - 4).equalsIgnoreCase(".ahk"))
			scriptName += ".ahk";
		try
		{
			// Get resource
			final File scriptFile = new File(referenceClass.getResource(scriptName).toURI());

			// Start AHK script
			final Process process = AHKManager.checkExistance(scriptFile).start();
			if (blocking)
				process.waitFor();
		} catch (final URISyntaxException | InterruptedException e)
		{
		}
	}
}
