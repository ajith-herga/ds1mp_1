package Distributed1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class GrepClient {

	Requester req;
	
	GrepClient() {
		req = new Requester();
	}
	
	private class Requester implements Runnable {
		Socket sock = null;
        public Requester() {
			System.out.println("Client: Construct");
	        try {
	            sock = new Socket("ajith-Lenovo-IdeaPad-P580", 1024);
	        } catch (UnknownHostException e) {
	            System.err.println("Don't know about host: localhost.");
	            System.exit(1);
	        } catch (IOException e) {
	            System.err.println("Couldn't get socket for "
	                               + "the connection to: localhost.");
	            System.exit(1);
	        }
			System.out.println("Client: Construct Done");
        }

		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("Client: Thread Start Send");
			PrintWriter out = null;
	        BufferedReader in = null;
	        try {
	            out = new PrintWriter(sock.getOutputStream(), true);
	            in = new BufferedReader(new InputStreamReader(
	                                        sock.getInputStream()));
	        } catch (IOException e) {
	            System.err.println("Couldn't get I/O for "
	                               + "the connection to: localhost.");
	            System.exit(1);
	        }

	        System.out.println("Client: Hi");
	        out.println("grep reads running");
	        try {
	        	System.out.println("Client: " + in.readLine());
	        } catch (IOException e) {
	            System.err.println("Couldn't read for "
                        + "the connection to: localhost.");
	            System.exit(1);
	        }
			System.out.println("Client: Thread Finish Send");
		}

	}
	
	public void startrun() {
		// TODO Auto-generated method stub
		Thread t = new Thread(req);
		t.start();
 	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GrepClient client = new GrepClient();
		client.startrun();
	}

}
