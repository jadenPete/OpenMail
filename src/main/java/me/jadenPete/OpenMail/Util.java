package me.jadenPete.OpenMail;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/*
 * This class is responsible for:
 *	- Interacting with the user.
 *	- Handling and reporting errors.
 *	- Making sure that provided data is valid, but
 *		assuming that it contains the right characters
 *		and is provided in the right amount.
 *	- Providing usefull functions to other classes.
 * 
 * It does not interact with the MySQL database in any way.
 * It's sole purpose is to be called upon by onCommand.
 */
public class Util {
	// Variable to access the plugin's config.yml.
	private static FileConfiguration config;
	
	// The class's constructor set's the config variable
	// to getConfig(), referenced from the Main class,
	// since we cannot directly access non-static objects.
	public Util(Main instance){
		config = instance.getConfig();
	}
	
	// Display a specific page of mail to the player.
	public static void readPage(Player player, String[] args){
		// Make sure that the number is a 32bit signed integer.
		int number = 0;
		
		try {
			number = Integer.parseInt(args[1]);
		} catch(Exception e){
			player.sendMessage(config.getString("messages.int-error"));
		}
		
		if(number != 0){
			double messageCount = SQLData.getNumber(player.getName()) - 1;
			double pageSize = config.getInt("options.page-size");
			
			int pageCount = (int) Math.ceil(messageCount / pageSize);
			
			// Make sure that the page exists.
			if(number <= pageCount){
				// Tell the player that the messages are being loaded.
				player.sendMessage(config.getString("messages.read"));
				
				int currentMessage = number * (int) pageSize - ((int) pageSize - 1);
				int lastMessage;
				
				if(number * pageSize > messageCount){
					lastMessage = (int) messageCount;
				} else {
					lastMessage = number * (int) pageSize;
				}
				
				// Display every message on the page.
				for(; currentMessage <= lastMessage; currentMessage++){
					// Access the message from the SQL database and
					// convert the array to a readable message.
					Object[] message = SQLData.readMail(player, currentMessage);
					
					// Make sure that the message exists.
					if((int) message[0] == 2){
						String messageString = config.getString("messages.read-message");
						
						// Check whether the sender is offline, so we can
						// convert their UUID to a username correctly.
						String senderName;
						
						if(Bukkit.getPlayer((String) message[1]) == null){
							senderName = Bukkit.getOfflinePlayer(UUID.fromString((String) message[1])).getName();
						} else {
							senderName = Bukkit.getPlayer(UUID.fromString((String) message[1])).getName();
						}
						
						messageString = messageString.replace("%u", senderName);
						
						// Take the message format defined in config.yml and
						// replace the markers with the appropriate message values.
						player.sendMessage(messageString.replace("%n", String.valueOf((int) message[2]))
														.replace("%d", (String) message[3])
														.replace("%t", (String) message[4])
														.replace("%m", (String) message[5]));
						
					// If there was an error accessing the message, tell the player.
					} else {
						player.sendMessage(config.getString("messages.read-error"));
					}
				}
			} else {
				player.sendMessage(config.getString("messages.invalid-page").replace("%p", args[1]));
			}
		}
	}
	
	// Display a specified message to the player.
	public static void readMail(Player player, String[] args){
		// Make sure that the number is a 32bit signed integer.
		int number = 0;
		
		try {
			number = Integer.parseInt(args[1]);
		} catch(Exception e){
			player.sendMessage(config.getString("messages.int-error"));
		}
		
		if(number != 0){
			// Access the message from the SQL database and
			// convert the array to a readable message.
			Object[] message = SQLData.readMail(player, number);
			
			// Make sure that the message exists
			if((int) message[0] == 2){
				String messageString = config.getString("messages.read-message");
				
				// Tell the player that the message is being loaded.
				player.sendMessage(config.getString("messages.read"));
				
				// Check whether the sender is offline, so we can
				// convert their UUID to a username correctly.
				String senderName;
				
				if(Bukkit.getPlayer((String) message[1]) == null){
					senderName = Bukkit.getOfflinePlayer(UUID.fromString((String) message[1])).getName();
				} else {
					senderName = Bukkit.getPlayer(UUID.fromString((String) message[1])).getName();
				}
				
				messageString = messageString.replace("%u", senderName);
				
				// Take the message format defined in config.yml and
				// replace the markers with the appropriate message values.
				player.sendMessage(messageString.replace("%n", String.valueOf((int) message[2]))
												.replace("%d", (String) message[3])
												.replace("%t", (String) message[4])
												.replace("%m", (String) message[5]));
				
			// If it doesn't exist, tell the player.
			} else if((int) message[0] == 1){
				player.sendMessage(config.getString("messages.invalid-message").replace("%m", args[1]));
			// If there was an error accessing the message, tell the player.
			} else {
				player.sendMessage(config.getString("messages.read-error"));
			}
		}
	}
	
	// Delete a specified message.
	public static boolean deleteMail(Player player, String[] args){
		int[] numbers;
		
		// If only one argument is specified, convert
		// it into an array separated by commas.
		if(args.length == 2){
			String[] stringNumbers = args[1].split(",");
			numbers = new int[stringNumbers.length];
			
			// Convert the individual Strings to int's.
			for(int a = 0; a < stringNumbers.length; a++){
				// Make sure that the number(s) are 32bit signed integers.
				try {
					numbers[a] = Integer.parseInt(stringNumbers[a]);
				} catch(Exception e){
					player.sendMessage(config.getString("messages.int-error"));
					
					return true;
				}
			}
		// Otherwise calculate the numbers in between
		// the two arguments and put them into an array.
		} else {
			int number1 = 0;
			int number2 = 0;
			
			// Make sure that the numbers are all 32bit signed integers.
			try {
				number1 = Integer.parseInt(args[1]);
				number2 = Integer.parseInt(args[2]);
			} catch(Exception e){
				player.sendMessage(config.getString("messages.int-error"));
				
				return true;
			}
			
			if(number1 <= number2){
				numbers = new int[(number2 - number1) + 1];
				
				for(int a = number1; a <= number2; a++){
					numbers[a - number1] = a;
				}
			} else {
				return false;
			}
		}
		
		// Check that all of the numbers are valid.
		String invalidNumbers = "";
		int invalidNumberCount = 0;
		
		boolean deleteError = false;
		
		// Loop over all the numbers in the array.
		for(int a = 0; a < numbers.length; a++){
			int returnStatus = (int) SQLData.readMail(player, numbers[a])[0];
			
			// If it exists, attempt to delete it.
			// If there is an error, flip a boolean that says so.
			if(returnStatus == 2){
				if(!SQLData.deleteMail(player, numbers[a])){
					deleteError = true;
				}
			// If it didn't exist, add it to a list.
			} else if(returnStatus == 1){
				if(args.length == 2){
					invalidNumbers = args[1];
					invalidNumberCount = 1;
				} else {
					if(invalidNumbers != ""){
						invalidNumbers += ", ";
					}
					
					invalidNumbers += (numbers[a] + a);
					invalidNumberCount++;
				}
			// If there was an error reading it in the
			// first place, flip a boolean that says so.
			} else {
				deleteError = true;
			}
		}
		
		// If there was an error deleting/numbering some or all of the messages, tell the player.
		if(!SQLData.numberMail(player) || deleteError){
			if(args.length == 2){
				player.sendMessage(config.getString("messages.delete-error"));
			} else {
				player.sendMessage(config.getString("messages.delete-multiple-error"));
			}
		} else {
			// If some or all of the messages don't exist, tell the player.
			if(invalidNumberCount == 1){
				player.sendMessage(config.getString("messages.invalid-message").replace("%m", invalidNumbers));
			} else if(invalidNumberCount > 1){
				player.sendMessage(config.getString("messages.invalid-messages").replace("%m", invalidNumbers));
			// Finally, if everything went fine, tell the player.
			} else {
				if(numbers.length == 1){
					player.sendMessage(config.getString("messages.delete"));
				} else {
					player.sendMessage(config.getString("messages.delete-multiple"));
				}
			}
		}
		
		return true;
	}
	
	// Wipe the player's entire inbox.
	public static void clearMail(Player player){
		// Tell the player that it was successful or that it failed.
		if(SQLData.clearMail(player)){
			player.sendMessage(config.getString("messages.clear"));
		} else {
			player.sendMessage(config.getString("messages.clear-error"));
		}
	}
	
	// Send specified player(s) a message.
	public static void sendMail(Player player, String[] args){
		String[] players = args[1].split(",");
		String message = "";
		
		// Make sure that all the specified players exist.
		String invalidPlayers = getInvalidPlayers(players);
		int invalidPlayerCount = 0;
		
		boolean send_error = false;
		
		for(int a = 2; a < args.length; a++){
			if(a != 2){
				message += " ";
			}
			
			message += args[a];
		}
		
		if(invalidPlayers != ""){
			// Clever hack to calculate the amount of values (separed by commas) in the String.
			invalidPlayerCount = (invalidPlayers.length() - invalidPlayers.replace(",", "").length()) + 1;
		}
		
		// If all the players exist, continue on with the sub-command.
		// Otherwise tell the player which players don't exist.
		if(invalidPlayerCount == 0){
			// Get the current date and time.
			String date = new SimpleDateFormat("dd MMMM yyyy").format(Calendar.getInstance().getTime());
			String time = new SimpleDateFormat("kk:mm:ss").format(Calendar.getInstance().getTime());
			
			// Send the message to each player.
			for(int a = 0; a < players.length; a++){
				// Get the first available message number in the player's inbox.
				int number = SQLData.getNumber(players[a]);
				
				// If it was successfull, send the message.
				// Otherwise tell the player that it wasn't.
				if(number > 0){
					if(!SQLData.sendMail(players[a], player, number, date, time, message)){
						send_error = true;
					}
				} else {
					player.sendMessage(config.getString("messages.number-error"));
				}
			}
			
			// If there was an error sending the message, tell the player.
			// Otherwise tell the player that it was successful.
			if(send_error){
				player.sendMessage(config.getString("messages.send-error"));
			} else {
				player.sendMessage(config.getString("messages.send"));
			}
		} else if(invalidPlayerCount == 1){
			player.sendMessage(config.getString("messages.invalid-player").replace("%u", invalidPlayers));
		} else {
			player.sendMessage(config.getString("messages.invalid-players").replace("%u", invalidPlayers));
		}
	}
	
	// If the player has new mail, send them a notification.
	public static void sendNotification(Player player){
		int number = SQLData.getNumber(player.getName());
		
		if(number == 1){
			player.sendMessage(config.getString("messages.new-message"));
		} else if(number > 1){
			player.sendMessage(config.getString("messages.new-messages").replace("%n", String.valueOf(number)));
		}
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
