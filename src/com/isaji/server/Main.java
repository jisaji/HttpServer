package com.isaji.server;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Http Server on port " + args[0]);
        System.out.println("Using DocumentRoot " + args[1]);
        System.out.println("Using cgi-bin directory " + args[2]);
        new HttpServer(Integer.parseInt(args[0]), args[1], args[2]).run();
    }
}
