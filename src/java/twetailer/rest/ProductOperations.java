package twetailer.rest;

import java.util.logging.Logger;

public class ProductOperations extends BaseOperations {
    private static final Logger log = Logger.getLogger(ProductOperations.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

}