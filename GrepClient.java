import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

public class GrepClient {

    ArrayList<Requester> req_list;
	String query = "";

	GrepClient() {
		Properties prop = new Properties();
		String[] ports = null;
	    try {
	    	try {
	    		prop.load(new FileInputStream("config.properties"));
			} catch (FileNotFoundException e) {
				System.out.println("No config file");
				e.printStackTrace();
				System.exit(0);
			}
		    req_list = new ArrayList<Requester>();
	    	for (Object hostname: prop.keySet()) {
	    		ports = prop.getProperty((String)hostname).split(",");
	    	    for (String portOne: ports) {
	    	    	req_list.add(new Requester((String)hostname, portOne));
	    	    }
	    	}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class Requester extends Thread {
		Socket sock = null;
        String hostName = null, portOne = null;
        public Requester(String _hostName, String _portOne) {
            hostName = _hostName;
            portOne = _portOne;
			System.out.printf("Client: Construct: Querying %s %s\n", hostName, portOne);
	        try {
	            sock = new Socket(hostName, Integer.parseInt(portOne));
	        } catch (UnknownHostException e) {
	            System.err.println("Don't know about host: localhost.");
	            return;
	        } catch (IOException e) {
	            System.err.println("Couldn't get socket for "
	                               + "the connection to: localhost.");
	            return;
	        }
			System.out.println("Client: Construct Done");
        }

		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("Client: Thread Start Send");
			PrintWriter out = null;
	        BufferedReader in = null;
	        String servLine = null;
	        if (sock == null) {
	        	return;
	        }
	        try {
	            out = new PrintWriter(sock.getOutputStream(), true);
	            in = new BufferedReader(new InputStreamReader(
	                                        sock.getInputStream()));
	        } catch (IOException e) {
	            System.err.println("Couldn't get I/O for "
	                               + "the connection to: localhost.");
	            return;
	        }

	        System.out.println("Client: Hi");
	        out.println("grep" + query);
	        try {
	        	while ((servLine = in.readLine()) != null) {
	        		System.out.println("(" + hostName + "," + portOne + "): " + servLine);
	        	}
	        } catch (IOException e) {
	            System.err.println("Couldn't read for "
                        + "the connection to: localhost.");
	            return;
	        }
			System.out.println("Client: Thread Finish Send");
		}

	}
	
	public void startrun(String[] arg) {
		// TODO Auto-generated method stub
        if (arg.length == 0) {
            return;
        }
        for (String cli : arg)
            query += " " + cli;
        System.out.println(query);
		for (Requester req: req_list) {
			req.start();
		}
		joinThreads();
 	}

	public void joinThreads() {
		for (Requester req: req_list) {
			try {
				req.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GrepClient client = new GrepClient();
		client.startrun(args);
	}

}
