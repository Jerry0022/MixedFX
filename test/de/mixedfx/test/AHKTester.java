package de.mixedfx.test;

import java.io.IOException;

import de.mixedfx.file.FileObject;
import de.mixedfx.java.ComplexString;
import de.mixedfx.windows.ahk.AHKManager;

public class AHKTester
{

	public static void main(final String[] args)
	{
		System.out.println(FileObject.create("MsgBox").toString());

		try
		{
			/*
			 * Commands as one String example
			 */
			AHKManager.runAHKCommands("MsgBox, Test!\nMsgBox, Test2!", true);

			/*
			 * Commands as separate String example
			 */
			final ComplexString cmds = new ComplexString();
			cmds.add("MsgBox, Test!");
			cmds.add("MsgBox, Test2!");
			AHKManager.runAHKCommands(cmds, true);

			/*
			 * File in the working directory. File extension essential, see create method!
			 */
			AHKManager.runAHKFile(FileObject.create("MsgBox.ahk"), true);

			/*
			 * File in the src directory. File extension doesn't matter!
			 */
			AHKManager.runEmbeddedAHKFile(AHKTester.class, "MsgBox", true);

			/*
			 * File somewhere else, e.g. last used file. File extension doesn't matter!
			 */
			AHKManager.runAHKFile(FileObject.create().setPath(AHKManager.getTempFolder().toString()).setFullName("MsgBox"), true);

			System.out.println("DONE!");
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

}
