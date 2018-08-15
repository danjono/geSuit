package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.objects.GSPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

/**
 * Command: /gsreload Permission needed: gesuit.reload or gesuit.admin Arguments: none What does it do: Reloads every config
 */
public class DebugCommand extends Command
{

    public DebugCommand()
    {
        super("gsdebug");
    }

    private boolean ppvalid = false;
    private boolean gsvalid = false;
    private String sname = "";
    @Override
    public void execute(CommandSender sender, String[] args)
    {
    	if (!(sender.hasPermission("gesuit.debug") || sender.hasPermission("gesuit.admin"))) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.NO_PERMISSION);
            return;
        }

    	if (args.length == 0) {
	
	        // Toggle debug
	        geSuit.instance.setDebugEnabled(!geSuit.instance.isDebugEnabled());
	        
	        PlayerManager.sendMessageToTarget(sender, "geSuit debug is now: " + (geSuit.instance.isDebugEnabled() ? "ENABLED" : "DISABLED"));
    	} else {
    		String action = args[0];
			switch (action) {
				case "help":
					PlayerManager.sendMessageToTarget(sender, ChatColor.GREEN + "geSuit Debug Commands:");
					PlayerManager.sendMessageToTarget(sender, ChatColor.YELLOW + "/gsdebug onlineplayers" + ChatColor.WHITE + " - Dump online player list");
					PlayerManager.sendMessageToTarget(sender, ChatColor.YELLOW + "/gsdebug cachedplayers" + ChatColor.WHITE + " - Dump cached player list");
					break;
				case "onlineplayers":
					// Useful for troubleshooting issues with the onlinePlayers map
					PlayerManager.sendMessageToTarget(sender, "List of entries in onlinePlayers:");
					for (String player : PlayerManager.onlinePlayers.keySet()) {
						GSPlayer gs = PlayerManager.onlinePlayers.get(player);
                        gsvalid = false;
                        ppvalid = false;
                        sname = "";
                        getPlayer(gs);
						PlayerManager.sendMessageToTarget(sender, "  " + ChatColor.AQUA + player +
								ChatColor.WHITE + " -> GS:" + (gsvalid ? ChatColor.GREEN + "yes" : ChatColor.RED + "no") +
								ChatColor.WHITE + " / PP:" + (ppvalid ? ChatColor.GREEN + "yes" : ChatColor.RED + "no") +
								ChatColor.WHITE + " / SRV:" + (!sname.isEmpty() ? ChatColor.GREEN + sname : ChatColor.RED + "none"));
					}
					break;
				case "cachedplayers":
					// Useful for troubleshooting issues with the onlinePlayers map
					PlayerManager.sendMessageToTarget(sender, "List of entries in cachedplayers:");
					for (UUID uuid : PlayerManager.cachedPlayers.keySet()) {
						GSPlayer gs = PlayerManager.cachedPlayers.get(uuid);
                        gsvalid = false;
                        ppvalid = false;
                        sname = "";
                        getPlayer(gs);
						PlayerManager.sendMessageToTarget(sender, "  " + ChatColor.AQUA + uuid +
								ChatColor.WHITE + " -> GS:" + (gsvalid ? ChatColor.GREEN + "yes" + ChatColor.AQUA + " (" + gs.getName() + ")" : ChatColor.RED + "no") +
								ChatColor.WHITE + " / PP:" + (ppvalid ? ChatColor.GREEN + "yes" : ChatColor.RED + "no") +
								ChatColor.WHITE + " / SRV:" + (!sname.isEmpty() ? ChatColor.GREEN + sname : ChatColor.RED + "none"));
					}
					break;
				default:
					PlayerManager.sendMessageToTarget(sender, "ERROR: Invalid debug action");
					break;
			}
		}
    }

    private void getPlayer(GSPlayer player) {
        if (player != null) {
            gsvalid = true;
            ProxiedPlayer pp = player.getProxiedPlayer();
            if (pp != null) {
                ppvalid = true;
                Server s = pp.getServer();
                if ((s != null) && (s.getInfo() != null)) {
                    sname = s.getInfo().getName();
                }
            }
        }
    }
}
