package com.jecvay.ecosuites.eswebhook;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;


public class ApiServer {
    private static final String HOSTNAME = "0.0.0.0";
    private static final int PORT = 51015;
    private static final int BACKLOG = 1;

    private static final int NO_RESPONSE_LENGTH = -1;

    private SpongeExecutorService minecraftExecutor = null;


    static private ApiServer server = null;

    private ApiServer(SpongeExecutorService minecraftExecutor) {
        this.minecraftExecutor = minecraftExecutor;
        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(HOSTNAME, PORT), BACKLOG);
            server.createContext("/", he -> {
                try {
                    final Headers headers = he.getResponseHeaders();
                    final String requestMethod = he.getRequestMethod().toUpperCase();
                    switch (requestMethod) {
                        case "POST":
                            BufferedReader in = new BufferedReader(new InputStreamReader(he.getRequestBody()));
                            String responseBody = in.lines().collect(Collectors.joining());
                            onReceive(responseBody);
                            headers.set("Content-Type", "application/json; charset=UTF-8");
                            final byte[] rawResponseBody = responseBody.getBytes(StandardCharsets.UTF_8);
                            he.sendResponseHeaders(200, rawResponseBody.length);
                            he.getResponseBody().write(rawResponseBody);
                            break;
                        default:
                            headers.set("Allow", "GET,POST");
                            he.sendResponseHeaders(405, -1);
                            break;
                    }
                } finally {
                    he.close();
                }
            });
            System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            server.start();
            System.out.println("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <K, V> Map<K, V> parseToMap(String json,
                                              Class<K> keyType,
                                              Class<V> valueType) {
        return JSON.parseObject(json,
                new TypeReference<Map<K, V>>(keyType, valueType) {
                });
    }

    private void sendToConsole(String text) {
        minecraftExecutor.submit(() -> {
            try {
                Sponge.getServer().setBroadcastChannel(MessageChannel.TO_CONSOLE);
                Sponge.getServer().getBroadcastChannel().send(Text.of(text));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void sendToAll(String text) {
        minecraftExecutor.submit(() -> {
            try {
                Sponge.getServer().setBroadcastChannel(MessageChannel.TO_ALL);
                Sponge.getServer().getBroadcastChannel().send(Text.of(text));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void onReceive(String receiveText) {

        try {
            // List<Map<String, Object>> justMap = JSON.parseObject(receiveText, new TypeReference<List<Map<String, Object>>>(){});

            Map<String, Object> data = parseToMap(receiveText, String.class, Object.class);
            String action = data.getOrDefault("action", "unknown").toString();
            if (action.equals("chat")) {
                String content = data.getOrDefault("content", "").toString();
                String name = data.getOrDefault("name", "").toString();
                if (content.length() > 0 && name.length() > 0) {
                    sendToAll(String.format("[qq: %s] %s", name, content));
                } else {
                    sendToConsole(String.format("[Unknown Coolq Chat] %s", receiveText));
                }
            } else {
                sendToConsole(String.format("[Unknown Coolq Data] %s", receiveText));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public ApiServer getServer(SpongeExecutorService minecraftExecutor) {
        if (server == null) {
            server = new ApiServer(minecraftExecutor);
        } else {
            // server = new ApiServer();
        }
        return server;
    }
}

/*
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
*/