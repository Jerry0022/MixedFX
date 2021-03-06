package de.mixedfx.gui;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;

import java.util.Set;

public class Blurrer {
    private final static String STYLECLASS_EXCEPT = Blurrer.class.getName().replace(".", "-").concat("EXCEPTME");
    private final static String STYLECLASS_BLURRED = Blurrer.class.getName().replace(".", "-").concat("EFFECT");
    private static EventHandler<MouseEvent> mouseCatcher;

    static {
        Blurrer.mouseCatcher = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                event.consume();
            }
        };
    }

    /**
     * First unblurs all of this blurring effected nodes. Then blurs all nodes in an efficient way except the argument.
     *
     * @param exceptMe The one which shall not be blurred.
     */
    public static void blur(Node exceptMe) {
        // Catch all Mouse Events!
        exceptMe.getScene().addEventFilter(MouseEvent.ANY, mouseCatcher);

        // Blur and darken effects
        final BoxBlur effect = new BoxBlur();
        effect.setIterations(3);
        ColorAdjust darker = new ColorAdjust();
        darker.setBrightness(-0.6);
        effect.setInput(darker);

        // Apply effects on all elements except the one to show
        exceptMe.getStyleClass().add(STYLECLASS_EXCEPT);
        Node parent = exceptMe.getParent();
        while (parent != null) {
            if (parent instanceof Parent) {
                for (Node child : ((Parent) parent).getChildrenUnmodifiable()) {
                    if (child.lookup("." + STYLECLASS_EXCEPT) == null) {
                        child.getStyleClass().add(STYLECLASS_BLURRED);
                        child.setEffect(effect);
                    }
                }
            }
            parent = parent.getParent();
        }
        exceptMe.getStyleClass().remove(STYLECLASS_EXCEPT);
    }

    /**
     * Stop blurring.
     *
     * @param exceptMe The one on which {@link Blurrer#blur(Node)} was called.
     */
    public static void unBlur(Node exceptMe) {
        // Catch all Mouse Events!
        exceptMe.getScene().removeEventFilter(MouseEvent.ANY, mouseCatcher);

        Set<Node> blurredNodes = exceptMe.getScene().getRoot().lookupAll("." + STYLECLASS_BLURRED);
        for (Node node : blurredNodes) {
            node.getStyleClass().remove(STYLECLASS_BLURRED);
            node.setEffect(null);
        }
    }
}
