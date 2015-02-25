package MiddleManServer;

import java.io.*;
import java.net.*;
//import java.util.*;

public class ClientHandler extends Thread {

	private Socket cclient;

	public ClientHandler(Socket client) {
		this.cclient = client;
	}

	public void run() {

		BufferedOutputStream clientOutgoing = null;
		BufferedInputStream clientIncoming = null;
		BufferedOutputStream realServerOutgoing = null;
		BufferedInputStream realServerIncoming = null;
		Socket realServerSocket = null;

		try {

			clientOutgoing = new BufferedOutputStream(cclient.getOutputStream());
			clientIncoming = new BufferedInputStream(cclient.getInputStream());

			// Request which we get from client
			String clientRequest = "";

			// Take the incoming request
			byte[] buf = new byte[4096];// create a buffer of bytes to get images(changed from char to bny array )
			clientIncoming.read(buf, 0, 4096);
			clientRequest = new String(buf);
			String[] stringsInReq = clientRequest.split(" ");
			String urlString = stringsInReq[1].toString();

			URL clientURL = new URL(urlString);
			String hostName = clientURL.getHost();

			// This is the actual request which will be sent to web server
			String webserverRequest = stringsInReq[0] + " " + stringsInReq[1];

			// Create a new socket for connecting to destination server
			realServerSocket = new Socket(hostName, 80);
			realServerOutgoing = new BufferedOutputStream(
					realServerSocket.getOutputStream());
			realServerIncoming = new BufferedInputStream(
					realServerSocket.getInputStream());

			System.out.println(webserverRequest);
			realServerOutgoing.write(webserverRequest.getBytes());

			int bufCount = 0;
			while ((bufCount = realServerIncoming.read(buf, 0, 4096)) > 0) {
				System.out.println("The number of bytes read are: "+ bufCount );
				clientOutgoing.write(buf, 0, bufCount);
				clientOutgoing.flush();
			}
			System.out.println("Data Transfer done for " + webserverRequest);

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {

			try {
				realServerSocket.close();
				cclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
