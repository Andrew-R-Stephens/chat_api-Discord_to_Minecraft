package DiscordBridgeBot.DiscordBridgeBot;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;

public class MainApp {

	public static JDA jda = null;
	public static String prefix = "/mc";

	public static void main(String[] args) throws LoginException {

		try {

			jda = JDABuilder.createDefault("NzQyMTE0Mzc5MDQyMTI3ODk0.XzBZ3w.IPqOhlixlt2odsfaNKn5LJUM7oo").build();
			jda.getPresence().setStatus(OnlineStatus.ONLINE);
			jda.setAutoReconnect(true);
			
			String ip = "localhost";
			int port = 25565;
			if(args.length == 1) 
				ip = args[0];
			if(args.length == 2) 
				port = Integer.parseInt(args[1]);
			CommandListener commandListener = new CommandListener(ip, port);
			jda.addEventListener(commandListener);

			System.out.println("Discord Bot enabled!\nPlease wait...");

		} catch (Exception e) {

			e.printStackTrace();
			System.out.println("Discord Bot failed to enable!");
		}

		try {
			Thread.sleep(2000L);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

}
