package com.google.wave.api;

import java.util.List;
import java.util.Map;

public class MockWavelet implements Wavelet {
    public void addParticipant(String participant) { }
    public Blip appendBlip() { return null; }
    public Blip appendBlip(String writeBackDataDocument) { return null; }
    public void appendDataDocument(String name, String data) { }
    public Wavelet createWavelet(List<String> participants, String dataDocumentWriteBack) { return null; }
    public long getCreationTime() { return 0; }
    public String getCreator() { return null; }
    public String getDataDocument(String name) { return null; }
    public Map<String, String> getDataDocuments() { return null; }
    public long getLastModifiedTime() { return 0; }
    public List<String> getParticipants() { return null; }
    public Blip getRootBlip() { return null; }
    public String getRootBlipId() { return null; }
    public String getTitle() { return null; }
    public long getVersion() { return 0; }
    public String getWaveId() { return null; }
    public String getWaveletId() { return null; }
    public boolean hasDataDocument(String name) { return false; }
    public void removeParticipant(String participant) { }
    public void setDataDocument(String name, String data) { }
    public void setTitle(String title) { }
    public void setTitle(StyledText styledText) { }
}
