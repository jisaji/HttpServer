package com.isaji.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
        while (true) {
            try (
                    ServerSocket serverSocket = new ServerSocket(portNumber);
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {
                System.out.println("Reading Request");
                HttpRequestLine httpRequestLine = readRequest(in);
                System.out.println("Process Request");
                List<String> requestBody = processRequest(httpRequestLine);
                System.out.println("Writing Response");
                writeResponse(out, requestBody);
            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port "
                        + portNumber + " or listening for a connection");
                System.out.println(e.getMessage());
            }
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

    private List<String> processRequest(HttpRequestLine httpRequestLine) throws IOException {
        String uri = httpRequestLine.getRequestURI();
        if (uri.endsWith("/")) {
            uri += "index.html";
        }

        List<String> requestBody;
        try {
            requestBody = Files.readAllLines(Paths.get(documentRoot + uri));
        } catch (NoSuchFileException e) {
            //TODO: Throw a 400
            System.out.println(e);
            throw e;
        } catch (IOException e) {
            //TODO: Throw a 500
            System.out.println(e);
            throw e;
        }
        return requestBody;
    }

    public static void writeResponse(PrintWriter out, List<String> requestBody) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html; charset=UTF-8");
        out.println("");
        for (String line : requestBody) {
            out.println(line);
        }
    }
}
