package Distributed1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

public class GrepServer {

		
	ServerSocket sock = null;
	String hostname = null, localPort = null;
	
	public GrepServer() {
		System.out.println("Listen: Construct Start");
		for (int i = 1024; i < 1500; i ++) {
			try {
			    sock = new ServerSocket(i);
			} 
			catch (IOException e) {
			    System.out.printf("Could not listen on port: %d", i);
			    continue;
			}
			break;
		}
		
		System.out.println("Listen: Construct End");
		Properties prop = new Properties();
		String ports = null;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
			localPort = Integer.toString(sock.getLocalPort());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
	    	try {
	    		prop.load(new FileInputStream("config.properties"));
			} catch (FileNotFoundException e) {
				System.out.println("Warning, Creating a new config file");
				File file = new File("config.properties");
				file.createNewFile();
				file.exists();
				//Retrying
				prop.load(new FileInputStream("config.properties"));
				e.printStackTrace();
			}
			try {
				ports = prop.getProperty(hostname);
				if (ports  == null) {
					prop.setProperty(hostname, localPort);
				} else if (!ports.contains(localPort)) {
					ports = ports + "," + localPort;
					prop.put(hostname, ports);
				} else {
					System.out.println("Unhandled Case, Ports already in file");
				}
	    	} catch (NullPointerException e) {
				prop.setProperty(hostname, localPort);
	    		e.printStackTrace();
	    	}
			prop.store(new FileOutputStream("config.properties"), null);			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	private class Worker implements Runnable {
		
		Socket clientSocket;
		
		public Worker(Socket clientSocket) {
		    this.clientSocket = clientSocket;	
		}
		
		public void run() {
			System.out.println("Worker: Begin");
			try {
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String inputLine, outputLine, cmdOut;
	
				if ((inputLine = in.readLine()) != null) {
					System.out.printf("Worker: Received %s\n", inputLine);
					if (inputLine.startsWith("grep ")) {
						String args[] = inputLine.substring(5).split(" ");
						String command = args[0] + "|" + args[1];
						String command1[] = {"egrep", command, "/tmp/logfile_group425_45"};
						Process p = Runtime.getRuntime().exec(command1);
						System.out.println("Worker:Command " + command);
						BufferedReader pin = new BufferedReader(new InputStreamReader(p.getInputStream()));
						while ((cmdOut = pin.readLine()) != null) {
							System.out.println(cmdOut);
						}
						p.waitFor();
						System.out.println("Exit Status " + p.exitValue());
						outputLine = "Hi There! from Server";
						out.println(outputLine);
					} else {
						outputLine = "Unexpected";
						out.println(outputLine);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Worker: Ending");
		}
	}

		
	private class Acceptor implements Runnable {
		Socket clientSocket = null;


		public void run() {
			// TODO Auto-generated method stub
			System.out.println("Acceptor: Start");
			while (true) {
				try {
				    clientSocket = sock.accept();
				} 
				catch (IOException e) {
				    System.out.printf("Accept failed for main socket %d", sock.getLocalPort());
				    System.exit(-1);
				}
				System.out.println("Acceptor: Got Connection");
				Worker work = new Worker(clientSocket);
				Thread t = new Thread(work);
				t.start();
			}
		}
	
    }

	private class Shutdown implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Properties prop = new Properties();
			String ports = null;
		    try {
		    	prop.load(new FileInputStream("config.properties"));
				ports = prop.getProperty(hostname);
					if (ports  != null) {
						prop.setProperty(hostname, localPort);
						if (ports.contains(localPort)) {
							ports = ports + "," + localPort;
							prop.put(hostname, ports);
						} else {
							System.out.println("Ports absent in file");
						}
					} else {
						System.out.println("Hosname absent in file");
					}
				prop.store(new FileOutputStream("config.properties"), null);			
			} catch (FileNotFoundException e) {
				System.out.println("Could not find file to delete");
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	public void startrun() {
		// TODO Auto-generated method stub
		Acceptor accept = new Acceptor();
		Thread t = new Thread(accept);
		t.start();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Main: Begin");
		GrepServer serv = new GrepServer();
		serv.startrun();
		System.out.println("Main: Done");
		while(true);
	}
}
