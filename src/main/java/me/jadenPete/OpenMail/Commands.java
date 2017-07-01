package me.jadenPete.OpenMail;

import org.bukkit.configuration.file.FileConfiguration;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/*
 * This class is responsible for:
 *   - Parsing commands and only checking
 *     that arguments contain the right characters
 *     and are provided in the right amount.
 * 
 * It does not do anything on it's own.
 * It does not interact with the MySQL database in any way.
 * It's sole purpose is to be called upon by Main
 * and call the Util class to operate.
 */
public class Commands implements CommandExecutor {
	// Variable to access the plugin's config.yml.
	private static FileConfiguration config;
	
	// The class's constructor set's the config variable
	// to getConfig(), referenced from the Main class,
	// since we cannot directly access non-static objects.
	public Commands(Main instance){
		config = instance.getConfig();
	}
	
	// Parses the plugin command.
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
						
						Util.readPage(player, args);
						
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
						
						Util.readMail(player, args);
						
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
						
						return Util.deleteMail(player, args);
					}
					
					case "clear":{
						// Carry on with the sub-command if no arguments are specified.
						// Otherwise return false and show the command's usage.
						if(args.length == 1){
							Util.clearMail(player);
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
						
						Util.sendMail(player, args);
						
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
}
