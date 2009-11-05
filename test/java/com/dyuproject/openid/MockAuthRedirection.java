package com.dyuproject.openid;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dyuproject.util.http.UrlEncodedParameterMap;

public class MockAuthRedirection implements AuthRedirection {
    public void redirect(UrlEncodedParameterMap params, HttpServletRequest request, HttpServletResponse response) throws IOException { }
}
