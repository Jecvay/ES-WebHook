package com.jecvay.ecosuites.eswebhook.event;

import com.jecvay.ecosuites.eswebhook.ESWebhook;
import com.jecvay.ecosuites.eswebhook.manager.EconomyManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.service.economy.EconomyService;

public class EconomyListener {
    private ESWebhook plugin;
    private EconomyManager economyManager;

    public EconomyListener(ESWebhook plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        plugin.getLogger().info("EconomyListener init");
    }

    @Listener
    public void onProviderChange(ChangeServiceProviderEvent event) {
        plugin.getLogger().info("EconomyService changed!");
        if (event.getNewProvider() instanceof EconomyService) {
            economyManager.setEconomyService((EconomyService) event.getNewProvider());
        }
    }
}