package com.isaji.server;

import java.util.List;

public class HttpRequest {
    private final HttpRequestLine httpRequestLine;
    private final List<String> headers;
    private final byte[] body;

    public HttpRequest(HttpRequestLine httpRequestLine, List<String> headers, byte[] body) {
        this.httpRequestLine = httpRequestLine;
        this.headers = headers;
        this.body = body;
    }

    public HttpRequestLine getHttpRequestLine() {
        return httpRequestLine;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }
}
