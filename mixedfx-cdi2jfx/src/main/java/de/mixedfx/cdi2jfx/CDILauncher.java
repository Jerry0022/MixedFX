package de.mixedfx.cdi2jfx;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javafx.application.Application;
import javafx.stage.Stage;

public abstract class CDILauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
	Weld weld = new Weld();
	WeldContainer container = weld.initialize();
	container.instance().select(this.getClass()).get().run(primaryStage);
    }

    public abstract void run(Stage primaryStage) throws Exception;
}
