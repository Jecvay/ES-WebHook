package com.jecvay.ecosuites.eswebhook.manager;

import com.jecvay.ecosuites.eswebhook.ApiClient;
import com.jecvay.ecosuites.eswebhook.ESWebhook;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;

import java.math.BigDecimal;
import java.util.Optional;

public class EconomyManager {

    private ESWebhook plugin;
    private EventContext evenContext;
    private EconomyService economyService;
    private Currency defaultCurrency;

    public EconomyManager(ESWebhook plugin) {
        this.plugin = plugin;
        evenContext = EventContext.builder().add(EventContextKeys.PLUGIN, plugin.getContainer()).build();
        reloadEconomyProvider();
    }

    public void setEconomyService(EconomyService economyService) {
        this.economyService = economyService;
        defaultCurrency = economyService.getDefaultCurrency();
    }

    public void reloadEconomyProvider() {
        Optional<EconomyService> serviceOptional = Sponge.getServiceManager().provide(EconomyService.class);
        if (serviceOptional.isPresent()) {
            setEconomyService(serviceOptional.get());
        } else {
            ApiClient.sendCommonException("No economy plugin");
        }
    }

    public Double getBalance(Player player) {
        Optional<UniqueAccount> uOpt = economyService.getOrCreateAccount(player.getUniqueId());
        if (uOpt.isPresent()) {
            UniqueAccount acc = uOpt.get();
            BigDecimal balance = acc.getBalance(economyService.getDefaultCurrency());
            return balance.doubleValue();
        }
        return -1D;
    }

    public ResultType easyAddMoney(Player player, Double money) {
        if (money >= 0) {
            return deposit(player, money);
        } else if (money < 0) {
            return withdraw(player, -money);
        }
        return ResultType.FAILED;
    }

    public ResultType withdraw(Player player, Double money) {
        Optional<UniqueAccount> uOpt = economyService.getOrCreateAccount(player.getUniqueId());
        if (uOpt.isPresent()) {
            TransactionResult result = uOpt.get().withdraw(
                    defaultCurrency, getBigDecimal(money),
                    Cause.of(evenContext, plugin.getContainer())
            );
            return result.getResult();
        }
        return ResultType.FAILED;
    }

    public ResultType deposit(Player player, Double money) {
        Optional<UniqueAccount> uOpt = economyService.getOrCreateAccount(player.getUniqueId());
        if (uOpt.isPresent()) {
            TransactionResult result = uOpt.get().deposit(
                    defaultCurrency, getBigDecimal(money),
                    Cause.of(evenContext, plugin.getContainer())
            );
            return result.getResult();
        }
        return ResultType.FAILED;
    }

    private BigDecimal getBigDecimal(double value) {
        return BigDecimal.valueOf((long)(value * 100), 2);
    }
}
