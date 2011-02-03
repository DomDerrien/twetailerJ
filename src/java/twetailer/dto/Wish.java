package twetailer.dto;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import domderrien.jsontools.JsonObject;

/**
 * Define the attributes of a consumer wish
 *
 * @see twetailer.dto.Command
 * @see twetailer.dto.Request
 * @see twetailer.dto.Demand
 * @see twetailer.dto.Consumer
 * @see twetailer.dto.Payment
 * @see twetailer.dto.Proposal
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Wish extends Request {

    /** Default constructor */
    public Wish() {
        super();
    }

    /**
     * Creates a wish
     *
     * @param in HTTP request parameters
     */
    public Wish(JsonObject in) {
        this();
        fromJson(in);
    }

    /**
     * Provided to reproduce the JDO behavior with Unit tests
     *
     * @return Object instance for chaining
     */
    protected Wish resetLists() {
        super.resetLists();
        return this;
    }
}
