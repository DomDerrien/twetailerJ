package twetailer.dao;

import javax.jdo.PersistenceManager;

import twetailer.InvalidIdentifierException;
import twetailer.dto.Influencer;
import twetailer.task.step.BaseSteps;
import domderrien.i18n.DateUtils;

/**
 * Controller defining various methods used for the CRUD operations on Influencer entities
 *
 * @author Dom Derrien
 */
public class InfluencerOperations extends BaseOperations {

    /**
     * Create the Influencer instance with the given parameters
     *
     * @param influencer Resource to persist
     * @return Just created resource
     *
     * @see InfluencerOperations#createInfluencer(PersistenceManager, Influencer)
     */
    public Influencer createInfluencer(Influencer influencer) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return createInfluencer(pm, influencer);
        }
        finally {
            pm.close();
        }
    }

    /**
     * Create the Influencer instance with the given parameters
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param influencer Resource to persist
     * @return Just created resource
     */
    public Influencer createInfluencer(PersistenceManager pm, Influencer influencer) {
        influencer = pm.makePersistent(influencer);
        influencer.setReferralId(generateReferralId(influencer.getKey()));
        influencer = pm.makePersistent(influencer);
        return influencer;
    }

    /**
     * Use the given key to get the corresponding Influencer instance
     *
     * @param key Identifier of the Influencer instance
     * @return First Influencer instance matching the given filter or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Influencer record
     *
     * @see InfluencerOperations#getInfluencer(PersistenceManager, Long)
     */
    public Influencer getInfluencer(Long key) throws InvalidIdentifierException {
        PersistenceManager pm = getPersistenceManager();
        try {
            return getInfluencer(pm, key);
        }
        finally {
            Boolean.TRUE.booleanValue(); // Stupid & harmless call to prevent false alarm from Cobertura & DataNucleus
            pm.close();
        }
    }

    /**
     * Use the given key to get the corresponding Influencer instance
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param key Identifier of the Influencer instance
     * @return First Influencer instance matching the given filter or <code>null</code>
     *
     * @throws InvalidIdentifierException If the given identifier does not match a valid Influencer record
     */
    public Influencer getInfluencer(PersistenceManager pm, Long key) throws InvalidIdentifierException {
        if (key == null || key.equals(Long.valueOf(Influencer.DEFAULT_REFERRAL_ID))) {
            Influencer defaultInfluencer = new Influencer();
            defaultInfluencer.setEmail("influencer-program@anothersocialeconomy.com");
            defaultInfluencer.setName("AnotherSocialEconomy.com");
            defaultInfluencer.setUrl("http://anothersocialeconomy.com/");
            return defaultInfluencer;
        }
        try {
            return pm.getObjectById(Influencer.class, key);
        }
        catch(Exception ex) {
            throw new InvalidIdentifierException("Error while retrieving Influencer instance for identifier: " + key + " -- ex: " + ex.getMessage(), ex);
        }
    }

    /**
     * Persist the given (probably updated) resource
     *
     * @param influencer Resource to update
     * @return Updated resource
     *
     * @see InfluencerOperations#updateInfluencer(PersistenceManager, Influencer)
     */
    public Influencer updateInfluencer(Influencer influencer) {
        PersistenceManager pm = getPersistenceManager();
        try {
            return updateInfluencer(pm, influencer);
        }
        finally {
            Boolean.TRUE.booleanValue(); // Stupid & harmless call to prevent false alarm from Cobertura & DataNucleus
            pm.close();
        }
    }

    /**
     * Persist the given (probably updated) resource while leaving the given persistence manager open for future updates
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param influencer Resource to update
     * @return Updated resource
     */
    public Influencer updateInfluencer(PersistenceManager pm, Influencer influencer) {
        return pm.makePersistent(influencer);
    }

    public static String generateReferralId(Long influencerKey) {
        Long salt = 798435422321L;
        Long reducedNow = DateUtils.getNowCalendar().getTimeInMillis() % salt;
        Long signature = influencerKey * reducedNow * influencerKey.hashCode();
        return influencerKey.toString() + String.valueOf(INFORMATION_SEPARATOR) + String.valueOf(Math.abs(signature));
    }

    public static final char INFORMATION_SEPARATOR = '-';

    /**
     * Verifies the referral identifier syntax and look-up into the
     * data store to be sure that the given identifier has not been forged
     *
     * @param pm Persistence manager instance to use - let open at the end to allow possible object updates later
     * @param referralId identifier to process
     * @return <code>true</code> if the identifier is really associated to an valid Influencer record
     */
    public boolean verifyReferralIdValidity(PersistenceManager pm, String referralId) {
        if (referralId == null) {
            return false;
        }
        if (Influencer.DEFAULT_REFERRAL_ID.equals(referralId)) {
            return true;
        }
        if (referralId.length() < 5) {
            return false;
        }
        int limit = referralId.length();
        int idx = 0, firstDash = -1, secondDash = -1;
        while (idx < limit) {
            char digit = referralId.charAt(idx);
            if (digit == INFORMATION_SEPARATOR) {
                if (firstDash == -1) { firstDash = idx; }
                else if (secondDash == -1) { secondDash = idx; }
                else { // More than two dashes
                    return false;
                }
            }
            else if (digit < '0' || '9' < digit) { // Not a digit
                return false;
            }
            idx ++;
        }
        if (secondDash == -1) { // Not exactly two dashes
            return false;
        }
        if (referralId.length() - secondDash != 3) { // Not exactly two variable digits
            return false;
        }
        try {
            Long influencerKey = Long.valueOf(referralId.substring(0, firstDash));
            Influencer influencer = BaseSteps.getInfluencerOperations().getInfluencer(pm, influencerKey);
            if (!influencer.getReferralId().equals(referralId.substring(0, secondDash))) {
                return false;
            }
        }
        catch(Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * Extracts the Influencer record key from the given referral identifier
     */
    public static Long getInfluencerKey(String referralId) {
        if (referralId == null || referralId.length() < 2) {
            return Long.valueOf(Influencer.DEFAULT_REFERRAL_ID);
        }
        // Assume the referralId is valid
        return Long.valueOf(referralId.substring(0, referralId.indexOf(InfluencerOperations.INFORMATION_SEPARATOR)));
    }

    /**
     * Extracts the variable part in the referral identifier (last two digits after the second dash)
     */
    public static Long getReferralVariableIndex(String referralId) {
        if (referralId == null || referralId.length() < 2) {
            return Long.valueOf(Influencer.DEFAULT_REFERRAL_ID);
        }
        // Assume the referralId is valid
        return Long.valueOf(referralId.substring(referralId.lastIndexOf(InfluencerOperations.INFORMATION_SEPARATOR) + 1));
    }
}
