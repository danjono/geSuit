package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.tasks.SendPluginMessage;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DelWorldSpawn {

    public static void execute(ServerInfo server, String world) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);

        try {
            out.writeUTF("DelWorldSpawn");
            out.writeUTF(world);
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        geSuit.proxy.getScheduler().runAsync(geSuit.getInstance(),
            new SendPluginMessage(geSuit.CHANNEL_NAMES.SPAWN_CHANNEL, server, bytes));
    }
}
