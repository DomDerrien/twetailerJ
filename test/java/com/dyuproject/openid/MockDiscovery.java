package com.dyuproject.openid;

public class MockDiscovery implements Discovery {
    public OpenIdUser discover(Identifier identifier, OpenIdContext context) throws Exception { return null; }
}
