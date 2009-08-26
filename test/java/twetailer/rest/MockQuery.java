package twetailer.rest;

import java.util.Collection;
import java.util.Map;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@SuppressWarnings("serial")
public class MockQuery implements Query {

	public void addExtension(String arg0, Object arg1) {}
	public void addSubquery(Query arg0, String arg1, String arg2) {}
	public void addSubquery(Query arg0, String arg1, String arg2, String arg3) {}

	public void addSubquery(Query arg0, String arg1, String arg2,
			String... arg3) {}

	@SuppressWarnings("unchecked") public void addSubquery(Query arg0, String arg1, String arg2, Map arg3) {}
	public void cancel() {}
	public void close(Object arg0) {}
	public void closeAll() {}
	public void compile() {}
	public void declareImports(String arg0) {}
	public void declareParameters(String arg0) {}
	public void declareVariables(String arg0) {}

	public long deletePersistentAll() {
		return 0;
	}

	public long deletePersistentAll(Object... arg0) {
		return 0;
	}

	@SuppressWarnings("unchecked") public long deletePersistentAll(Map arg0) {
		return 0;
	}

	public Object execute() {
		return null;
	}

	public Object execute(Object arg0) {
		return null;
	}

	public Object execute(Object arg0, Object arg1) {
		return null;
	}

	public Object execute(Object arg0, Object arg1, Object arg2) {
		return null;
	}

	public Object executeWithArray(Object... arg0) {
		return null;
	}

	@SuppressWarnings("unchecked") public Object executeWithMap(Map arg0) {
		return null;
	}

	public FetchPlan getFetchPlan() {
		return null;
	}

	public boolean getIgnoreCache() {
		return false;
	}

	public PersistenceManager getPersistenceManager() {
		return null;
	}

	public boolean isUnmodifiable() {
		return false;
	}

	@SuppressWarnings("unchecked") public void setCandidates(Extent arg0) {}
	@SuppressWarnings("unchecked") public void setCandidates(Collection arg0) {}
	@SuppressWarnings("unchecked") public void setClass(Class arg0) {}
	@SuppressWarnings("unchecked") public void setExtensions(Map arg0) {}
	public void setFilter(String arg0) {}
	public void setGrouping(String arg0) {}
	public void setIgnoreCache(boolean arg0) {}
	public void setOrdering(String arg0) {}
	public void setRange(String arg0) {}
	public void setRange(long arg0, long arg1) {}
	public void setResult(String arg0) {}
	@SuppressWarnings("unchecked") public void setResultClass(Class arg0) {}
	public void setTimeoutMillis(int arg0) {}
	public void setUnique(boolean arg0) {}
	public void setUnmodifiable() {}
    public void cancel(Thread arg0) {
        // TODO Auto-generated method stub
        
    }
    public void cancelAll() {}
    public Boolean getSerializeRead() {
        return null;
    }
    public Integer getTimeoutMillis() {
        return null;
    }
    public void setSerializeRead(Boolean arg0) {}
    public void setTimeoutMillis(Integer arg0) {}
}
