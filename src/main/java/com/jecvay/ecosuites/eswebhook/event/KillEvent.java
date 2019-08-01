package com.jecvay.ecosuites.eswebhook.event;

import com.alibaba.fastjson.JSONObject;
import com.jecvay.ecosuites.eswebhook.ApiClient;
import com.jecvay.ecosuites.eswebhook.ESWebhook;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;

public class KillEvent {
    private ESWebhook plugin;
    public KillEvent(ESWebhook esWebhook) {
        plugin = esWebhook;
    }

    @Listener
    public void onKillEntity(DestructEntityEvent.Death event) {
        event.getCause().first(Player.class).ifPresent(player -> {
            Entity entity = event.getTargetEntity();
            String entityId = entity.getType().getId();
            boolean isSuicide = false;

            // 创造模式不发送
            if (player.gameMode().get() == GameModes.CREATIVE) {
                return;
            }

            // 杀自己养的不发送
            if (entity.getCreator().isPresent()) {
                return;
            }

            // 自杀
            if (entity instanceof Player && entity.equals(player)) {
                isSuicide = true;
            }

            // 通用事件发送: kill
            JSONObject json = new JSONObject();
            json.put("event", "kill");
            json.put("uuid", player.getUniqueId());
            json.put("player", player.getName());
            json.put("entity_id", entityId);
            json.put("is_suicide", isSuicide);
            ApiClient.sendCommonEvent(json);
        });
    }
}
