package twetailer.dto;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Logger;

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
 * Base class for all Twetailer entities stored in the back-end infrastructure
 *
 * @see twetailer.dto.Command
 * @see twetailer.dto.Consumer
 * @see twetailer.dto.Location
 * @see twetailer.dto.Payment
 * @see twetailer.dto.RawCommand
 * @see twetailer.dto.SaleAssociate
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

    @Persistent
    private String tracking;

    public static final String TRACKING = "_tracking";

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

    /**
     * Provided to reproduce the JDO behavior with Unit tests
     *
     * @return Object instance for chaining
     */
    protected Entity resetLists() {
        key = null;
        creationDate = null;
        modificationDate = null;
        return this;
    }

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        setKey(key, false);
    }

    public void setKey(Long key, boolean isCacheRelated) {
        if (key == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'key'");
        }
        if (this.key != null && !this.key.equals(key) && !isCacheRelated) {
            throw new IllegalArgumentException("Cannot override the key of an object with a new one");
        }
        if (key != 0L) {
            this.key = key;
        }
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
        if (modificationDate == null) {
            return creationDate;
        }
        return modificationDate;
    }

    public void updateModificationDate() {
        if (key != null) {
            setModificationDate(DateUtils.getNowDate());
        }
    }

    public void setModificationDate(Date modificationDate) {
        if (modificationDate == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'modificationDate'");
        }
        this.modificationDate = modificationDate;
    }

    public String getTracking() {
        return tracking;
    }

    public void setTracking(String tracking) {
        this.tracking = tracking;
    }

    @Override
    public JsonObject toJson() {
        JsonObject out = new GenericJsonObject();
        if (getKey() != null) { out.put(KEY, getKey()); }
        if (getCreationDate() != null) { out.put(CREATION_DATE, DateUtils.dateToISO(getCreationDate())); }
        if (getLocationKey() != null) { out.put(LOCATION_KEY, getLocationKey()); }
        out.put(MARKED_FOR_DELETION, getMarkedForDeletion());
        if (getModificationDate() != null) { out.put(MODIFICATION_DATE, DateUtils.dateToISO(getModificationDate())); }
        if (getTracking() != null && 0 < getTracking().length()) { out.put(TRACKING, getTracking(), true); }
        return out;
    }

    @Override
    public TransferObject fromJson(JsonObject in) {
        return fromJson(in, false, false);
    }

    public TransferObject fromJson(JsonObject in, boolean isUserAdmin, boolean isCacheRelated) {
        if (isCacheRelated) { isUserAdmin = isCacheRelated; }

        if ((getKey() == null || isCacheRelated) && in.containsKey(KEY)) { setKey(in.getLong(KEY), isCacheRelated); }
        if (isCacheRelated && in.containsKey(CREATION_DATE)) {
            try {
                Date importedCreationDate = DateUtils.isoToDate(in.getString(CREATION_DATE));
                setCreationDate(importedCreationDate);
            }
            catch (ParseException ex) {
                Logger.getLogger(Command.class.getName()).warning("Invalid format in due date: " + in.getString(CREATION_DATE) + ", for serialized consumer.key=" + getKey() + " -- message: " + ex.getMessage());
                setCreationDate(DateUtils.getNowDate());
            }
        }
        if (in.containsKey(LOCATION_KEY)) { setLocationKey(in.getLong(LOCATION_KEY)); }
        if (isCacheRelated && in.containsKey(MARKED_FOR_DELETION)) { setMarkedForDeletion(in.getBoolean(MARKED_FOR_DELETION)); }
        if (isCacheRelated && in.containsKey(MODIFICATION_DATE)) {
            try {
                Date importedModificationDate = DateUtils.isoToDate(in.getString(MODIFICATION_DATE));
                setModificationDate(importedModificationDate);
            }
            catch (ParseException ex) {
                Logger.getLogger(Command.class.getName()).warning("Invalid format in due date: " + in.getString(MODIFICATION_DATE) + ", for serialized consumer.key=" + getKey() + " -- message: " + ex.getMessage());
                updateModificationDate();
            }
        }
        else {
            updateModificationDate();
        }
        if (in.containsKey(TRACKING)) { setTracking(in.getString(TRACKING, true)); }

        return this;
    }
}
