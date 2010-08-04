package twetailer.dto;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

/**
 * Base class for all Twetailer entities (Seed excepted) stored in the back-end infrastructure
 *
 * @see twetailer.dto.Command
 * @see twetailer.dto.Consumer
 * @see twetailer.dto.Location
 * @see twetailer.dto.Payment
 * @see twetailer.dto.RawCommand
 * @see twetailer.dto.SaleAssociate
 * @see twetailer.dto.Seed
 * @see twetailer.dto.Settings
 * @see twetailer.dto.Store
 *
 * @author Dom Derrien
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public class Entity implements TransferObject {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    public static final String KEY = "key";

    @Persistent
    private Date creationDate;

    public static final String CREATION_DATE = "creationDate";

    @Persistent
    private Long locationKey;

    public final static String LOCATION_KEY = Location.LOCATION_KEY;

    @Persistent
    private Boolean markedForDeletion = Boolean.FALSE;

    public static final String MARKED_FOR_DELETION = "markedForDeletion";

    @Persistent
    private Date modificationDate;

    public static final String MODIFICATION_DATE = "modificationDate";

    /** Default constructor */
    public Entity() {
        Date now = DateUtils.getNowDate();
        setCreationDate(now);
        setModificationDate(now);
    }

    /**
     * Creates a consumer
     *
     * @param in HTTP request parameters
     */
    public Entity(JsonObject in) {
        this();
        fromJson(in);
    }

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        if (key == null) {
            resetKey();
        }
        else if (this.key == null) {
            if (key != 0L) {
                this.key = key;
            }
        }
        else if (!this.key.equals(key)) {
            throw new IllegalArgumentException("Cannot override the key of an object with a new one");
        }
    }

    public void resetKey() {
        this.key = null;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        if (creationDate == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'creationDate'");
        }
        this.creationDate = creationDate;
    }

    public void resetCoreDates() {
        Date now = DateUtils.getNowDate();
        setCreationDate(now);
        setModificationDate(now);
    }

    public Long getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(Long locationKey) {
        if (locationKey == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'locationKey'");
        }
        this.locationKey = locationKey;
    }

    public Boolean getMarkedForDeletion() {
        return markedForDeletion == null ? Boolean.FALSE : markedForDeletion;
    }

    public void setMarkedForDeletion(Boolean markedForDeletion) {
        this.markedForDeletion = markedForDeletion;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void updateModificationDate() {
        Date now = DateUtils.getNowDate();
        setModificationDate(now);
    }

    public void setModificationDate(Date modificationDate) {
        if (modificationDate == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'modificationDate'");
        }
        this.modificationDate = modificationDate;
    }

    public JsonObject toJson() {
        JsonObject out = new GenericJsonObject();
        if (getKey() != null) {
            out.put(KEY, getKey());
        }
        out.put(CREATION_DATE, DateUtils.dateToISO(getCreationDate()));
        if (getLocationKey() != null) {
            out.put(LOCATION_KEY, getLocationKey());
        }
        out.put(MARKED_FOR_DELETION, getMarkedForDeletion());
        out.put(MODIFICATION_DATE, DateUtils.dateToISO(getModificationDate()));
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        if (in.containsKey(KEY)) {
            setKey(in.getLong(KEY));
        }
        // if (in.containsKey(CREATION_DATE)) { ... } // Cannot be set manually
        if (in.containsKey(LOCATION_KEY)) { setLocationKey(in.getLong(LOCATION_KEY)); }
        // if (in.containsKey(MARKED_FOR_DELETION)) { ... } // Cannot be set manually
        updateModificationDate();
        return this;
    }
}
