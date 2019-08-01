package com.jecvay.ecosuites.eswebhook;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

// Singleton
public class ApiServer {
    private static final String HOSTNAME = "0.0.0.0";
    private static final int PORT = 50205;
    private static final int BACKLOG = 1;
    private static final String[] ALLOW_ECO_OP = {"add", "query"};

    private static final int NO_RESPONSE_LENGTH = -1;

    private SpongeExecutorService minecraftExecutor = null;


    static private ApiServer server = null;

    private ESWebhook plugin = null;

    private ApiServer(ESWebhook plugin, SpongeExecutorService minecraftExecutor) {
        this.plugin = plugin;
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

                            // coolq 发过来的 json 数据处理就在这里哦, responseBody 是一个文本哦
                            onReceive(responseBody);

                            headers.set("Content-Type", "application/json; charset=UTF-8");
                            final byte[] rawResponseBody = responseBody.getBytes(StandardCharsets.UTF_8);
                            he.sendResponseHeaders(200, rawResponseBody.length);
                            he.getResponseBody().write(rawResponseBody);
                            break;
                        case "GET":
                            final byte[] simpleBody = "<h1>OK</h1>".getBytes(StandardCharsets.UTF_8);
                            headers.set("Content-Type", "text/html; charset=UTF-8");
                            he.sendResponseHeaders(200, simpleBody.length);
                            he.getResponseBody().write(simpleBody);
                        default:
                            headers.set("Allow", "GET,POST");
                            he.sendResponseHeaders(405, -1);
                            break;
                    }
                } finally {
                    he.close();
                }
            });
            server.start();
        } catch (Exception e) {
            ApiClient.sendCommonException(e);
        }
    }

    static public ApiServer getServer(ESWebhook plugin, SpongeExecutorService minecraftExecutor) {
        if (server == null) {
            server = new ApiServer(plugin, minecraftExecutor);
        } else {
            // server = new ApiServer();
        }
        return server;
    }

    private static <K, V> Map<K, V> parseToMap(String json,
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
                ApiClient.sendCommonException(e);
            }
        });
    }

    private void sendToAll(String text) {
        minecraftExecutor.submit(() -> {
            try {
                Sponge.getServer().setBroadcastChannel(MessageChannel.TO_ALL);
                Sponge.getServer().getBroadcastChannel().send(Text.of(text));
            } catch (Exception e) {
                ApiClient.sendCommonException(e);
            }
        });
    }

    private void runCommand(JSONObject source, String cmd) {
        minecraftExecutor.submit(() -> {
           try {
               Sponge.getCommandManager().process(CmdSource.getInstance(source), cmd);
           } catch (Exception e) {
               ApiClient.sendCommonException(e);
           }
        });
    }

    private void sendServerStatus(JSONObject cqSource) {
        minecraftExecutor.submit(() -> {
            JSONObject json = new JSONObject();
            Collection<Player> playerList = Sponge.getServer().getOnlinePlayers();
            List<String> playerNameList = new ArrayList<>();
            playerList.forEach(player -> {
                playerNameList.add(player.getName());
            });
            json.put("players", playerNameList);
            json.put("online", playerNameList.size());
            json.put("source", cqSource);
            ApiClient.sendServerStatus(json);
        });
    }

    private void handleEconomy(JSONObject cqSource, Map<String, Object> data) {
        String playerName = data.get("player").toString();
        String operation = data.get("operation").toString();

        String context = data.get("context").toString();
        if (playerName.length() == 0) {
            return;
        } else if (!Arrays.asList(ALLOW_ECO_OP).contains(operation)) {
            return;
        }

        Player player = Sponge.getServer().getPlayer(playerName).orElse(null);
        if (player == null) {
            ApiClient.sendCommonException("handleEconomy cannot find " + playerName);
            return;
        }

        ResultType resultType = ResultType.FAILED;
        Double balance = plugin.getEconomyManager().getBalance(player);
        if (operation.equals("add")) {
            Integer amount = (Integer) data.get("amount");
            if (amount != null) {
                resultType = plugin.getEconomyManager().easyAddMoney(player, amount.doubleValue());
            }
        } else if (operation.equals("query")) {
            resultType = ResultType.SUCCESS;
        }

        ApiClient.sendEconomyResult(cqSource, playerName, resultType.name(), balance, context);
    }

    private void onReceive(String receiveText) {
        /*
        * 来源 [cqSource]: QQ私聊 / 群聊
        *  - key: 与 "action" 同级的 "source"
        *  - value: {"qq": xxxxx, "group": yyyyy}
        *  - 如果没有 "group" 就是私聊
        *  - 本插件不进行处理, 直接 echo 回去即可
        * */

        try {

            Map<String, Object> data = parseToMap(receiveText, String.class, Object.class);
            String action = data.getOrDefault("action", "unknown").toString();
            JSONObject cqSource = (JSONObject) data.get("source");
            switch (action) {
                case "chat": {        // QQ -> MC 聊天
                    String content = data.getOrDefault("content", "").toString();
                    String name = data.getOrDefault("name", "").toString();
                    if (content.length() > 0 && name.length() > 0) {
                        sendToAll(String.format("[qq: %s] %s", name, content));
                    } else {
                        sendToConsole(String.format("[Unknown Coolq Chat] %s", receiveText));
                    }
                    break;
                }
                case "cmd": {      // 使用 MC 指令
                    String content = data.getOrDefault("content", "").toString();
                    if (content.length() == 0) {
                        sendToConsole(String.format("[MC指令错误] %s", receiveText));
                        break;
                    }
                    runCommand(cqSource, content);
                    break;
                }
                case "status":        // 查询服务器信息
                    sendServerStatus(cqSource);
                    break;
                case "economy":
                    handleEconomy(cqSource, data);
                    break;
                default:
                    sendToConsole(String.format("[Unknown Coolq Data] %s", receiveText));
                    break;
            }

        } catch (Exception e) {
            ApiClient.sendCommonException(e);
        }
    }


}
