package com.isaji.server;

import java.util.ArrayList;
import java.util.List;

public class HttpResponse {
    private final int responseCode;
    private final List<String> headers;
    private final List<String> body;

    public HttpResponse(int responseCode) {
        this(responseCode, new ArrayList<>());
    }

    public HttpResponse(int responseCode, List<String> body) {
        this.responseCode = responseCode;
        this.headers = new ArrayList<String>();
        this.body = body;
    }

    public String getStatusLine() {
        switch (responseCode) {
            case 200:
                return "HTTP/1.1 200 OK";
            case 404:
                return "HTTP/1.1 404 Not Found";
            case 500:
                return "HTTP/1.1 500 Internal Server Error";
            default:
                throw new RuntimeException("StatusLine not found for responseCode: " + responseCode);
        }
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<String> getBody() {
        return body;
    }
}
