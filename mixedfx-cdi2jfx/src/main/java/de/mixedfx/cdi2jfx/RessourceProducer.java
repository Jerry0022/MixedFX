package de.mixedfx.cdi2jfx;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;

public class RessourceProducer {

    @Produces
    public FXMLLoader buildLoader() {
	FXMLLoader loader = new FXMLLoader();
	loader.setControllerFactory(new Callback<Class<?>, Object>() {

	    @Override
	    public Object call(Class<?> param) {
		return CDI.current().select(param);
	    }

	});
	return loader;
    }

}
