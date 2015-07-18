package de.mixedfx.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import de.mixedfx.file.FileObject;

public class LayoutTester
{
	public static void main(final String[] args)
	{
		// INITIALISIERUNG IM LANTOOL
		// Feststellen, welche Elemente das LanTool hat, z. B. Bilder für Buttons
		final List<LayoutElement<?>> layoutElements = new ArrayList<>();
		final LayoutElement<Image> ll = new LayoutElement<Image>("redButton", Image.class);
		layoutElements.add(ll);
		final LayoutElement<File> ll2 = new LayoutElement<File>("style", File.class);
		layoutElements.add(ll2);

		// Erstellen eines LayoutManagers mit dem man im LanTool dynamisch das Layout wechseln kann
		final LayoutManager lm = new LayoutManager(FileObject.create().setPath("assets\\layouts"), layoutElements);
		lm.applyLayout("HAMMER");

		// IRGENDWO IM CODE im LanTool
		final ImageView view = new ImageView();
		view.imageProperty().bind(ll.object);
	}
}
