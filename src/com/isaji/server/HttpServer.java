package com.isaji.server;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

        Path fullPath = Paths.get(documentRoot + uri);
        if (!Files.exists(fullPath)) {
            return new HttpResponse(404);
        }

        byte[] requestBody;
        if (uri.startsWith(cgibinDirectory)) {
            try {
                Process process = Runtime.getRuntime().exec(fullPath.toString());
                process.waitFor();
                requestBody = IOUtils.toByteArray(process.getInputStream());
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
        }
        return "Content-Type: text/html";
    }
}