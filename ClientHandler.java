package MiddleManServer;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler extends Thread {

	private Socket cclient;
	private HashMap<URL, byte[]> urlHashMap;
	private LinkedList<URL> urlList;

	public ClientHandler(Socket client, HashMap<URL, byte[]> urlHashMap,LinkedList<URL> urlList) {
		this.cclient = client;
		this.urlHashMap = urlHashMap;
		this.urlList = urlList;
	}

	public void run() {

		BufferedOutputStream clientOutgoing = null;
		BufferedInputStream clientIncoming = null;
		BufferedOutputStream realServerOutgoing = null;
		BufferedInputStream realServerIncoming = null;
		Socket realServerSocket = null;
		URL clientURL = null;
		boolean urlFound = false;
		byte[] cachedContent = new byte[1048576];

		try {
			clientOutgoing = new BufferedOutputStream(cclient.getOutputStream());
			clientIncoming = new BufferedInputStream(cclient.getInputStream());

			// Request which we get from client
			String clientRequest = "";
			byte[] buf = new byte[4096];// create a buffer of bytes 
			
			clientIncoming.read(buf, 0, 4096);
			clientRequest = new String(buf);
			String[] stringsInReq = clientRequest.split(" ");
			String urlString = stringsInReq[1].toString();
			clientURL = new URL(urlString);
			String hostName = clientURL.getHost();

			synchronized (urlHashMap) {
				if (urlHashMap.containsKey(clientURL)) {
					urlFound = true;
					int index = urlList.indexOf(clientURL); // Find the index of the cached URL
					urlList.remove(index); // Remove the URL from LinkdList from existing position
					urlList.add(0, clientURL); // Add the URL to the first position in the Linked List
					clientOutgoing.write(urlHashMap.get(clientURL), 0,urlHashMap.get(clientURL).length); 
					clientOutgoing.flush();
				}
			}
			
			if (!urlFound) {
				// This is the actual request which will be sent to web server
				String webserverRequest = stringsInReq[0] + " "+ stringsInReq[1] + "\n";// end of request
		
				// Create a new socket for connecting to destination server
				realServerSocket = new Socket(hostName, 80);
				realServerOutgoing = new BufferedOutputStream(
						realServerSocket.getOutputStream());
				realServerIncoming = new BufferedInputStream(
						realServerSocket.getInputStream());

				System.out.println(webserverRequest);
				realServerOutgoing.write(webserverRequest.getBytes());
				realServerOutgoing.flush();

				int bufCount = 0;
				int initial = 0;
				while ((bufCount = realServerIncoming.read(buf, 0, 4096)) > 0) {
					clientOutgoing.write(buf, 0, bufCount);
					clientOutgoing.flush();
					
					if ((initial + bufCount) <= cachedContent.length) {
						System.arraycopy(buf, 0, cachedContent, initial, bufCount);
						initial += bufCount;
					}
				}

				synchronized (urlHashMap) {
					if (urlList.size() != 100) {
						urlList.add(clientURL);
						urlHashMap.put(clientURL, cachedContent);
					} else {
						URL getLastInList = urlList.getLast();
						urlList.removeLast();
						urlList.addFirst(clientURL);
						urlHashMap.remove(getLastInList);
					}
				}

				System.out.println("Data Transfer done for " + webserverRequest);
			}

		} catch (SocketException e) {
			System.out.println("The connection is closed for: " + clientURL);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {

			try {
				if (realServerSocket != null) {
					realServerSocket.close();
				}
				cclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}