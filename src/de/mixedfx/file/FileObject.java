package de.mixedfx.file;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * <pre>
 * Provides a class to create an Object representing a File or Folder but only contains Strings.
 * Also contains methods to get elements of this File representation such as path, name and extension!
 * Overwritten methods: equals(), toString() and clone() method!
 * Read the JavaDoc of the methods to produce no undesired results!
 * </pre>
 *
 * @author Jerry
 */
public class FileObject implements Cloneable
{
	/**
	 * The separator between the prefix and the name in the file name. As soon as this parameter is
	 * modified, all actions taken on a FileObject will deal with the new one. Default: "###"
	 */
	public static String	prefixSeparator		= "###";
	/**
	 * The separator between the name and the extension in the file name. As soon as this parameter
	 * is modified, all actions taken on a FileObject will deal with the new one. Default: "."
	 */
	public static String	extensionSeparator	= ".";

	public static FileObject create()
	{
		return new FileObject();
	}

	public static FileObject create(final File file)
	{
		return new FileObject(file);
	}

	private String	path;		// pure path maybe without last folder
	private String	prefix;	// without separator
	private String	name;		// pure name without sth. else
	private String	extension;	// without separator

	/**
	 * <pre>
	 * Fills all available values with empty Strings.
	 * Please fill manually the values via:
	 * {@link #setPath(String)}
	 * {@link #setPrefix(String)}
	 * {@link #setName(String)}
	 * {@link #setExtension(String)}
	 * or:
	 * {@link #setPath(String)}
	 * {@link #setFullName(String)}
	 * </pre>
	 */
	public FileObject()
	{
		this.clear();
	}

	/**
	 * Converts a file to a {@link FileObject}. See also {@link #FileObject()}.
	 *
	 * @param file
	 *            The file or folder which shall be used to create the FileObject
	 */
	public FileObject(final File file)
	{
		this();

		if (file.isDirectory())
			this.setPath(FilenameUtils.getFullPath(file.getAbsolutePath()));
		else if (file.isFile())
		{
			this.setPath(FilenameUtils.getFullPath(file.getAbsolutePath()));
			this.setFullName(FilenameUtils.getName(file.getAbsolutePath()));
		}
	}

	/**
	 * @return Returns the corresponding {@link File} even if the file is not valid / created
	 */
	public File getFile()
	{
		return new File(this.getFullPath());
	}

	/**
	 * @return Returns the size of the file or folder as {@link BigInteger} in Bytes
	 */
	public BigInteger getSize()
	{
		return FileUtils.sizeOfAsBigInteger(this.getFile());
	}

	/**
	 * @return Returns the URI representation of this FileObject
	 */
	public String toURI()
	{
		return new File(this.getFullPath()).toURI().toString();
	}

	/**
	 * Compares file/folder size and name (ignoring case)
	 *
	 * @param toCompare
	 * @return Returns true if size and name is equal.
	 */
	public boolean equalsNameSize(final FileObject toCompare)
	{
		if (this.getName().equalsIgnoreCase(toCompare.getName()) && this.getSize().equals(toCompare.getSize()))
			return true;
		else
			return false;
	}

	/**
	 * Compares file/folder fullPath.
	 *
	 * @param toCompare
	 *            The FileObject to compare.
	 * @return Returns true if the FileObject is the same ignoring case.
	 */
	public boolean equals(final FileObject toCompare)
	{
		if (this.getFullPath().equalsIgnoreCase(toCompare.getFullPath()))
			return true;
		else
			return false;
	}

	/**
	 * Checks if {@link #getFullPath()} is valid. This means the criterias of the os.
	 *
	 * @return boolean True if it fulfills the criterias, false if not.
	 */
	public boolean isValid()
	{
		try
		{
			final File toTest = new File(this.getFullPath());
			toTest.getCanonicalPath();
			return true;
		}
		catch (final IOException e)
		{
			return false;
		}
	}

	@Override
	public String toString()
	{
		return this.getFullPath();
	}

	@Override
	public FileObject clone()
	{
		return FileObject.create(this.getFile());
	}

	// Getters and Setters

	/**
	 * All set attributes are deleted.
	 */
	public void clear()
	{
		this.path = "";
		this.prefix = "";
		this.name = "";
		this.extension = "";
	}

	/**
	 * ATTENTION: If this object represents a directory use {@link #getFullPath()} instead to get
	 * reliably the full path!
	 *
	 * @return Returns only the path!
	 */
	public String getPath()
	{
		return this.path;
	}

	/**
	 * Returns the entire path including the file/folder name with prefix and extension
	 *
	 * @return
	 */
	public String getFullPath()
	{
		if (this.getPath().equals("") && this.getFullName().equals(""))
			return "";
		else
			return DataHandler.fuse(this.getPath(), this.getFullName());
	}

	/**
	 * @param path
	 *            Set the path (excluding file)
	 */
	public FileObject setPath(final String path)
	{
		this.path = path;
		return this;
	}

	/**
	 * @return Returns only the prefix.
	 */
	public String getPrefix()
	{
		return this.prefix;
	}

	/**
	 * @return Returns the prefix plus separator
	 */
	public String getFullPrefix()
	{
		if (this.getPrefix().equals(""))
			return this.getPrefix();
		else
			return this.getPrefix() + FileObject.prefixSeparator;
	}

	/**
	 * @param prefix
	 *            Set the prefix with/without separator
	 */
	public FileObject setPrefix(final String prefix)
	{
		if (prefix.endsWith(FileObject.prefixSeparator))
			this.prefix = prefix.replaceFirst(FileObject.prefixSeparator, "");
		else
			this.prefix = prefix;
		return this;
	}

	/**
	 * @return Returns the name without prefix and without extension
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * @return Returns the name with prefix and without extension
	 */
	public String getFullNameWithoutExtension()
	{
		return this.getFullPrefix() + this.getName();
	}

	/**
	 * @return Returns the name with prefix and with extension
	 */
	public String getFullName()
	{
		return this.getFullNameWithoutExtension() + this.getFullExtension();
	}

	/**
	 * Set the name excluding directory, prefix and extension.
	 *
	 * @param name
	 *            File/Folder name without prefix without extension
	 */
	public FileObject setName(final String name)
	{
		this.name = name;
		return this;
	}

	/**
	 * !!! Overwrites all previously set values!
	 *
	 * @param name
	 *            File/Folder name with/without prefix with/without extension, but without the
	 *            (parent) directory
	 */
	public FileObject setFullName(String name)
	{
		this.setExtension(FilenameUtils.getExtension(name));

		name = FilenameUtils.getBaseName(name);
		final String prefix = name.split(FileObject.prefixSeparator)[0];
		if (!prefix.equals(name))
		{
			this.setPrefix(prefix);
			this.setName(name.substring(prefix.length() + FileObject.prefixSeparator.length()));
		}
		else
		{
			this.setName(name);
			this.prefix = "";
		}
		return this;
	}

	/**
	 * @return Returns the extension without separator
	 */
	public String getExtension()
	{
		return this.extension;
	}

	/**
	 * @return Returns separator + extension
	 */
	public String getFullExtension()
	{
		if (this.getExtension().equals(""))
			return this.getExtension();
		else
			return FileObject.extensionSeparator + this.getExtension();
	}

	/**
	 * @param extension
	 *            Set the file extension with/without separator, e. g. ".png" or only "png"
	 */
	public FileObject setExtension(final String extension)
	{
		if (extension.startsWith(FileObject.extensionSeparator))
			this.extension = extension.replaceFirst("\\" + FileObject.extensionSeparator, "");
		else
			this.extension = extension;
		return this;
	}
}
