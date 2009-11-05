package com.dyuproject.openid;

import com.dyuproject.openid.Discovery.UserCache;

public class MockUserCache implements UserCache {
    public OpenIdUser get(String key, boolean clone) { return null; }
    public void put(String key, OpenIdUser user) { }
}
