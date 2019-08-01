package com.jecvay.ecosuites.eswebhook.event;

import com.alibaba.fastjson.JSONObject;
import com.jecvay.ecosuites.eswebhook.ApiClient;
import com.jecvay.ecosuites.eswebhook.ESWebhook;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MiningEvent {
    private ESWebhook plugin;
    public MiningEvent(ESWebhook esWebhook) {
        plugin = esWebhook;
    }

    @Listener
    public void onBreakBlock(ChangeBlockEvent.Break event, @Root Player player) {

        if (player.gameMode().get() == GameModes.CREATIVE) {
            return;
        }

        List<String> blockList = new ArrayList<>();

        // 石头列表计算
        event.getTransactions().forEach(trans->{
            String blockId = trans.getOriginal().getState().getId();
            Optional<UUID> creator = trans.getOriginal().getCreator();

            if (!trans.isValid()) {
                return;
            }

            // 自己放下的矿不算
            if (creator.isPresent()) {
                return;
            }

            blockList.add(blockId);
        });

        // 通用事件发送: kill
        JSONObject json = new JSONObject();
        json.put("event", "break_block");
        json.put("uuid", player.getUniqueId());
        json.put("player", player.getName());
        json.put("block_list", blockList);
        ApiClient.sendCommonEvent(json);
    }
}
