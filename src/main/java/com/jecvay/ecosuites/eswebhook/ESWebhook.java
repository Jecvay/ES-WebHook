package com.jecvay.ecosuites.eswebhook;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.util.Optional;

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

    private ApiServer apiServer = null;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        ApiClient.logger = logger;
        SpongeExecutorService minecraftExecutor = Sponge.getScheduler().createSyncExecutor(this);
        apiServer = ApiServer.getServer(minecraftExecutor);
        logger.info("-------Start-------->" + ApiClient.sendServerStartMsg());
    }

    @Listener
    public void onServerReload(GameReloadEvent event) {
        // ApiClient.sendServerReloadMsg();
        SpongeExecutorService minecraftExecutor = Sponge.getScheduler().createSyncExecutor(this);
        apiServer = ApiServer.getServer(minecraftExecutor);
        logger.info("-------Reload-------->" + ApiClient.sendServerReloadMsg() + apiServer.toString());
    }

    @Listener
    public void onChat(MessageChannelEvent.Chat e, @First Player p) {
        ApiClient.sendChat(p.getName(), e.getMessage().toPlain());
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
                }
            }
            ApiClient.sendDeath(playerName, killerName);
        }
    }
}
