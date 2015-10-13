package de.mixedfx.gui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2(topic = "GUI")
public class EasyModifier {
    /**
     * Trigger to go in modifying mode is a boolean change!
     *
     * @param root    The root to manipulate, all listeners and events of all sub nodes will still work.
     * @param config  The config to be used!
     * @param handler The handler which shall apply as long as the root is in modifying node! DoIt signalizes if the Parent is in modifying mode.
     */
    public static void init(final Parent root, final BooleanProperty trigger, final EasyModifierConfig config, final EasyModifierHandler handler) {
        final EasyModifierHandler styleHandler = (parent, doIt) ->
        {
            if (doIt) {
                parent.getStyleClass().add(config.dynamicClass);
            }
            else {
                parent.getStyleClass().remove(config.dynamicClass);
            }
        };

        trigger.addListener((observable, oldValue, newValue) -> EasyModifier.runOnAllSubNodes(root, config.staticClass, newValue, styleHandler, handler));
    }

    /**
     * Trigger to go in modifying mode is a key pressed or released on the full root!
     *
     * @param root       The root to manipulate, all listeners and events of all sub nodes will still work.
     * @param triggerKey The key which shall trigger this action! Null means any key.
     * @param config     The config to be used!
     * @param handler    The handler which shall apply as long as the root is in modifying node! DoIt signalizes if the Parent is in modifying mode.
     */
    public static void init(final Parent root, final KeyCode triggerKey, final EasyModifierConfig config, final EasyModifierHandler handler) {
        final BooleanProperty trigger = new SimpleBooleanProperty();
        EasyModifier.init(root, trigger, config, handler);

        if (config.keyPressed) {
            root.setOnKeyPressed(ke ->
            {
                if ((triggerKey == null) || ke.getCode().equals(triggerKey)) {
                    trigger.set(true);
                }
            });
            root.setOnKeyReleased(ke ->
            {
                if ((triggerKey == null) || ke.getCode().equals(triggerKey)) {
                    trigger.set(false);
                }
            });
        }
        else {
            root.setOnKeyReleased(ke ->
            {
                if ((triggerKey == null) || ke.getCode().equals(triggerKey)) {
                    trigger.set(!trigger.get());
                }
            });
        }
    }

    /**
     * Returns a list of nodes which match the style class as well as they must have an id (which id doesn't matter)!
     *
     * @param root       The starting node!
     * @param styleClass The css style class which must be actively applied to each node as identifier which nodes shall be modified!
     * @param doIt       This value will be forwarded to {@link EasyModifierHandler#modify(Parent, boolean)}!
     * @param modifier   If not null, {@link EasyModifierHandler#modify(Parent, boolean)} is called on all matching Parents. If null no actions are taken, use the returned list instead.
     * @return Returns a list of all matched childrens and childrens of childrens etc. (including the root).
     */
    public static List<Parent> runOnAllSubNodes(final Parent root, final String styleClass, final boolean doIt, final EasyModifierHandler... modifier) {
        return EasyModifier.runOnAllSubNodes(root, styleClass, doIt, new ArrayList<>(), modifier);
    }

    private static List<Parent> runOnAllSubNodes(final Parent root, final String styleClass, final boolean doIt, final List<Parent> emptyList, final EasyModifierHandler... modifier) {
        if (root.getStyleClass().contains(styleClass))
            if ((root.getId() != "") && (root.getId() != null)) {
                if (modifier != null)
                    for (final EasyModifierHandler mod : modifier)
                        mod.modify(root, doIt);
                emptyList.add(root);
            }
            else {
                log.warn("The node " + root + " has no id but has the identifying style class " + styleClass + ". A modifying operation can only perform if the id is uniquely filled.");
            }

        if (root.getChildrenUnmodifiable().size() > 0) {
            for (final Node node : root.getChildrenUnmodifiable()) {
                if (node instanceof Parent) {
                    EasyModifier.runOnAllSubNodes((Parent) node, styleClass, doIt, emptyList, modifier);
                }
                else if (root.getStyleClass().contains(styleClass))
                    log.warn("The node " + node + " is not a Parent but has the identifying style class " + styleClass + ". Only javafx.scene.Parent are supported!");
            }
        }
        return emptyList;
    }

}
