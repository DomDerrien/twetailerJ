package com.dyuproject.util.http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class MockHttpConnector implements HttpConnector {
    public Response doDELETE(String url, Map<?, ?> headers) throws IOException { return null; }
    public Response doDELETE(String url, Iterable<Parameter> headers) throws IOException { return null; }
    public Response doDELETE(String url, Map<?, ?> headers, Map<?, ?> parameters) throws IOException { return null; }
    public Response doDELETE(String url, Iterable<Parameter> headers, Map<?, ?> parameters) throws IOException { return null; }
    public Response doDELETE(String url, Iterable<Parameter> headers, Iterable<Parameter> parameters) throws IOException { return null; }
    public Response doGET(String url, Map<?, ?> headers) throws IOException { return null; }
    public Response doGET(String url, Iterable<Parameter> headers) throws IOException { return null; }
    public Response doGET(String url, Map<?, ?> headers, Map<?, ?> parameters) throws IOException { return null; }
    public Response doGET(String url, Iterable<Parameter> headers, Map<?, ?> parameters) throws IOException { return null; }
    public Response doGET(String url, Iterable<Parameter> headers, Iterable<Parameter> parameters) throws IOException { return null; }
    public Response doHEAD(String url, Map<?, ?> headers) throws IOException { return null; }
    public Response doHEAD(String url, Iterable<Parameter> headers) throws IOException { return null; }
    public Response doPOST(String url, Map<?, ?> headers, Map<?, ?> parameters, String charset) throws IOException { return null; }
    public Response doPOST(String url, Map<?, ?> headers, Iterable<Parameter> parameters, String charset) throws IOException { return null; }
    public Response doPOST(String url, Iterable<Parameter> headers, Map<?, ?> parameters, String charset) throws IOException { return null; }
    public Response doPOST(String url, Iterable<Parameter> headers, Iterable<Parameter> parameters, String charset) throws IOException { return null; }
    public Response doPOST(String url, Map<?, ?> headers, String contentType, byte[] data) throws IOException { return null; }
    public Response doPOST(String url, Iterable<Parameter> headers, String contentType, byte[] data) throws IOException { return null; }
    public Response doPOST(String url, Map<?, ?> headers, String contentType, InputStreamReader reader) throws IOException { return null; }
    public Response doPOST(String url, Iterable<Parameter> headers, String contentType, InputStreamReader reader) throws IOException { return null; }
    public Response doPUT(String url, Map<?, ?> headers, Map<?, ?> parameters, String charset) throws IOException { return null; }
    public Response doPUT(String url, Map<?, ?> headers, Iterable<Parameter> parameters, String charset) throws IOException { return null; }
    public Response doPUT(String url, Iterable<Parameter> headers, Map<?, ?> parameters, String charset) throws IOException { return null; }
    public Response doPUT(String url, Iterable<Parameter> headers, Iterable<Parameter> parameters, String charset) throws IOException { return null; }
    public Response doPUT(String url, Map<?, ?> headers, String contentType, byte[] data) throws IOException { return null; }
    public Response doPUT(String url, Iterable<Parameter> headers, String contentType, byte[] data) throws IOException { return null; }
    public Response doPUT(String url, Map<?, ?> headers, String contentType, InputStreamReader reader) throws IOException { return null; }
    public Response doPUT(String url, Iterable<Parameter> headers, String contentType, InputStreamReader reader) throws IOException { return null; }

}
