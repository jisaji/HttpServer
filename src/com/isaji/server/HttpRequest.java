package com.isaji.server;

import java.util.List;
import java.util.Map;

public class HttpRequest {
    private final HttpRequestLine httpRequestLine;
    private final Map<String, String> headers;
    private final String body;

    public HttpRequest(HttpRequestLine httpRequestLine, Map<String, String> headers, String body) {
        this.httpRequestLine = httpRequestLine;
        this.headers = headers;
        this.body = body;
    }

    public HttpRequestLine getHttpRequestLine() {
        return httpRequestLine;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
