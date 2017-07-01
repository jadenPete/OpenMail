package me.jadenPete.OpenMail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/*
 * This class is responsible for:
 *   - Connecting to the plugin's MySQL database.
 *   - Reading from the plugin's MySQL database.
 *   - Writing to the plugin's MySQL database.
 *   
 * It assumes that all data provided to it is valid,
 * and only handles errors related to MySQL.
 *   
 * It does not interact with the player or the chat in any way.
 * It's sole purpose is to be called upon by the Commands class.
 */
public class SQLData {
	// Variable to access the MySQL Database.
	public static Connection connection;
	
	// Connect to the MySQL Database.
	public static void getConnection(){
		String username = "jadenpete";
		String password = "password";
		
		String url = "jdbc:mysql://localhost/openmail";
		
		try {
			connection = DriverManager.getConnection(url, username, password);
		} catch(Exception e){
			System.out.println("Error connecting to the OpenMail database.");
		}
	}
	
	// Disconnect from the MySQL Database.
	public static void closeConnection(){
		try {
			connection.close();
		} catch(Exception e){
			System.out.println("Error disconnecting from the OpenMail database.");
		}
	}
	
	// Gets the next message number for a player's inbox.
	@SuppressWarnings("deprecation")
	public static int getNumber(String player){
		try {
			// Check whether the player is offline, so we can
			// convert their username to a UUID correctly.
			String playerUUID;
			
			if(Bukkit.getPlayer(player) == null){
				playerUUID = Bukkit.getOfflinePlayer(player).getUniqueId().toString();
			} else {
				playerUUID = Bukkit.getPlayer(player).getUniqueId().toString();
			}
			
			String query = "select number from mail where player='" + playerUUID + "'";
			PreparedStatement ps = connection.prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			
			int number;
			for(number = 1; rs.next(); number++);
			
			return number;
		} catch(Exception e){
			return 0;
		}
	}
	
	// Read mail from a player's inbox
	/* Return Object[] structure:
	 * 0 - Return status
	 *   2 - Exists
	 *   1 - Doesn't Exist
	 *   0 - Error 
	 * 1 - Sender
	 * 2 - Number
	 * 3 - Date
	 * 4 - Time
	 * 5 - Message
	 */
	public static Object[] readMail(Player player, int number){
		try {
			String query = "select number from mail where player='" + player.getUniqueId() + "' and number='" + number + "'";
			
			// If the message exists, carry on with the function.
			// Otherwise return a simple error with no data.
			if(connection.prepareStatement(query).executeQuery().next()){
				query = "select * from mail where player='" + player.getUniqueId() + "' and number='" + number + "'";
				PreparedStatement ps = connection.prepareStatement(query);
				ResultSet rs = ps.executeQuery();
				
				Object[] message = new Object[6];
				
				rs.first();
				
				message[0] = 2;
				message[1] = rs.getString("sender");
				message[2] = rs.getInt("number");
				message[3] = rs.getString("date");
				message[4] = rs.getString("time");
				message[5] = rs.getString("message");
				
				return message;
			} else {
				Object[] result = {1};
				
				return result;
			}
		} catch(Exception e){
			Object[] result = {0};
			
			return result;
		}
	}
	
	// Delete message from a player's inbox
	public static boolean deleteMail(Player player, int number){
		try {
			// Delete the message
			String query = "delete from mail where player='" + player.getUniqueId() + "' and number='" + number + "'";
			PreparedStatement ps = connection.prepareStatement(query);

			ps.executeUpdate();
			
			// Recalculate all message numbers but preserve the order.
			String query2 = "select number from mail where player='" + player.getUniqueId() + "' order by number";
			PreparedStatement ps2 = connection.prepareStatement(query2, ResultSet.TYPE_SCROLL_INSENSITIVE);
			ResultSet rs2 = ps2.executeQuery();
			
			for(int updatedNumber = 1; rs2.next(); updatedNumber++){
				String query3 = "update mail set number=? where player=? and number=?";
				PreparedStatement ps3 = connection.prepareStatement(query3);
				
				ps3.setInt(1, updatedNumber);
				ps3.setString(2, player.getUniqueId().toString());
				ps3.setInt(3, rs2.getInt("number"));
				
				ps3.executeUpdate();
			}
			
			return true;
		} catch(Exception e){
			e.printStackTrace();
			
			return false;
		}
	}
	
	// Clear a player's inbox.
	public static boolean clearMail(Player player){
		try {
			String query = "delete from mail where player='" + player.getUniqueId() + "'";
			PreparedStatement ps = connection.prepareStatement(query);

			ps.executeUpdate();
			
			return true;
		} catch(Exception e){
			return false;
		}
	}
	
	// Sends a message to a player's inbox.
	@SuppressWarnings("deprecation")
	public static boolean sendMail(String player, Player sender, int number, String date, String time, String message){
		try {
			String query = "insert into mail values(?,?,?,?,?,?)";
			PreparedStatement ps = connection.prepareStatement(query);
			
			// Check whether the player is offline, so we can
			// convert their username to a UUID correctly.
			String playerUUID;
			
			if(Bukkit.getPlayer(player) == null){
				playerUUID = Bukkit.getOfflinePlayer(player).getUniqueId().toString();
			} else {
				playerUUID = Bukkit.getPlayer(player).getUniqueId().toString();
			}
			
			ps.setString(1, playerUUID);
			ps.setString(2, sender.getUniqueId().toString());
			ps.setInt(3, number);
			ps.setString(4, date);
			ps.setString(5, time);
			ps.setString(6, message);
			
			ps.executeUpdate();
			
			return true;
		} catch(Exception e){
			return false;
		}
	}
}
