package twetailer.j2ee;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.wave.api.Blip;
import com.google.wave.api.Event;
import com.google.wave.api.EventType;
import com.google.wave.api.MockBlip;
import com.google.wave.api.MockEvent;
import com.google.wave.api.MockTextView;
import com.google.wave.api.MockWavelet;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.TextView;
import com.google.wave.api.Wavelet;

public class TestWaveResponderServlet {

    class MockRobotMessageBundle implements RobotMessageBundle {
        public boolean blipHasChanged(Blip blip) { return false; }
        public Wavelet createWavelet(List<String> participants) { return null; }
        public Wavelet createWavelet(List<String> participants, String dataDocumentWriteBack) { return null; }
        public List<Event> filterEventsByType(EventType eventType) { return null; }
        public Blip getBlip(String waveId, String waveletId, String blipId) { return null; }
        public List<Event> getBlipSubmittedEvents() { return null; }
        public List<Event> getEvents() { return null; }
        public List<Event> getParticipantsChangedEvents() { return null; }
        public String getRobotAddress() { return null; }
        public Wavelet getWavelet() { return null; }
        public Wavelet getWavelet(String waveId, String waveletId) { return null; }
        public boolean isNewWave() { return false; }
        public boolean wasParticipantAddedToNewWave(String participantId) { return false; }
        public boolean wasParticipantAddedToWave(String participantId) { return false; }
        public boolean wasSelfAdded() { return false; }
        public boolean wasSelfRemoved() { return false; }
    };

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructor() {
        new WaveResponderServlet();
    }

    @Test
    public void testProcessI() {
        new WaveResponderServlet().processEvents(new MockRobotMessageBundle());
    }

    @Test
    public void testProcessII() {
        new WaveResponderServlet().processEvents(new MockRobotMessageBundle() {
            @Override
            public Wavelet getWavelet() {
                return new MockWavelet() {
                    @Override
                    public Blip appendBlip() {
                        return new MockBlip() {
                            @Override
                            public TextView getDocument() {
                                return new MockTextView() {
                                    @Override
                                    public void append(String text) {
                                        assertTrue(text.contains("first visit"));
                                    }
                                };
                            }
                        };
                    }
                };
            }
            @Override
            public boolean wasSelfAdded() {
                return true;
            }
        });
    }

    @Test
    public void testProcessIII() {
        new WaveResponderServlet().processEvents(new MockRobotMessageBundle() {
            @Override
            public List<Event> getBlipSubmittedEvents() {
                return new ArrayList<Event>();
            }
        });
    }

    @Test
    public void testProcessIv() {
        new WaveResponderServlet().processEvents(new MockRobotMessageBundle() {
            @Override
            public Wavelet getWavelet() {
                return new MockWavelet() {
                    @Override
                    public Blip appendBlip() {
                        return new MockBlip() {
                            @Override
                            public TextView getDocument() {
                                return new MockTextView() {
                                    @Override
                                    public void append(String text) {
                                        assertTrue(text.contains("cannot process"));
                                        assertTrue(text.contains("!help"));
                                    }
                                };
                            }
                        };
                    }
                };
            }
            @Override
            public List<Event> getBlipSubmittedEvents() {
                Event event = new MockEvent() {
                    @Override
                    public Blip getBlip() {
                        return new MockBlip() {
                            @Override
                            public TextView getDocument() {
                                return new MockTextView() {
                                    @Override
                                    public String getText() {
                                        return "!help";
                                    }
                                };
                            }
                        };
                    }
                };
                List<Event> events = new ArrayList<Event>();
                events.add(event);
                return events;
            }
        });
    }
}
