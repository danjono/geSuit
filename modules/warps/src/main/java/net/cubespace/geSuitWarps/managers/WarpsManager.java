package net.cubespace.geSuitWarps.managers;

import net.cubespace.geSuit.BukkitModule;
import net.cubespace.geSuit.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class WarpsManager extends DataManager {

    public WarpsManager(BukkitModule instance) {
        super(instance);
    }

    public void warpPlayer(final CommandSender sender, final String senderName, final String warp) {
    	Player p = Bukkit.getPlayer(sender.getName());
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("WarpPlayer");
            out.writeUTF(sender.getName());
            out.writeUTF(senderName);
            out.writeUTF(warp);
            out.writeBoolean(sender.hasPermission("gesuit.warps.warp." + warp.toLowerCase()) || sender.hasPermission("gesuit.warps.warp.*"));
            out.writeBoolean(sender.hasPermission("gesuit.warps.bypass"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void setWarp(CommandSender sender, String name, boolean hidden, boolean global) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        Location l = ( ( Player ) sender ).getLocation();
        try {
            out.writeUTF( "SetWarp" );
            out.writeUTF( sender.getName() );
            out.writeUTF( name );
            out.writeUTF( l.getWorld().getName() );
            out.writeDouble( l.getX() );
            out.writeDouble( l.getY() );
            out.writeDouble( l.getZ() );
            out.writeFloat( l.getYaw() );
            out.writeFloat( l.getPitch() );
            out.writeBoolean( hidden );
            out.writeBoolean( global );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void setWarpDesc(CommandSender sender, String warpName, String description) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        Location l = ( ( Player ) sender ).getLocation();
        try {
            out.writeUTF( "SetWarpDesc" );
            out.writeUTF( sender.getName() );
            out.writeUTF( warpName );
            out.writeUTF( description );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void silentWarpPlayer(final CommandSender sender, final String senderName, final String warp) {
        Player p = Bukkit.getPlayer(sender.getName());
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("SilentWarpPlayer");
            out.writeUTF(sender.getName());
            out.writeUTF(senderName);
            out.writeUTF(warp);
            out.writeBoolean(sender.hasPermission("gesuit.warps.warp." + warp.toLowerCase()) || sender.hasPermission("gesuit.warps.warp.*"));
            out.writeBoolean(sender.hasPermission("gesuit.warps.bypass"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void deleteWarp(CommandSender sender, String warp) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "DeleteWarp" );
            out.writeUTF( sender.getName() );
            out.writeUTF( warp );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }


    public void listWarps(CommandSender sender) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "GetWarpsList" );
            out.writeUTF( sender.getName() );
            out.writeBoolean( sender.hasPermission( "gesuit.warps.list.server" ) );
            out.writeBoolean( sender.hasPermission( "gesuit.warps.list.global" ) );
            out.writeBoolean( sender.hasPermission( "gesuit.warps.list.hidden" ) );
            out.writeBoolean( sender.hasPermission( "gesuit.warps.bypass" ) );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }
}