package com.google.wave.api;

import java.util.Collection;

public class MockEvent implements Event {
    public Collection<String> getAddedParticipants() { return null; }
    public Blip getBlip() { return null; }
    public String getButtonName() { return null; }
    public String getChangedTitle() { return null; }
    public Long getChangedVersion() { return null; }
    public String getCreatedBlipId() { return null; }
    public String getModifiedBy() { return null; }
    public String getRemovedBlipId() { return null; }
    public Collection<String> getRemovedParticipants() { return null; }
    public Long getTimestamp() { return null; }
    public EventType getType() { return null; }
    public Wavelet getWavelet() { return null; }
}
