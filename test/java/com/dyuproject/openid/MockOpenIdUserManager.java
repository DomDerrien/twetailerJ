package com.dyuproject.openid;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Dom Derrien
 *
 */
public class MockOpenIdUserManager implements OpenIdUserManager {
    private boolean shouldThrowUnknownHostException = false;
    private boolean shouldThrowFileNotFoundException = false;
    private boolean shouldThrowIllegalArgumentException = false;
    public void makeItUnknownHostException() { shouldThrowUnknownHostException = true; }
    public void makeItFileNotFoundException() { shouldThrowFileNotFoundException = true; }
    public void makeItIllegalArgumentException() { shouldThrowIllegalArgumentException = true; }

    private boolean invalidated = false;
    public boolean isInvalidated() { return invalidated; }

    public OpenIdUser getUser(HttpServletRequest request) throws IOException {
        if (shouldThrowUnknownHostException) {
            throw new UnknownHostException("Done in purpose");
        }
        if (shouldThrowFileNotFoundException) {
            throw new FileNotFoundException("DoneInPurpose");
        }
        if (shouldThrowIllegalArgumentException) {
            throw new IllegalArgumentException("DoneInPurpose");
        }
        return null;
    }
    public void init(Properties properties) { }
    public boolean invalidate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        invalidated = true;
        return false;
    }
    public boolean saveUser(OpenIdUser user, HttpServletRequest request, HttpServletResponse response) throws IOException { return false; }
}
