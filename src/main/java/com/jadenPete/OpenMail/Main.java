package com.jadenPete.OpenMail;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * This class is responsible for:
 *   - Starting and stopping the plugin.
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
		
		// Run the Util class constructor, which uses an
		// instance of the main class to access non-static methods.
		new Util(this);
		
		// When the plugin command is used, call onCommand in the Commands class.
		getCommand("mail").setExecutor(new Commands(this));
		
		// Connect to the MySQL Database.
		SQLData.getConnection();
	}
	
	// Fired when the plugin is disabled.
	@Override
	public void onDisable(){
		SQLData.closeConnection();
	}
}
