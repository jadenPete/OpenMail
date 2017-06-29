package com.jadenPete.OpenMail;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/*
 * This class is responsible for:
 *   - Starting and stopping the plugin.
 *   - Parsing commands and only checking
 *     that arguments contain the right characters
 *     and are provided in the right amount.
 *   - Providing usefull functions to other classes.
 *     
 * It does not do anything on it's own.
 * It's sole purpose is to manage the plugin
 * and call the Commands class to operate.
 */

public class Main extends JavaPlugin{
	FileConfiguration config = getConfig();
	
	// Fired when the plugin is first enabled.
	@Override
	public void onEnable(){
		// If the configuration file doesn't exist, copy the default one.
		saveDefaultConfig();
		
		// Run the Commands class constructor, which uses an
		// instance of the main class to access non-static methods.
		new Commands(this);
		
		// Connect to the MySQL Database.
		SQLData.getConnection();
	}
	
	// Fired when the plugin is disabled.
	@Override
	public void onDisable(){
		SQLData.closeConnection();
	}
	
	// Parses the plugin command.
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		// Check that the command is being executed by a player,
		// and not the console, another plugin, or a command block.
		// Otherwise display an error-message specified in config.yml.
		if(sender instanceof Player){
			// Variable for the player who executed the command,
			Player player = (Player) sender;
			
			// If an argument is specified, carry on with the command.
			// Otherwise return false and show the command's usage.
			if(args.length > 0){
				// Parse the sub-command.
				switch(args[0]){
					case "page":{
						// Carry on with the sub-command if a number is specified.
						// Otherwise return false and show the command's usage.
						if(args.length == 2){
							if(!args[1].matches("[0-9]+")){
								return false;
							}
						} else {
							return false;
						}
						
						Commands.readPage(player, args);
						
						break;
					}
					
					case "read":{
						// Carry on with the sub-command if a number is specified.
						// Otherwise return false and show the command's usage.
						if(args.length == 2){
							if(!args[1].matches("[0-9]+")){
								return false;
							}
						} else {
							return false;
						}
						
						Commands.readMail(player, args);
						
						break;
					}
					
					case "delete":{
						// Carry on with the sub-command if one/two numbers are specified.
						// Otherwise return false and show the command's usage.
						if(args.length == 2){
							if(!args[1].matches("[0-9]+")){
								return false;
							}
						} else if(args.length == 3){
							if(!args[1].matches("[0-9]+") ||
							   !args[2].matches("[0-9]+")){
								return false;
							}
						} else {
							return false;
						}
						
						return Commands.deleteMail(player, args);
					}
					
					case "clear":{
						// Carry on with the sub-command if no arguments are specified.
						// Otherwise return false and show the command's usage.
						if(args.length == 1){
							Commands.clearMail(player);
						} else {
							return false;
						}
						
						break;
					}
					
					case "send":{
						/* 
						 * Carry on with the sub-command if at least two arguments are specified.
						 * Otherwise return false and show the command's usage.
						 *
						 * If two arguments are specified, check that the first argument is a single,
						 * alpha-numeric username or a list of alpha-numeric usernames separated by commas.
						 *
						 * If three arguments are specified, check that the
						 * first and second are alpha-numeric usernames.
						 */
						
						if(args.length == 3){
							if(!args[1].matches("[a-zA-Z0-9_]+(,[a-zA-Z0-9_]+)*") &&
								args[1].length() >= 3 && args[1].length() <= 16){
								return false;
							}
						} else if(args.length == 4){
							if(!args[1].matches("[a-zA-Z0-9_]+") ||
								args[1].length() < 3 || args[1].length() > 16 ||
							   !args[2].matches("[a-zA-Z0-9_]+") ||
							    args[2].length() < 3 || args[2].length() > 16){
								return false;
							}
						} else {
							return false;
						}
						
						Commands.sendMail(player, args);
						
						break;
					}
					
					default:{
						return false;
					}
				}
			} else {
				return false;
			}	
		} else {
			sender.sendMessage(config.getString("messages.non-player"));
		}
		
		return true;
	}
	
	// Sort out the invalid players from a specified list of players.
	@SuppressWarnings("deprecation")
	public static String getInvalidPlayers(String[] players){
		String invalidPlayers = "";
		
		// Loop through the specified array
		// and add the invalid players to a new one.
		for(int a = 0; a < players.length; a++){
			if(Bukkit.getPlayer(players[a]) == null &&
			  !Bukkit.getOfflinePlayer(players[a]).hasPlayedBefore()){
				if(invalidPlayers != ""){
					invalidPlayers += ", ";
				}
				
				invalidPlayers += players[a];
			}
		}
		
		// Return the new array.
		return invalidPlayers;
	}
}
