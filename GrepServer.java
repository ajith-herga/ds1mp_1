
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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GrepServer {

		
	ServerSocket sock = null;
	String hostname = null, localPort = null;
	Acceptor acceptor;
	
	public GrepServer() {
		System.out.println("Listen: Construct Start");
		for (int i = 1024; i < 1500; i ++) {
			try {
			    sock = new ServerSocket(i);
			} 
			catch (IOException e) {
			    System.out.printf("Could not listen on port: %d, Trying %d", i, i+1);
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
			System.out.println("Server running on host: "+ hostname + "port: " + localPort);
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
				if (file.exists()) {
					System.out.println("File Exists");
				}
				//Retrying
				prop = new Properties();
				prop.load(new FileInputStream("config.properties"));
				e.printStackTrace();
			}
			ports = prop.getProperty(hostname);
			if (ports  == null) {
				prop.setProperty(hostname, localPort);
			} else if (!ports.contains(localPort)) {
				ports = ports + "," + localPort;
				prop.put(hostname, ports);
			} else {
				System.out.println("Unhandled Case, Ports already in file");
			}
			prop.store(new FileOutputStream("config.properties"), null);		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	private class Worker extends Thread {
		
		Socket clientSocket;
		
		public Worker(Socket clientSocket) {
		    this.clientSocket = clientSocket;	
		}
		
		public void run() {
			System.out.println("Worker: Begin");

			PrintWriter out = null;
			BufferedReader in = null;
			try {
				String inputLine, cmdOut;
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				if ((inputLine = in.readLine()) != null) {
					System.out.printf("Worker: Received %s\n", inputLine);
					if (inputLine.startsWith("grep ")) {
						String args[] = inputLine.substring(5).split(" ");
						String command = args[0] + ".*___";
						if (args.length == 2) {
							if (args[1].startsWith("^")) {
								command += args[1].substring(1);
							} else {
								command += ".*" + args[1];
							}
						}
						String command1[] = {"grep", command, "/tmp/logfile_group425_45"};
						Process p = Runtime.getRuntime().exec(command1);
						System.out.println("Worker:Command " + command);
						BufferedReader pin = new BufferedReader(new InputStreamReader(p.getInputStream()));
						while ((cmdOut = pin.readLine()) != null) {
							out.println(cmdOut);
						}
						p.waitFor();
						System.out.println("Exit Status " + p.exitValue());
					} else {
						out.println("Unexpected");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				if (out != null)
					out.close();
				try {
					if (in != null) 
						in.close();
					if (clientSocket != null)
						clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Worker: Ending");
		}
	}

		
	private class Acceptor extends Thread {
		Socket clientSocket = null;
		List<Worker> workers = new ArrayList<Worker>();

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
				Worker worker = new Worker(clientSocket);
				worker.start();
				workers.add(worker);
			}
		}
	
    }

	public void startrun() {
		// TODO Auto-generated method stub
		acceptor = new Acceptor();
		acceptor.start();
	}
	
	public void shutdown(){
		acceptor.interrupt();
    	try {
			System.out.println("Updating config");
    		Properties prop = new Properties();
    		prop.load(new FileInputStream("config.properties"));
			String ports = prop.getProperty(hostname);
			if (ports  != null && ports.contains(localPort)) {
				System.out.println("Old Ports for this host - "+ports);
				System.out.println(localPort);
				ports = ports.replaceAll(localPort, "");
				ports = ports.replaceAll(",,", ",");
				if(ports.startsWith(","))
					ports = ports.substring(1);
				if(ports.endsWith(","))
					ports = ports.substring(0,ports.length()-1);
				System.out.println("New Ports for this host - "+ ports);
				prop.put(hostname, ports);
			}
			prop.store(new FileOutputStream("config.properties"), null);		
		} catch (FileNotFoundException e) {
			System.out.println("Config file not found.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
/*		try {
			sock.close();
		} catch (IOException e) {
			System.out.println("Caught Exception while closing the socket");
			e.printStackTrace();
		}
*/		System.out.println("Server:stopped ");		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Main: Begin");
		final GrepServer serv = new GrepServer();
		System.out.println("Server: Port Acquired");		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run(){
				System.out.println("Server: Trying to stop ");		
				serv.shutdown();
			}
		});
		System.out.println("Server: Shutdown hook attached");		
		serv.startrun();
		System.out.println("Server:started ");		
		System.out.println("Main: Done");
		while(true){
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println("Main interrupted!");
				e.printStackTrace();
			}
		}
	}
}
