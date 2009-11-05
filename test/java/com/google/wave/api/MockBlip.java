package com.google.wave.api;

import java.util.List;

public class MockBlip implements Blip {
    public Blip createChild() { return null; }
    public void delete() { }
    public void deleteInlineBlip(Blip child) { }
    public String getBlipId() { return null; }
    public Blip getChild(int index) { return null; }
    public List<String> getChildBlipIds() { return null; }
    public List<Blip> getChildren() { return null; }
    public List<String> getContributors() { return null; }
    public String getCreator() { return null; }
    public TextView getDocument() { return null; }
    public long getLastModifiedTime() { return 0; }
    public Blip getParent() { return null; }
    public String getParentBlipId() { return null; }
    public long getVersion() { return 0; }
    public Wavelet getWavelet() { return null; }
    public boolean hasChildren() { return false; }
    public boolean isChildAvailable(int index) { return false; }
    public boolean isDocumentAvailable() { return false; }
    public boolean isParentAvailable() { return false; }
}
