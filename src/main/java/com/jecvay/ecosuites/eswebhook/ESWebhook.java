package com.jecvay.ecosuites.eswebhook;

import com.google.inject.Inject;
import com.jecvay.ecosuites.eswebhook.event.*;
import com.jecvay.ecosuites.eswebhook.manager.EconomyManager;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;

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

    @Inject
    private Game game;

    @Inject
    private PluginContainer pluginContainer;

    private ApiServer apiServer = null;
    private EconomyManager economyManager = null;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        ApiClient.logger = logger;
        logger.info("Start " + ApiClient.sendServerStartMsg());
    }

    @Listener
    public void onServerReload(GameReloadEvent event) {
        logger.info("Reload " + ApiClient.sendServerReloadMsg());
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        /*
         * During this state, the plugin should finish any work needed in order to be functional.
         * Global event handlers should get registered in this stage.
         * */
        logger.info("onInit");

        registerCustomListeners();

        SpongeExecutorService minecraftExecutor = Sponge.getScheduler().createSyncExecutor(this);
        apiServer = ApiServer.getServer(this, minecraftExecutor);
    }


    @Listener
    public void onChat(MessageChannelEvent.Chat e, @First Player p) {
        ApiClient.sendChat(p.getName(), e.getMessage().toPlain());
    }

    private void registerCustomListeners() {
        economyManager = new EconomyManager(this);
        game.getEventManager().registerListeners(this, new EconomyListener(this, economyManager));
        // game.getEventManager().registerListeners(this, new DeathEvent(this));
        game.getEventManager().registerListeners(this, new KillEvent(this));
        game.getEventManager().registerListeners(this, new LoginEvent(this));
        game.getEventManager().registerListeners(this, new MiningEvent(this));
    }

    public PluginContainer getContainer() {
        return pluginContainer;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
}
