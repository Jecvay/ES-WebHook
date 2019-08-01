package com.jecvay.ecosuites.eswebhook;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.util.Tuple;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

class PostThread extends Thread {
    private String targetURL;
    private String urlParameters;

    public PostThread(String targetURL, String urlParameters) {
        this.targetURL = targetURL;
        this.urlParameters = urlParameters;
    }

    public void run() {
        HttpURLConnection connection = null;

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/json; utf-8");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes("UTF-8").length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.write(urlParameters.getBytes("UTF-8"));
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            // return response.toString();
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().contains("refused")) {
                // do nothing
            } else {
                e.printStackTrace();
            }
            // return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}

class FakeQueue extends Thread {
    private Thread running = null;
    static private FakeQueue fakeQueue = null;
    private Queue<Tuple<String, String>> postQueue = null;

    private FakeQueue() { }

    static public FakeQueue getInstance() {
        if (fakeQueue == null) {
            fakeQueue = new FakeQueue();
            fakeQueue.running = null;
            fakeQueue.postQueue = new ConcurrentLinkedQueue<>();
            fakeQueue.start();
        }
        return fakeQueue;
    }

    void newPost(String url, String para) {
        postQueue.add(new Tuple<>(url ,para));
    }

    public void run() {

        // 轮询 任务 & 队列
        while (true) {
            try {
                if (running != null && running.isAlive()) {
                    sleep(50);
                    continue;
                }
                Tuple data = postQueue.poll();
                if (data == null) {
                    // 顺便把 cmdResultCache 发出去

                    ApiClient.sendCmdResultCache();
                    sleep(500);
                    continue;
                }

                running = new PostThread(data.getFirst().toString(), data.getSecond().toString());
                running.setDaemon(true);
                running.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

public class ApiClient {
    // 这里的方法, 都有可能是主线程调过来, 不要阻塞

    final static String CQURL = "http://jecvay.com:30004";

    static Logger logger;

    private static JSONObject cmdResultCache = null;

    // 底层不要直接调
    private static String executePost(String targetURL, String urlParameters) {
        /*
        Thread postThread = new PostThread(targetURL, urlParameters);
        postThread.setDaemon(true);
        postThread.start();
        */
        FakeQueue.getInstance().newPost(targetURL, urlParameters);
        return "[executePost] Data Sent!";
    }

    // 上面那个轮询线程会把 cmdCache 发出去
    static void sendCmdResultCache() {
        if (cmdResultCache == null) {
            return;
        }
        sendMessage(cmdResultCache, false);
        cmdResultCache = null;
    }

    // 添加数据到 cmdResultCache 缓存, 避免多条发送
    private static String addCmdResultCache(JSONObject jsonData) {
        if (cmdResultCache == null) {
            cmdResultCache = jsonData;
        } else {
            String cacheContent = cmdResultCache.getString("content");
            String newContent = jsonData.getString("content");
            cmdResultCache.put("content", cacheContent + "\n" + newContent);
        }
        return "[addCmdResultCache] Data Saved!";
    }

    private static String sendMessage(JSONObject jsonData, boolean useCache) {
        JSONObject json = new JSONObject();
        json.put("message_type", "sponge");
        json.put("post_type", "message");
        json.put("data", jsonData);
        String jsonString = json.toJSONString();
        if (useCache && jsonData.getString("action").equals("cmd_result")) {
            return addCmdResultCache(jsonData);
        } else {
            return executePost(CQURL, jsonString);
        }
    }

    // 用这个, 最基础的给 python server 发消息的
    private static String sendMessage(JSONObject jsonData) {
        return sendMessage(jsonData, true);
    }

    // 启动服务器
    static String sendServerStartMsg() {
        JSONObject json = new JSONObject();
        json.put("action", "server_start");
        json.put("time", timeNow());
        return sendMessage(json);
    }

    // 启动服务器
    static String sendServerReloadMsg() {
        JSONObject json = new JSONObject();
        json.put("action", "server_reload");
        json.put("time", timeNow());
        return sendMessage(json);
    }

    // 发送服务器状态信息
    static String sendServerStatus(JSONObject jsonData) {
        JSONObject json = new JSONObject();
        json.put("action", "status");
        json.put("time", timeNow());
        json.put("status", jsonData);
        return sendMessage(json);
    }

    // 有人说话
    static String sendChat(String playerName, String message) {
        JSONObject json = new JSONObject();
        json.put("action", "chat");
        json.put("time", timeNow());
        json.put("player", playerName);
        json.put("content", message);
        return sendMessage(json);
    }

    // 有人死了
    static public String sendDeath(String playerName, String killerName) {
        JSONObject json = new JSONObject();
        json.put("action", "death");
        json.put("time", timeNow());
        json.put("player", playerName);
        if (killerName.length() > 0) {
            json.put("killer", killerName);
        }
        return sendMessage(json);
    }

    // 执行cmd命令返回结果
    static String sendCmdResult(JSONObject cqSource, String message) {
        JSONObject json = new JSONObject();
        json.put("action", "cmd_result");
        json.put("time", timeNow());
        json.put("content", message);
        json.put("source", cqSource);
        return sendMessage(json);
    }

    // 登录
    static public String sendLoginMsg(GameProfile profile) {
        UUID uuid = profile.getUniqueId();
        String playerName = profile.getName().get();

        JSONObject json = new JSONObject();
        json.put("action", "login_game");
        json.put("time", timeNow());
        json.put("uuid", uuid);
        json.put("player", playerName);
        return sendMessage(json);
    }

    // 离线
    static public String sendLeaveMsg(Player player) {
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();

        JSONObject json = new JSONObject();
        json.put("action", "leave_game");
        json.put("time", timeNow());
        json.put("uuid", uuid);
        json.put("player", playerName);
        return sendMessage(json);
    }

    // Economy Result
    static public String sendEconomyResult(JSONObject cqSource, String playerName, String result, Double balance, String context) {
        JSONObject json = new JSONObject();
        json.put("action", "economy_result");
        json.put("time", timeNow());
        json.put("source", cqSource);
        json.put("player", playerName);
        json.put("result", result);
        json.put("context", context);
        json.put("balance", balance);
        return sendMessage(json);
    }

    // ApiServer Exception
    static public String sendCommonException(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));

        JSONObject json = new JSONObject();
        json.put("action", "apiserver_exception");
        json.put("time", timeNow());
        json.put("exception", errors.toString());
        return sendMessage(json);
    }

    static public String sendCommonException(String msg) {
        JSONObject json = new JSONObject();
        json.put("action", "apiserver_exception");
        json.put("time", timeNow());
        json.put("exception", msg);
        return sendMessage(json);
    }

    static public String sendCommonEvent(JSONObject commonData) {
        JSONObject json = new JSONObject();
        json.put("action", "common_event");
        json.put("time", timeNow());
        json.put("data", commonData);
        return sendMessage(json);
    }

    private static long timeNow() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return timestamp.getTime();
    }
}
