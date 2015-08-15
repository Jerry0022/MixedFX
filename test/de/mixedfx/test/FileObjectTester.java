package de.mixedfx.test;

import java.io.File;

import de.mixedfx.file.FileObject;

public class FileObjectTester
{
	public static void main(String[] args)
	{
		System.out.println(FileObject.create(new File("assets")).size());
	}
}