package com.isaji.server;

import java.util.ArrayList;
import java.util.List;

public class HttpResponse {
    private final int responseCode;
    private final List<String> headers;
    private final byte[] body;

    public HttpResponse(int responseCode) {
        this(responseCode, new byte[0]);
    }

    public HttpResponse(int responseCode, byte[] body) {
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

    public byte[] getBody() {
        return body;
    }
}
