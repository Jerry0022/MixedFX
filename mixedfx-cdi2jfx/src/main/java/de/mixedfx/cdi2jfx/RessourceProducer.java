package de.mixedfx.cdi2jfx;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;

public class RessourceProducer {
    @Inject
    private Instance<Object> instancer;

    @Produces
    public FXMLLoader buildLoader() {
	FXMLLoader loader = new FXMLLoader();
	loader.setControllerFactory(new Callback<Class<?>, Object>() {

	    @Override
	    public Object call(Class<?> param) {
		return RessourceProducer.this.instancer.select(param);
	    }

	});
	return loader;
    }

}
