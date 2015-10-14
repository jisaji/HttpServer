package com.isaji.server;

public class HttpRequestLine {

    private final String method;
    private final String requestURI;
    private final String httpVersion;

    public HttpRequestLine(String requestLine) {
        String[] tokens = requestLine.split("\\s");
        method = tokens[0];
        requestURI = tokens[1];
        httpVersion = tokens[2];
    }

    public String getMethod() {
        return method;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public String toString() {
        return method + " " + requestURI + " " + httpVersion;
    }
}
