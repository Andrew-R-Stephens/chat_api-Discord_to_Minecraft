package DiscordBridgeBot.DiscordBridgeBot;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import net.dv8tion.jda.api.EmbedBuilder;

public class BridgeClient implements Runnable {

	private CommandListener commandListener = null;
	
	private Socket serverSocket = null;
	private PrintWriter server_out = null;
	private BufferedReader server_in = null;

	private String fromServer = null, fromDiscord = null;

	private boolean isRunning = true;
	
	public BridgeClient(CommandListener commandListener) {
		
		this.commandListener = commandListener;
		
	}

	public void run() {

		Thread socket_connect_thread = new Thread(new Runnable() {
			public void run() {
				boolean errorSent = false;
				while (isRunning) {
					if (!isConnected()) {
						try {
							connect();
						} catch (ConnectException e) {
							if (!errorSent) {
								System.out.println("Error: " + e);
								errorSent = true;
							}
						} catch (UnknownHostException e) {
							if (!errorSent) {
								System.out.println("Error: " + e);
								errorSent = true;
							}
						} catch (IOException e) {
							if (!errorSent) {
								System.out.println("Error: " + e);
								errorSent = true;
							}
						}
					}

					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						System.out.println(e);
					}
				}
			}
		});

		try {
			socket_connect_thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Thread client_output_thread = new Thread(new Runnable() {
			public void run() {
				while (isRunning) {
					if (isConnected()) {
						if (fromDiscord != null) {
							server_out.println(fromDiscord);
							System.out.println("To Server: " + fromDiscord);
							fromDiscord = null;
						}
					}

					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		/**
		 * From Server
		 **/
		Thread client_input_thread = new Thread(new Runnable() {
			public void run() {

				boolean errorSent = false;
				while (isRunning) {
					try {
						
						if (isConnected() && ((fromServer = server_in.readLine()) != null)) {
							errorSent = false;

							System.out.println(fromServer);
							String[] args = fromServer.split("/", 3);

							if(args[0].equals("say") && args.length >= 3) {
								
								MainApp.jda.getTextChannelById(commandListener.getLastTextChannelID()).sendMessage("`MC Server` **" + args[1] + "**: " + args[2]).queue();
								System.out.println("To Discord: " + fromServer);
								
							}
							else 
								if(args[0].equals("list") && args.length == 2) {
								
									String[] namesRaw = args[1].split("#");
	
									int count = 0;
									String names = "";
									for(String s: namesRaw)
										if(!s.equals("")) {
											count ++;
											names += "\n" + s;
										}
									
									EmbedBuilder messageList = new EmbedBuilder();
									messageList.setColor(new Color(0, 255, 55));
									messageList.setTitle("Players Online: " + count);
									messageList.setDescription(names);
	
									System.out.println("using id: " + commandListener.getLastTextChannelID());
									
									MainApp.jda.getTextChannelById(commandListener.getLastTextChannelID())
											.sendMessage(messageList.build()).queue();
									
									System.out.println("To Discord: " + fromServer);
									
								}
									
							fromServer = null;
							
						}
					} catch (SocketException e) {
						if (!errorSent) {
							System.out.println("Error: " + e);
							if (!errorSent) {
								System.out.println("Error: " + e);
								errorSent = true;
							}
						}
					} catch (IOException e) {
						System.out.println(e.getMessage());
					}

					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						System.out.println(e.getMessage());
					}
				}
			}
		});

		socket_connect_thread.start();
		client_output_thread.start();
		client_input_thread.start();
	}

	public void setOutputMessage(String msg) {

		fromDiscord = msg;

	}

	public boolean isConnected() {
		return !(serverSocket == null || serverSocket.isClosed() || server_in == null);
	}

	public void connect() throws UnknownHostException, IOException {
		
		serverSocket = new Socket(commandListener.getIP(), 8100);
		System.out.println("Server Socket connected!");
		server_out = new PrintWriter(serverSocket.getOutputStream(), true);
		server_in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
		
	}

}
