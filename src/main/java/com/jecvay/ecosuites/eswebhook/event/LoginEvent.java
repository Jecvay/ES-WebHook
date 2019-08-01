package com.jecvay.ecosuites.eswebhook.event;

import com.jecvay.ecosuites.eswebhook.ApiClient;
import com.jecvay.ecosuites.eswebhook.ESWebhook;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.profile.GameProfile;

public class LoginEvent {
    private ESWebhook plugin;
    public LoginEvent(ESWebhook esWebhook) {
        plugin = esWebhook;
    }

    @Listener
    public void onClientLogin(ClientConnectionEvent.Login e) {
        final GameProfile profile = e.getProfile();
        ApiClient.sendLoginMsg(profile);
    }

    @Listener
    public void onClientLeave(ClientConnectionEvent.Disconnect e) {
        Player player = e.getTargetEntity();
        ApiClient.sendLeaveMsg(player);
    }
}
