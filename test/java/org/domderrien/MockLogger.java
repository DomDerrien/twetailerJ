package org.domderrien;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class MockLogger extends Logger {

	public MockLogger(String name, String resourceBundleName) {
		super(name, resourceBundleName);
	}
	
	@Override
	public void log(LogRecord record) {
		record.toString();
	}
	
	@Override
	public Level getLevel() {
		return Level.FINEST;
	}
	
	@Override
	public boolean isLoggable(Level level) {
		return true;
	}

}
