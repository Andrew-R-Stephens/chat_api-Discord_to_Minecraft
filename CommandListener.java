package DiscordBridgeBot.DiscordBridgeBot;

import java.awt.Color;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

	private BridgeClient client = null;
	private Thread clientThread = null;

	private long lastTextChannelID = 0;
	
	private String ip;
	private int port;

	public CommandListener(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public void onReady(ReadyEvent e) {

		client = new BridgeClient(this);
		clientThread = new Thread(client);
		clientThread.start();

		System.out.println("Bot enabled.");
	}

	public void onDisable() {
		System.out.println("Bot disabled.");
	}

	public void onDisconnect() {

	}

	public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

		boolean isCheckingStatus = false;

		User u = e.getAuthor();
		TextChannel c = e.getChannel();
		lastTextChannelID = c.getIdLong();
		Message m = e.getMessage();
		System.out.println("saved id: " + lastTextChannelID);

		String[] args = m.getContentRaw().split("\\s+");

		if ((!u.isBot()) && args.length >= 1) {

			if (args[0].equalsIgnoreCase(MainApp.prefix)) {

				// one argument commands
				if (args.length == 2) {
					if (args[1].equalsIgnoreCase("help")) {

						EmbedBuilder eb = new EmbedBuilder();
						eb.setColor(new Color(0, 255, 55));
						eb.setAuthor("Bridges Discord with the Bone Babies Minecraft server.");
						eb.setTitle("Available Commands");
						eb.addField(new Field("/mc help", "Displays list of available commands.", false));
						eb.addField(new Field("/mc status", "Checks if the server is online.", false));
						eb.addField(new Field("/mc start", "Starts the server remotely.", false));
						eb.addField(new Field("/mc list", "Lists players currently on the server.", false));
						eb.addField(new Field("/mc say", "(Still in progress) Communicate directly to the server chat.",
								false));

						c.sendMessage(eb.build()).queue();

					} else if (args[1].equalsIgnoreCase("start")) {
						if (!isCheckingStatus) {
							c.sendMessage("Checking server...").queue();
							if ((int) (serverIsRunning()[0]) == 1) {
								c.sendMessage("Server is already running.").queue();
							} else {
								c.sendMessage(
										"Server is unresponsive. Attempting to start server. (This may take a moment)...")
										.queue();

								try {
									Runtime.getRuntime().exec("cmd.exe /k start run.bat");
								} catch (IOException e2) {
									System.out.println(e2.getMessage());
								}

								try {
									Thread.sleep(15000);
								} catch (InterruptedException e1) {
									e1.printStackTrace();
								}

								c.sendMessage("Checking server status. Please wait...").queue();
								if ((int) (serverIsRunning()[0]) == 1)
									c.sendMessage("Server should now be **online**.").queue();
								else
									c.sendMessage("Server is still **offline**. Contact server owner for assistance.")
											.queue();

							}
						}

					} else if (args[1].equalsIgnoreCase("status")) {

						c.sendMessage("Checking server status. Please wait...").queue();
						double[] serverStatus = serverIsRunning();
						if (serverStatus[0] == 1) {
							c.sendMessage("Server is **online** - *(Responded in " + (int) serverStatus[1] + " ms)*")
									.queue();
						} else
							c.sendMessage("Server is **offline**.").queue();

					} else if (args[1].equalsIgnoreCase("list")) {

						client.setOutputMessage(args[1]);
						
						/*
						File file = new File("plugins/MCServerBot/online-users.txt");
						BufferedReader reader = null;
						try {
							reader = new BufferedReader(new FileReader(file));
							playerName = reader.readLine();
							while (playerName != null) {

								playerList += playerName + "\n";
								playerCount++;
								playerName = reader.readLine();

							}
						} catch (FileNotFoundException e2) {
							e2.printStackTrace();
						} catch (IOException e2) {
							e2.printStackTrace();
						}

						try {
							reader.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						*/

					} else if (args[1].equalsIgnoreCase("map")) {

						EmbedBuilder eb = new EmbedBuilder();
						eb.setColor(new Color(0, 255, 55));
						eb.setTitle("Tritium's Server Dynmap");
						eb.setDescription("Link to Dynmap");
						eb.setThumbnail("https://happydiggers.net/attachment.php?attachmentid=3614&d=1519542718&stc=1");
						eb.addField(new Field("", "http://"+ip+":8200/", true));

						c.sendMessage(eb.build()).queue();
						
					}
				}

				// two argument commands
				if (args.length >= 3) {
					if (args[1].equalsIgnoreCase("say")) {
						if (client != null && client.isConnected()) {
							String s = m.getContentRaw();
							client.setOutputMessage(args[1] + "/" + u.getName() + "/"
									+ s.substring(args[1].length() + 1 + s.indexOf(args[1])));
						}
					}
				}
			}
		}
	}

	public double[] serverIsRunning() {

		double available = 1;
		long time = 0;

		Socket s = null;
		try {
			long start = System.currentTimeMillis();
			s = new Socket(ip, port);
			if (!s.isClosed())
				s.close();
			long stop = System.currentTimeMillis();
			time = stop - start;
		} catch (UnknownHostException e) {
			available = 0;
			s = null;
		} catch (IOException e) {
			available = -1;
			s = null;
		} catch (NullPointerException e) {
			available = -2;
			s = null;
		}

		return new double[] { available, (double) time };

	}

	public long getLastTextChannelID() {
		return lastTextChannelID;
	}
	
	public String getIP() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
}
