package de.mixedfx.gui;

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

		final LayoutElement<Image> ll = new LayoutElement<>("redButton", Image.class);
		layoutElements.add(ll);
		final LayoutElement<String> ll2 = new LayoutElement<>("style", String.class);
		layoutElements.add(ll2);
		final LayoutElement<Integer> color = new LayoutElement<>("meineFarbe", Integer.class);
		layoutElements.add(color);

		// Erstellen eines LayoutManagers mit dem man im LanTool dynamisch das Layout wechseln kann
		final LayoutManager lm = new LayoutManager(FileObject.create().setPath("assets\\layouts"), layoutElements, "BlueMoon");

		// Wenn man das Layout ändert
		color.set(new Integer(5));
		lm.applyLayout("HAMMER");
		ll2.set("KRASS");
		color.set(new Integer(3));

		// IRGENDWO IM CODE im LanTool
		final ImageView view = new ImageView();
		view.imageProperty().bind(ll);
	}
}
