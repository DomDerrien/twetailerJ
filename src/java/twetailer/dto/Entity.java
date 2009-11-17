package twetailer.dto;

import java.text.ParseException;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import domderrien.i18n.DateUtils;
import domderrien.jsontools.GenericJsonObject;
import domderrien.jsontools.JsonObject;
import domderrien.jsontools.TransferObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Entity implements TransferObject {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    public static final String KEY = "key";

    @Persistent
    private Date creationDate = DateUtils.getNowDate();

    public static final String CREATION_DATE = "creationDate";

    @Persistent
    private Boolean markedForDeletion = Boolean.FALSE;

    public static final String MARKED_FOR_DELETION = "markedForDeletion";

    @Persistent
    private Date modificationDate = DateUtils.getNowDate();

    public static final String MODIFICATION_DATE = "modificationDate";

    /** Default constructor */
    public Entity() {
        setCreationDate(DateUtils.getNowDate());
        setModificationDate(DateUtils.getNowDate());
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
            throw new IllegalArgumentException("Cannot nullify the attribute 'creationDate' of type Date reference");
        }
        this.creationDate = creationDate;
    }

    public void resetCoreDates() {
        setCreationDate(DateUtils.getNowDate());
        setModificationDate(getCreationDate());
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void updateModificationDate() {
        setModificationDate(DateUtils.getNowDate());
    }

    public void setModificationDate(Date modificationDate) {
        if (modificationDate == null) {
            throw new IllegalArgumentException("Cannot nullify the attribute 'modificationDate' of type Date reference");
        }
        this.modificationDate = modificationDate;
    }

    public Boolean getMarkedForDeletion() {
        return markedForDeletion;
    }

    public void setMarkedForDeletion(Boolean markedForDeletion) {
        this.markedForDeletion = markedForDeletion;
    }

    public JsonObject toJson() {
        JsonObject out = new GenericJsonObject();
        if (getKey() != null) {
            out.put(KEY, getKey());
        }
        out.put(CREATION_DATE, DateUtils.dateToISO(getCreationDate()));
        out.put(MODIFICATION_DATE, DateUtils.dateToISO(getModificationDate()));
        out.put(MARKED_FOR_DELETION, getMarkedForDeletion());
        return out;
    }

    public TransferObject fromJson(JsonObject in) {
        if (in.containsKey(KEY)) {
            setKey(in.getLong(KEY));
        }
        if (in.containsKey(CREATION_DATE)) {
            try {
                Date creationDate = DateUtils.isoToDate(in.getString(CREATION_DATE));
                setCreationDate(creationDate);
            }
            catch (ParseException e) {
                // Ignored error, the date stays not set
            }
        }
        updateModificationDate();
        // Don't accept deletion here by external source because of possible side-effects
        // However, each final class is free to accept it if the side-effects are controlled
        // if (in.containsKey(MARKED_FOR_DELETION)) {
        //    setMarkedForDeletion(in.getBoolean(MARKED_FOR_DELETION));
        // }
        return this;
    }
}
