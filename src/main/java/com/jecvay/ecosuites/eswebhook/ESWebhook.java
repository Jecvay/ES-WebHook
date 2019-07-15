package com.jecvay.ecosuites.eswebhook;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(
        id = "es-webhook",
        name = "ES-Webhook",
        description = "Amazing Tools for Minecraft Server!",
        authors = {
                "Jecvay"
        }
)
public class ESWebhook {

    @Inject
    private Logger logger;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        NetUtils.logger = logger;
        logger.info("-------Start-------->" + NetUtils.sendServerStartMsg());
    }

    @Listener
    public void onServerReload(GameReloadEvent event) {
        // NetUtils.sendServerReloadMsg();
        logger.info("-------Reload-------->" + NetUtils.sendServerReloadMsg());
    }

    @Listener
    public void onChat(MessageChannelEvent.Chat e, @First Player p) {
        NetUtils.sendChat(p.getName(), e.getMessage().toPlain());
    }
}
