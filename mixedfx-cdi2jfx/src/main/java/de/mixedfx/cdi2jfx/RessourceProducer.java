package de.mixedfx.cdi2jfx;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;

public class RessourceProducer {
    @Inject
    private BeanManager manager;

    @Produces
    public FXMLLoader buildLoader() {
	FXMLLoader loader = new FXMLLoader();
	loader.setControllerFactory(new Callback<Class<?>, Object>() {

	    @SuppressWarnings("unchecked")
	    @Override
	    public Object call(Class<?> param) {
		CDI.current().select(param).get();
		Bean<?> bean = RessourceProducer.this.manager.getBeans(param).iterator().next();
		CreationalContext<Object> ctx = RessourceProducer.this.manager.createCreationalContext(null);
		return RessourceProducer.this.manager.getContext(bean.getScope()).get((Contextual<Object>) bean, ctx);
	    }

	});
	return loader;
    }

}
