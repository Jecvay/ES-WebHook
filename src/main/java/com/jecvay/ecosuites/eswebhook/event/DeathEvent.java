package com.jecvay.ecosuites.eswebhook.event;

import com.jecvay.ecosuites.eswebhook.ApiClient;
import com.jecvay.ecosuites.eswebhook.ESWebhook;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;

import java.util.Optional;

public class DeathEvent {
    private ESWebhook plugin;
    public DeathEvent(ESWebhook esWebhook) {
        plugin = esWebhook;
    }

    @Listener
    public void onPlayerDie(DestructEntityEvent.Death event) {
        if (event.getTargetEntity() instanceof Player) {
            Optional<EntityDamageSource> optDamageSource = event.getCause().first(EntityDamageSource.class);
            Player player = (Player) event.getTargetEntity();
            String playerName = player.getName();
            String killerName = "";
            if (optDamageSource.isPresent()) {
                EntityDamageSource damageSource = optDamageSource.get();
                Entity killer = damageSource.getSource();
                if (killer instanceof Player) {
                    killerName = ((Player) killer).getName();
                } else {
                    killerName = "#ID:" + killer.getType().getId();
                }
            }
            ApiClient.sendDeath(playerName, killerName);
        }
    }
}
