package com.google.wave.api;

import java.util.List;

public class MockTextView implements TextView {
    public void append(String text) { }
    public void appendElement(Element element) { }
    public Blip appendInlineBlip() { return null; }
    public void appendMarkup(String content) { }
    public void appendStyledText(StyledText styledText) { }
    public void delete() { }
    public void delete(Range range) { }
    public void deleteAnnotations(String name) { }
    public void deleteAnnotations(Range range) { }
    public void deleteElement(int index) { }
    public void deleteInlineBlip(Blip blip) { }
    public boolean elementExists(int index) { return false; }
    public List<Annotation> getAnnotations() { return null; }
    public List<Annotation> getAnnotations(Range range) { return null; }
    public List<Annotation> getAnnotations(String name) { return null; }
    public List<Annotation> getAnnotations(Range range, String name) { return null; }
    public String getAuthor() { return null; }
    public Element getElement(int index) { return null; }
    public List<Element> getElements() { return null; }
    public List<Element> getElements(Range range) { return null; }
    public List<Element> getElements(ElementType type) { return null; }
    public FormView getFormView() { return null; }
    public GadgetView getGadgetView() { return null; }
    public List<Blip> getInlineBlips() { return null; }
    public int getPosition(Element element) { return 0; }
    public List<Annotation> getStyles() { return null; }
    public List<Annotation> getStyles(Range range) { return null; }
    public List<Annotation> getStyles(StyleType style) { return null; }
    public List<Annotation> getStyles(Range range, StyleType style) { return null; }
    public String getText() { return null; }
    public String getText(Range range) { return null; }
    public boolean hasAnnotation(String name) { return false; }
    public void insert(int start, String text) { }
    public void insertElement(int index, Element element) { }
    public Blip insertInlineBlip(int start) { return null; }
    public Blip insertInlineBlipAfterFormElement(FormElement formElement) { return null; }
    public void insertStyledText(int start, StyledText styledText) { }
    public void replace(String text) { }
    public void replace(Range range, String text) { }
    public void replaceElement(int index, Element element) { }
    public void replaceStyledText(StyledText styledText) { }
    public void replaceStyledText(Range range, StyledText styledText) { }
    public void setAnnotation(String name, String value) { }
    public void setAnnotation(Range range, String name, String value) { }
    public void setAuthor(String author) { }
    public void setCreationTime(long creationTime) { }
    public void setStyle(StyleType style) { }
    public void setStyle(Range range, StyleType style) { }
}
