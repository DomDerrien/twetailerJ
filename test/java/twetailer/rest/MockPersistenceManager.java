package twetailer.rest;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

import javax.jdo.Extent;
import javax.jdo.FetchGroup;
import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.listener.InstanceLifecycleListener;

public class MockPersistenceManager implements PersistenceManager {

	@SuppressWarnings("unchecked")
	public void addInstanceLifecycleListener(InstanceLifecycleListener arg0, Class... arg1) {}
	public void checkConsistency() {}

	private boolean closed = false;

	public void close() {
		closed = true;
	}

	public Transaction currentTransaction() {
		return null;
	}

	public void deletePersistent(Object arg0) {}
	public void deletePersistentAll(Object... arg0) {}
	@SuppressWarnings("unchecked") public void deletePersistentAll(Collection arg0) {}

	public <T> T detachCopy(T arg0) {
		return null;
	}

	public <T> Collection<T> detachCopyAll(Collection<T> arg0) {
		return null;
	}

	public <T> T[] detachCopyAll(T... arg0) {
		return null;
	}

	public void evict(Object arg0) {}
	public void evictAll() {}
	public void evictAll(Object... arg0) {}
	@SuppressWarnings("unchecked") public void evictAll(Collection arg0) {}
	@SuppressWarnings("unchecked") public void evictAll(boolean arg0, Class arg1) {}
	public void flush() {}

	public boolean getCopyOnAttach() {
		return false;
	}

	public JDOConnection getDataStoreConnection() {
		return null;
	}

	public boolean getDetachAllOnCommit() {
		return false;
	}

	public <T> Extent<T> getExtent(Class<T> arg0) {
		return null;
	}

	public <T> Extent<T> getExtent(Class<T> arg0, boolean arg1) {
		return null;
	}

	@SuppressWarnings("unchecked") public FetchGroup getFetchGroup(Class arg0, String arg1) {
		return null;
	}

	public FetchPlan getFetchPlan() {
		return null;
	}

	public boolean getIgnoreCache() {
		return false;
	}

	@SuppressWarnings("unchecked") public Set getManagedObjects() {
		return null;
	}

	@SuppressWarnings("unchecked") public Set getManagedObjects(EnumSet<ObjectState> arg0) {
		return null;
	}

	@SuppressWarnings("unchecked") public Set getManagedObjects(Class... arg0) {
		return null;
	}

	@SuppressWarnings("unchecked") public Set getManagedObjects(EnumSet<ObjectState> arg0, Class... arg1) {
		return null;
	}

	public boolean getMultithreaded() {
		return false;
	}

	public Object getObjectById(Object arg0) {
		return null;
	}

	public Object getObjectById(Object arg0, boolean arg1) {
		return null;
	}

	public <T> T getObjectById(Class<T> arg0, Object arg1) {
		return null;
	}

	public Object getObjectId(Object arg0) {
		return null;
	}

	@SuppressWarnings("unchecked") public Class getObjectIdClass(Class arg0) {
		return null;
	}

	@SuppressWarnings("unchecked") public Collection getObjectsById(Collection arg0) {
		return null;
	}

	public Object[] getObjectsById(Object... arg0) {
		return null;
	}

	@SuppressWarnings("unchecked") public Collection getObjectsById(Collection arg0, boolean arg1) {
		return null;
	}

	public Object[] getObjectsById(Object[] arg0, boolean arg1) {
		return null;
	}

	public Object[] getObjectsById(boolean arg0, Object... arg1) {
		return null;
	}

	public PersistenceManagerFactory getPersistenceManagerFactory() {
		return null;
	}

	public Sequence getSequence(String arg0) {
		return null;
	}

	public Date getServerDate() {
		return null;
	}

	public Object getTransactionalObjectId(Object arg0) {
		return null;
	}

	public Object getUserObject() {
		return null;
	}

	public Object getUserObject(Object arg0) {
		return null;
	}

	public boolean isClosed() {
		return closed;
	}

	public void makeNontransactional(Object arg0) {}
	public void makeNontransactionalAll(Object... arg0) {}
	@SuppressWarnings("unchecked") public void makeNontransactionalAll(Collection arg0) {}

	Object persistedObject;
	
	public <T> T makePersistent(T arg0) {
		persistedObject = arg0;
		return arg0;
	}
	
	public Object getPersistedObject() {
		return persistedObject;
	}

	public <T> T[] makePersistentAll(T... arg0) {
		return null;
	}

	public <T> Collection<T> makePersistentAll(Collection<T> arg0) {
		return null;
	}

	public void makeTransactional(Object arg0) {}
	public void makeTransactionalAll(Object... arg0) {}
	@SuppressWarnings("unchecked") public void makeTransactionalAll(Collection arg0) {}
	public void makeTransient(Object arg0) {}
	public void makeTransient(Object arg0, boolean arg1) {}
	public void makeTransientAll(Object... arg0) {}
	@SuppressWarnings("unchecked") public void makeTransientAll(Collection arg0) {}
	public void makeTransientAll(Object[] arg0, boolean arg1) {}
	public void makeTransientAll(boolean arg0, Object... arg1) {}
	@SuppressWarnings("unchecked") public void makeTransientAll(Collection arg0, boolean arg1) {}

	public <T> T newInstance(Class<T> arg0) {
		return null;
	}

	@SuppressWarnings("unchecked") public Query newNamedQuery(Class arg0, String arg1) {
		return null;
	}

	@SuppressWarnings("unchecked") public Object newObjectIdInstance(Class arg0, Object arg1) {
		return null;
	}

	public Query newQuery() {
		return null;
	}

	public Query newQuery(Object arg0) {
		return null;
	}

	public Query newQuery(String arg0) {
		return null;
	}

	@SuppressWarnings("unchecked") public Query newQuery(Class arg0) {
		return null;
	}

	@SuppressWarnings("unchecked") public Query newQuery(Extent arg0) {
		return null;
	}

	public Query newQuery(String arg0, Object arg1) {
		return null;
	}

	@SuppressWarnings("unchecked") public Query newQuery(Class arg0, Collection arg1) {
		return null;
	}

	@SuppressWarnings("unchecked") public Query newQuery(Class arg0, String arg1) {
		return null;
	}

	@SuppressWarnings("unchecked") public Query newQuery(Extent arg0, String arg1) {
		return null;
	}

	@SuppressWarnings("unchecked") public Query newQuery(Class arg0, Collection arg1, String arg2) {
		return null;
	}

	public Object putUserObject(Object arg0, Object arg1) {
		return null;
	}

	public void refresh(Object arg0) {}
	public void refreshAll() {}
	public void refreshAll(Object... arg0) {}
	@SuppressWarnings("unchecked") public void refreshAll(Collection arg0) {}
	public void refreshAll(JDOException arg0) {}
	public void removeInstanceLifecycleListener(InstanceLifecycleListener arg0) {}

	public Object removeUserObject(Object arg0) {
		return null;
	}

	public void retrieve(Object arg0) {}
	public void retrieve(Object arg0, boolean arg1) {}
	@SuppressWarnings("unchecked") public void retrieveAll(Collection arg0) {}
	public void retrieveAll(Object... arg0) {}
	@SuppressWarnings("unchecked") public void retrieveAll(Collection arg0, boolean arg1) {}
	public void retrieveAll(Object[] arg0, boolean arg1) {}
	public void retrieveAll(boolean arg0, Object... arg1) {}
	public void setCopyOnAttach(boolean arg0) {}
	public void setDetachAllOnCommit(boolean arg0) {}
	public void setIgnoreCache(boolean arg0) {}
	public void setMultithreaded(boolean arg0) {}
    public void setQueryTimeoutMillis(Integer arg0) {}
	public void setUserObject(Object arg0) {}
    public Integer getQueryTimeoutMillis() {
        return null;
    }
}
