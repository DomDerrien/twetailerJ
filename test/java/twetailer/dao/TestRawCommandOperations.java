package twetailer.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import twetailer.ClientException;
import twetailer.DataSourceException;
import twetailer.dto.RawCommand;

public class TestRawCommandOperations {

    private MockAppEngineEnvironment mockAppEngineEnvironment;

    @Before
    public void setUp() throws Exception {
        mockAppEngineEnvironment = new MockAppEngineEnvironment();
        mockAppEngineEnvironment.setUp();

        BaseOperations.setPersistenceManagerFactory(mockAppEngineEnvironment.getPersistenceManagerFactory());
    }

    @After
    public void tearDown() throws Exception {
        mockAppEngineEnvironment.tearDown();
    }

    @Test
    public void testGetLogger() throws IOException {
        Logger log1 = new RetailerOperations().getLogger();
        assertNotNull(log1);
        Logger log2 = new RetailerOperations().getLogger();
        assertNotNull(log2);
        assertEquals(log1, log2);
    }

    @Test
    public void testCreateI() {
        RawCommand item = new RawCommandOperations().createRawCommand(new RawCommand());
        assertNotNull(item.getKey());
    }

    @Test(expected=RuntimeException.class)
    public void testCreateII() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        RawCommandOperations ops = new RawCommandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
            @Override
            public RawCommand createRawCommand(PersistenceManager pm, RawCommand rawCommand) {
                throw new RuntimeException("Done in purpose");
            }
        };

        ops.createRawCommand(new RawCommand());
    }

    @Test
    public void testGetI() throws DataSourceException {
        RawCommandOperations ops = new RawCommandOperations();
        RawCommand item = ops.createRawCommand(new RawCommand());

        RawCommand selected = ops.getRawCommand(item.getKey());
        assertNotNull(selected);
        assertEquals(item.getKey(), selected.getKey());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetII() throws DataSourceException {
        new RawCommandOperations().getRawCommand(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetIII() throws DataSourceException {
        new RawCommandOperations().getRawCommand(0L);
    }

    @Test(expected=DataSourceException.class)
    public void testGetIV() throws DataSourceException {
        new RawCommandOperations().getRawCommand(888L);
    }

    @Test
    public void testUpdate() throws ClientException, DataSourceException {
        final PersistenceManager pm = mockAppEngineEnvironment.getPersistenceManager();
        RawCommandOperations ops = new RawCommandOperations() {
            @Override
            public PersistenceManager getPersistenceManager() {
                return pm; // Return always the same object to be able to verify it has been closed
            }
        };
        RawCommand object = new RawCommand();
        object.setEmitterId("emitter");
        object = ops.createRawCommand(pm, object); // Gives the PersistenceManager so it won't be closed
        object.setEmitterId(null);
        object.setErrorMessage("error");

        RawCommand updated = ops.updateRawCommand(object);
        assertNotNull(updated);
        assertEquals(object.getKey(), updated.getKey());
        assertNull(object.getEmitterId());
        assertEquals("error", object.getErrorMessage());
        assertTrue(pm.isClosed());
    }
}
