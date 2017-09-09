package me.jadenPete.OpenMail;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class Events implements Listener {
	// Send the player a notification when they join.
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Util.sendNotification(event.getPlayer());
	}
	
	// Send the player a notification when they teleport from another world.
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event){
		if(event.getFrom().getWorld().getName() != event.getTo().getWorld().getName()){
			Util.sendNotification(event.getPlayer());
		}
	}
}
