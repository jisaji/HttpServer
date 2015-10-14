package com.isaji.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Http Server on port " + args[0]);
        System.out.println("Using DocumentRoot " + args[1]);
        new HttpServer(Integer.parseInt(args[0]), args[1]).run();
    }
}
