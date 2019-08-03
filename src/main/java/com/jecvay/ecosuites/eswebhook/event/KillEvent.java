package com.jecvay.ecosuites.eswebhook.event;

import com.alibaba.fastjson.JSONObject;
import com.jecvay.ecosuites.eswebhook.ApiClient;
import com.jecvay.ecosuites.eswebhook.ESWebhook;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.arrow.Arrow;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;

import java.util.Optional;

public class KillEvent {
    private ESWebhook plugin;
    public KillEvent(ESWebhook esWebhook) {
        plugin = esWebhook;
    }

    @Listener
    public void onKillEntity(DestructEntityEvent.Death event) {

        event.getCause().first(Entity.class).ifPresent(target -> {
            event.getCause().last(Entity.class).ifPresent(killer -> {
                boolean isPresent = false;

                if (killer instanceof Player || target instanceof Player) {

                    // 杀人养的
                    isPresent = target.getCreator().isPresent();

                    // 抛射物 (例如弓箭) 发射者
                    if (killer instanceof Projectile) {
                        Projectile arrow = (Projectile) killer;
                        killer = (Entity) arrow.getShooter();
                    }

                    // 通用事件发送: kill
                    JSONObject json = new JSONObject();
                    json.put("event", "kill");
                    json.put("is_present", isPresent);

                    if (killer instanceof Player) {
                        Player player = (Player) killer;
                        json.put("killer", "player:" + player.getName());
                    } else {
                        json.put("killer", killer.getType().getId());
                    }

                    if (target instanceof Player) {
                        Player player = (Player) target;
                        json.put("target", "player:" + player.getName());
                    } else {
                        json.put("target", target.getType().getId());
                    }
                    ApiClient.sendCommonEvent(json);
                }
            });
        });

    }
}
