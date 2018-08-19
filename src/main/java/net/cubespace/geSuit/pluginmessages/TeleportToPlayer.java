package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.tasks.SendPluginMessage;
import net.md_5.bungee.api.ProxyServer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class TeleportToPlayer {

    public static void execute(GSPlayer player, GSPlayer target) {
        if (!player.getServer().equals(target.getServer())) {
            player.getProxiedPlayer().connect(ProxyServer.getInstance().getServerInfo(target.getServer()));
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);

        try {
            out.writeUTF("TeleportToPlayer");
            out.writeUTF(player.getName());
            out.writeUTF(target.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        geSuit.proxy.getScheduler().runAsync(geSuit.getInstance(), new SendPluginMessage(geSuit
                .CHANNEL_NAMES.TELEPORT_CHANNEL.toString(), ProxyServer.getInstance().getServerInfo(target.getServer()), bytes));
    }
}
