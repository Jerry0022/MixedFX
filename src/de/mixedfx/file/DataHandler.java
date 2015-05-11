package de.mixedfx.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;

/**
 * <pre>
 * FileFolderHandler offers generic methods to access/change/delete
 * files and folders. It shall be used as first choice, all other file / folder accessing methods you can find in Apache's commons io.
 * It contains four sections of methods:
 * </pre>
 * <ol>
 * <li>Listing methods</li>
 * <li>File/Folder methods</li>
 * <li>Other methods</li>
 * </ol>
 *
 * @author Jerry
 */
public class DataHandler
{
	/**
	 * Prevents other classes from creating an own instance of FileFolderHandler
	 */
	private DataHandler()
	{
	}

	/*
	 * LISTING METHODS
	 */

	/**
	 * Returns the direct subfolders' names!
	 *
	 * @param parentFolder
	 * @return Returns the direct subfolders' names!
	 */
	public static ArrayList<String> getSubFolderList(final FileObject parentFolder)
	{
		final File file = new File(parentFolder.getFullPath());
		final String[] result = file.list((current, name) -> new File(current, name).isDirectory());
		return new ArrayList<String>(Arrays.asList(result));
	}

	/**
	 * Returns a list of files which are directly in the folder.
	 *
	 * @param fileObject
	 * @return Returns a list of files which are directly in the folder.
	 */
	public static Collection<File> listFiles(final FileObject fileObject)
	{
		return FileUtils.listFiles(new File(fileObject.getPath()), TrueFileFilter.TRUE, null);
	}

	/**
	 * Returns all executables (found by extension ".exe") in folder.
	 *
	 * @param parentFolder
	 * @return Returns all executables (found by extension ".exe") in folder.
	 */
	public static Collection<File> getExecuteables(final FileObject parentFolder)
	{
		return FileUtils.listFiles(parentFolder.toFile(), new RegexFileFilter("(.*\\.exe)$"), DirectoryFileFilter.DIRECTORY);
	}

	/*
	 * FILE/FOLDER METHODS
	 */

	/**
	 * Creates or finds a file. Creation could take some time.
	 *
	 * @param fileObject
	 *            The fileObject which should be created.
	 * @return Returns the new created file or the found file which already exists
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static File createOrFindFile(final FileObject fileObject) throws IOException, InterruptedException
	{
		final File newFile = new File(fileObject.getFullPath());

		// No reliable check solution available to check if file is already
		// opened by another program etc.
		if (!newFile.exists() || newFile.isDirectory())
		{
			// File doesn't exist yet
			newFile.getParentFile().mkdirs();
			newFile.createNewFile(); // Throws IOException e. g. if it can't access the drive
		}

		// File creation needs sometimes some time :)
		while (!newFile.exists() || !newFile.canWrite() || !newFile.canRead())
			Thread.sleep(10);

		return newFile;
	}

	/**
	 * Finds a file by retrieving it only with its name. The extension is NOT needed but can be
	 * available.
	 *
	 * @param fileObject
	 *            The fileName with or without extension! If without extension it finds first a file
	 *            with equal file name which has no extension, otherwise the first file with equal
	 *            file name with extension, otherwise the file is not found.
	 * @return Returns the found file.
	 * @throws FileNotFoundException
	 *             Throws exception if file doesn't exist
	 */
	public static File readFile(final FileObject fileObject) throws FileNotFoundException
	{

		File result = null;

		if (fileObject.getPath().equalsIgnoreCase("") || !new File(fileObject.getPath()).exists() || !new File(fileObject.getPath()).isDirectory())
			throw new FileNotFoundException("Path is empty!");

		final String fileFolder = FilenameUtils.getName(fileObject.getFullName()).toLowerCase();
		for (final File f : DataHandler.listFiles(fileObject))
			if (FilenameUtils.getExtension(fileFolder) == "")
			{
				// fileName doesn't contain an extension
				if (FilenameUtils.getBaseName(f.toString().toLowerCase()).equals(fileFolder))
				{
					result = f;
					break;
				}
			}
			else
				// fileName does contain an extension
				if (FilenameUtils.getName(f.toString()).toString().toLowerCase().equals(fileFolder))
				{
					result = f;
					break;
				}

		if (result != null)
			return result;
		else
			throw new FileNotFoundException("File doesn't exist or is a directory!");
	}

	/**
	 * Writes a file using {@link #createOrFindFile(FileObject)} ignoring exceptions.
	 *
	 * @param data
	 *            Should contain also the extension!
	 * @return Returns the file or null if it could not create a file.
	 */
	public static File writeFile(final FileObject data)
	{
		try
		{
			return DataHandler.createOrFindFile(data);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		catch (final InterruptedException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Deletes a file
	 *
	 * @param fullPath
	 *            The full absolute directory + file name
	 * @return True if file was successfully deleted or false if deletion failed
	 */
	public static boolean deleteFile(final FileObject fullPath)
	{
		boolean success;

		final File file = new File(fullPath.getFullPath());

		if (!file.exists())
			success = true; // File doesn't exist already
		else
			// File exists
			if (!file.isDirectory())
			{
				if (file.delete())
					success = true;
				else
					success = false;
			}
			else
				try
		{
					FileUtils.deleteDirectory(file);
					success = true;
		}
		catch (final IOException e)
		{
			success = false;
		}

		return success;
	}

	/**
	 * Creates a folder from a FileObject.
	 *
	 * @param folder
	 *            A representation of the folder which shall be created.
	 * @return Returns the folder (same as parameter).
	 * @throws IOException
	 *             If folder couln't be created it throws a IOException.
	 */
	public static FileObject createFolder(final FileObject folder) throws IOException
	{
		FileUtils.forceMkdir(folder.toFile());
		return folder;
	}

	/*
	 * OTHER FILE / FOLDER METHODS
	 */

	/**
	 * Fuses directory and file to one full path.
	 *
	 * @param directory
	 *            Should contain only \\ or / as folder separator. Last char as \\ or / is not
	 *            needed.
	 * @param fileFolder
	 *            The file or folder which should be fused.
	 * @return Returns a full string containing the directory and file or folder
	 */
	public static String fuse(String directory, final String fileFolder)
	{
		String fullPath = "";

		final String substring = directory.length() > 1 ? directory.substring(directory.length() - 1) : directory;
		if (!substring.equals("\\"))
			if (!substring.equals("/"))
				if (StringUtils.countMatches(directory, "/") > 0)
					directory += "/";
				else
					directory += "\\";

		fullPath = directory + fileFolder;

		return fullPath;
	}
}
