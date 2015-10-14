package com.isaji.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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
                processRequest(httpRequestLine);
                System.out.println("Writing Response");
                writeResponse(out);
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


    private void processRequest(HttpRequestLine httpRequestLine) {
        //TODO: Get the file requested from the URI
    }

    public static void writeResponse(PrintWriter out) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html; charset=UTF-8");
        out.println("");
        out.println("Hello World!");
    }
}
