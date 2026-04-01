package com.example.data_access;

import org.h2.tools.Server;

public class H2WebLauncher {
    private static final String WEB_PORT = "8082";
    private static final String TCP_PORT = "9092";

    public static void main(String[] args) {
        try {
            // Start the H2 web server with default settings
            // -web: Start the web server with the H2 Console
            // -webAllowOthers: Allow other computers to connect
            // -webPort: Use port 8082 for web interface
            Server webServer = Server.createWebServer(
                    "-web", 
                    "-webAllowOthers", 
                    "-webPort", WEB_PORT).start();
            
            System.out.println("H2 Web Console server started.");
            System.out.println("URL: " + webServer.getURL());
            System.out.println("Press Ctrl+C to stop the server...");
            
            // Optional: If you want to also start a TCP server for other applications to connect
            Server tcpServer = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", TCP_PORT).start();
            System.out.println("H2 TCP server started on port " + TCP_PORT + ".");
            
        } catch (Exception e) {
            System.out.println("Error starting H2 server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}