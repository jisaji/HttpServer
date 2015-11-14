package com.isaji.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;

public class HttpServer {
    private final int portNumber;
    private final String documentRoot;

    public HttpServer(int portNumber, String documentRoot) {
        this.portNumber = portNumber;
        this.documentRoot = documentRoot;
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
                    HttpRequestLine httpRequestLine = readRequest(in);

                    System.out.println("Process Request");
                    HttpResponse httpResponse = processRequest(httpRequestLine);

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

    public HttpRequestLine readRequest(BufferedReader in) throws IOException {
        //Read Request-Line
        String inputLine = in.readLine();
        HttpRequestLine httpRequestLine = new HttpRequestLine(inputLine);

        //Read Headers
        while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
            if (inputLine.equals("")) {
                break;
            }
        }

        return httpRequestLine;
    }

    private HttpResponse processRequest(HttpRequestLine httpRequestLine) {
        String uri = httpRequestLine.getRequestURI();
        if (uri.endsWith("/")) {
            uri += "index.html";
        }

        byte[] requestBody;
        try {
            requestBody = Files.readAllBytes(Paths.get(documentRoot + uri));
        } catch (NoSuchFileException e) {
            return new HttpResponse(404);
        } catch (IOException e) {
            System.out.println(e);
            return new HttpResponse(500);
        }
        HttpResponse httpResponse = new HttpResponse(200, requestBody);

        httpResponse.getHeaders().add("Content-Type: image/jpeg");

        int contentLength = requestBody.length;

        httpResponse.getHeaders().add("Content-Length: " + contentLength);
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
}