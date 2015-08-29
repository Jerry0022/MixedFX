package de.mixedfx.test;

import java.io.IOException;

import de.mixedfx.file.DataHandler;
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
			AHKManager.runAHKCommands("MsgBox, Test %A_IsAdmin%!\nMsgBox, Test2 %A_IsAdmin%!", false);

			System.out.println("GO ON");

			/*
			 * Commands as separate String example
			 */
			final ComplexString cmds = new ComplexString();
			cmds.add("MsgBox, Test! %A_IsAdmin%");
			cmds.add("MsgBox, Test2! %A_IsAdmin%");
			AHKManager.runAHKCommands(cmds, true);

			/*
			 * File in the src directory. File extension doesn't matter!
			 */
			AHKManager.runEmbeddedAHKFile(AHKTester.class, "MsgBox", true);

			/*
			 * File somewhere else, e.g. last used file. File extension doesn't matter!
			 */
			AHKManager.runAHKFile(FileObject.create().setPath(DataHandler.getTempFolder().toString()).setFullName("MsgBox"), false);

			/*
			 * File in the working directory (usually project directory). File extension essential, see create method!
			 */
			AHKManager.runAHKFile(FileObject.create("MsgBox.ahk"), false);

			System.out.println("Done :)");
		} catch (final IOException e)
		{
			System.out.println("Error detected!");
			e.printStackTrace();
		}
	}

}
