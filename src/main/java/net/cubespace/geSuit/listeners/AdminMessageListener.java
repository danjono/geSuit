package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 7/08/2017.
 */
public class AdminMessageListener implements Listener {

    @EventHandler
    public void receivePluginMessage(PluginMessageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!(event.getSender() instanceof Server))
            return;
        if (!event.getTag().equalsIgnoreCase(geSuit.CHANNEL_NAMES.ADMIN_CHANNEL.toString())) {
            return;
        }
        if (geSuit.getInstance().isDebugEnabled()) {
            Utilities.dumpPacket(event.getTag(), "RECV", event.getData(), true);
        }
        event.setCancelled(true);
        //todo any message processing here
    }
}