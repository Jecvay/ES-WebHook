package com.jecvay.ecosuites.eswebhook;

import com.alibaba.fastjson.JSONObject;
import javafx.beans.binding.When;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Tristate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CmdSource implements CommandSource {

    static private CommandSource consoleSource = null;
    static private CmdSource cmdSource = null;

    // 用来记录 CoolQ 是谁发过来要执行命令的
    private JSONObject cqSource = null;

    private CmdSource() {
        consoleSource = Sponge.getServer().getConsole();
    }

    static public CmdSource getInstance(JSONObject json) {
        if (cmdSource == null) {
            cmdSource = new CmdSource();
        }
        cmdSource.cqSource = json;
        return cmdSource;
    }

    @Override
    public String getName() {
        return consoleSource.getName();
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return consoleSource.getCommandSource();
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return consoleSource.getContainingCollection();
    }

    @Override
    public SubjectReference asSubjectReference() {
        return consoleSource.asSubjectReference();
    }

    @Override
    public boolean isSubjectDataPersisted() {
        return consoleSource.isSubjectDataPersisted();
    }

    @Override
    public SubjectData getSubjectData() {
        return consoleSource.getSubjectData();
    }

    @Override
    public SubjectData getTransientSubjectData() {
        return consoleSource.getTransientSubjectData();
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        return Tristate.TRUE;
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, SubjectReference parent) {
        return consoleSource.isChildOf(contexts, parent);
    }

    @Override
    public List<SubjectReference> getParents(Set<Context> contexts) {
        return consoleSource.getParents(contexts);
    }

    @Override
    public Optional<String> getOption(Set<Context> contexts, String key) {
        return consoleSource.getOption(contexts, key);
    }

    @Override
    public String getIdentifier() {
        return consoleSource.getIdentifier();
    }

    @Override
    public Set<Context> getActiveContexts() {
        return consoleSource.getActiveContexts();
    }

    @Override
    public void sendMessage(Text message) {
        String plain = message.toPlain();
        ApiClient.sendCmdResult(cqSource, plain);
    }

    @Override
    public MessageChannel getMessageChannel() {
        return consoleSource.getMessageChannel();
    }

    @Override
    public void setMessageChannel(MessageChannel channel) {
        consoleSource.setMessageChannel(channel);
    }
}
