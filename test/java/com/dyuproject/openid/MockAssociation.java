package com.dyuproject.openid;

import java.util.Map;

public class MockAssociation implements Association {
    private boolean verification = false;
    public void makeItSuccessful() { verification = true; }

    public boolean associate(OpenIdUser user, OpenIdContext context) throws Exception { return verification; }
    public boolean verifyAuth(OpenIdUser user, Map<String, String> authRedirect, OpenIdContext context) throws Exception { return verification; }
}
