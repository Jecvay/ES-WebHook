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

    private ApiServer apiServer = null;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        ApiClient.logger = logger;
        apiServer = ApiServer.getServer();
        logger.info("-------Start-------->" + ApiClient.sendServerStartMsg());
    }

    @Listener
    public void onServerReload(GameReloadEvent event) {
        // ApiClient.sendServerReloadMsg();
        apiServer = ApiServer.getServer();
        logger.info("-------Reload-------->" + ApiClient.sendServerReloadMsg() + apiServer.toString());
    }

    @Listener
    public void onChat(MessageChannelEvent.Chat e, @First Player p) {
        ApiClient.sendChat(p.getName(), e.getMessage().toPlain());
    }
}
