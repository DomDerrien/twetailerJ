package com.twetailer.j2ee;

import java.util.logging.Logger;

import org.domderrien.jsontools.JsonArray;
import org.domderrien.jsontools.JsonObject;

import com.google.appengine.api.users.User;
import com.twetailer.DataSourceException;

@SuppressWarnings("serial")
public class ProductsServlet extends BaseRESTServlet {
	private static final Logger log = Logger.getLogger(ProductsServlet.class.getName());

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	protected String createResource(JsonObject parameters, User loggedUser) throws DataSourceException {
		return null;
	}

	@Override
	protected void deleteResource(String resourceId, User loggedUser) throws DataSourceException {
	}

	@Override
	protected JsonObject getResource(JsonObject parameters, String resourceId, User loggedUser) throws DataSourceException {
		return null;
	}

	@Override
	protected JsonArray selectResources(JsonObject parameters) throws DataSourceException {
		return null;
	}

	@Override
	protected void updateResource(JsonObject parameters, String resourceId, User loggedUser) throws DataSourceException {
	}
}
