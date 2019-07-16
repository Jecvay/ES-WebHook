package com.jecvay.ecosuites.eswebhook;

import org.apache.commons.lang3.ObjectUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ApiServer {

    final static int port = 51015;
    static private ApiServer server = null;

    private ApiServer() {
        Thread serverThread = new ListenerThread(port);
        serverThread.setDaemon(true);
        serverThread.start();
    }

    static public ApiServer getServer() {
        if (server == null) {
            server = new ApiServer();
        } else {
            // server = new ApiServer();
        }
        return server;
    }

}

class ListenerThread extends Thread {

    private int port;

    public ListenerThread(int port) {
        this.port = port;
    }

    public void run() {
        try {
            System.out.println("listener server start");
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket client = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter out = new PrintWriter(client.getOutputStream());

                out.print("HTTP/1.1 200 \r\n"); // Version & status code
                out.print("Content-Type: application/json; utf-8\r\n"); // The type of data
                out.print("Connection: close\r\n"); // Will close stream
                out.print("\r\n"); // End of headers

                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    if (line.length() == 0) {
                        break;
                    }
                    sb.append(line);
                    sb.append("\r\n");
                    out.print(line + "\r\n");
                }

                System.out.println("[Receive]>>> " + sb.toString() + "\n\n");
                out.close();
                in.close();
                client.close();
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
}
