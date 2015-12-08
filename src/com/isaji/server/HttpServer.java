package com.isaji.server;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpServer {
    private final int portNumber;
    private final String documentRoot;
    private final String cgibinDirectory;

    public HttpServer(int portNumber, String documentRoot, String cgibinDirectory) {
        this.portNumber = portNumber;
        this.documentRoot = documentRoot;
        this.cgibinDirectory = cgibinDirectory;
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (true) {
                try (
                        Socket clientSocket = serverSocket.accept();
                        OutputStream out = clientSocket.getOutputStream();
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
                ) {
                    System.out.println("Reading Request");
                    HttpRequest httpRequest = readRequest(in);

                    System.out.println("Process Request");
                    HttpResponse httpResponse = processRequest(httpRequest);

                    System.out.println("Writing Response");
                    writeResponse(out, httpResponse);
                } catch (IOException e) {
                    System.out.println("IOException caught when listening for a connection");
                    System.out.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("IOException caught when trying to listen on port "
                    + portNumber);
            System.out.println(e.getMessage());
        }

    }

    public HttpRequest readRequest(BufferedReader in) throws IOException {
        //Read Request-Line
        String inputLine = in.readLine();
        HttpRequestLine httpRequestLine = new HttpRequestLine(inputLine);

        List<String> headers = new ArrayList<>();
        //Read Headers
        while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
            if (inputLine.equals("")) {
                break;
            }
            headers.add(inputLine);
        }

        Map<String, String> headerMap = buildHeaderMap(headers);
        String bodyString = null;
        if (headerMap.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headerMap.get("Content-Length"));
            char[] body = new char[contentLength];
            for (int i = 0; i < contentLength; i++) {
                body[i] = (char) in.read();
            }
            bodyString = new String(body);
            System.out.println(bodyString);
        }
        return new HttpRequest(httpRequestLine, headerMap, bodyString);
    }

    private HttpResponse processRequest(HttpRequest httpRequest) {
        String uri = httpRequest.getHttpRequestLine().getRequestURI();

        String queryString = null;
        if (uri.indexOf('?') >= 0) {
            queryString = uri.substring(uri.indexOf('?') + 1);
            System.out.println(queryString);
            uri = uri.substring(0, uri.indexOf('?'));
        }

        if (uri.endsWith("/")) {
            uri += "index.html";
        }

        Path fullPath = Paths.get(documentRoot + uri);
        if (!Files.exists(fullPath)) {
            return new HttpResponse(404);
        }

        byte[] requestBody = null;
        if (uri.startsWith(cgibinDirectory)) {
            try {
                if (httpRequest.getHttpRequestLine().getMethod().toUpperCase().equals("GET")) {
                    Process process = null;
                    if (queryString == null) {
                        process = new ProcessBuilder(fullPath.toString()).start();
                    } else {
                        ProcessBuilder processBuilder = new ProcessBuilder(fullPath.toString());
                        Map<String, String> env = processBuilder.environment();
                        env.put("QUERY_STRING", queryString);
                        env.put("REQUEST_METHOD", "GET");
                        process = processBuilder.start();
                    }
                    process.waitFor();
                    requestBody = IOUtils.toByteArray(process.getInputStream());
                } else if (httpRequest.getHttpRequestLine().getMethod().toUpperCase().equals("POST")) {
                    Process process = null;
                    ProcessBuilder processBuilder = new ProcessBuilder(fullPath.toString());
                    Map<String, String> env = processBuilder.environment();
                    env.put("REQUEST_METHOD", "POST");
                    env.put("CONTENT_LENGTH", httpRequest.getHeaders().get("Content-Length"));
                    env.put("CONTENT_TYPE", httpRequest.getHeaders().get("Content-Type"));
                    process = processBuilder.start();
                    process.getOutputStream().write(httpRequest.getBody().getBytes());
                    process.getOutputStream().close();
                    process.waitFor();
                    requestBody = IOUtils.toByteArray(process.getInputStream());
                }
            } catch (IOException e) {
                System.out.println(e);
                return new HttpResponse(500);
            } catch (InterruptedException e) {
                System.out.println(e);
                return new HttpResponse(500);
            }
        } else {
            try {
                requestBody = Files.readAllBytes(Paths.get(documentRoot + uri));
            } catch (IOException e) {
                System.out.println(e);
                return new HttpResponse(500);
            }
        }
        HttpResponse httpResponse = new HttpResponse(200, requestBody);
        httpResponse.getHeaders().add(getContentType(uri));
        httpResponse.getHeaders().add("Content-Length: " + requestBody.length);
        return httpResponse;
    }

    public static void writeResponse(OutputStream out, HttpResponse httpResponse) throws IOException {
        out.write((httpResponse.getStatusLine() + "\r\n").getBytes());
        for (String headers : httpResponse.getHeaders()) {
            out.write((headers + "\r\n").getBytes());
        }
        out.write("\r\n".getBytes());
        out.write(httpResponse.getBody());
    }

    private String getContentType(String uri) {
        if (uri.endsWith(".jpg") || uri.endsWith(".jpeg")) {
            return "Content-Type: image/jpeg";
        } else if (uri.endsWith(".gif")) {
            return "Content-Type: image/gif";
        } else if (uri.endsWith(".png")) {
            return "Content-Type: image/png";
        } else if (uri.endsWith(".css")) {
            return "Content-Type: text/css";
        } else if (uri.endsWith(".js")) {
            return "Content-Type: application/javascript";
        }
        return "Content-Type: text/html";
    }

    private Map<String, String> buildHeaderMap(List<String> headers) {
        Map<String, String> headerMap = new HashMap<>();
        for (String header : headers) {
            System.out.println(header);
            String key = header.substring(0, header.indexOf(':'));
            String value = header.substring(header.indexOf(':') + 1);
            headerMap.put(key, value.trim());
        }
        return headerMap;
    }
}