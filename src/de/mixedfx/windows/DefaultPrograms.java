package de.mixedfx.windows;

import de.mixedfx.file.FileObject;

public class DefaultPrograms
{
	public static final Program	HAMACHI	= new Program(FileObject.create().setPath("C:\\Program Files (x86)\\LogMeIn Hamachi").setFullName("hamachi-2-ui.exe"), "Hamachi-2.exe", "Hamachi2Svc");
	public static final Program	TUNNGLE	= new Program(FileObject.create().setPath("C:\\Program Files (x86)\\Tunngle").setFullName("Launcher.exe"), "Tunngle.exe", "TunngleService");
}
