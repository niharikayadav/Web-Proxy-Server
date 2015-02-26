package MiddleManServer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

public class ClientHandler extends Thread {

	private Socket cclient;
	private HashMap<URL, byte[]> urlHashMap;
	private LinkedList<URL> urlList;
	public ClientHandler(Socket client, HashMap<URL, byte[]> urlHashMap, LinkedList<URL> urlList) {
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
		byte[] cachedContent = new byte[100000];

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
			
			//System.out.println("Hostname is" + hostName);
			if(urlHashMap.containsKey(clientURL)) {
				
				System.out.println("*******In the loop if url is in HashMap!!*****");
				int index = urlList.indexOf(clientURL); // Find the index of the cached URL
				System.out.println("The index at which URL is stored in map is:" + index);
				urlList.remove(index); // Remove the URL from LinkdList from existing position
				urlList.add(0, clientURL); // Add the URL to the first position in the Linked List
				clientOutgoing.write(urlHashMap.get(clientURL), 0, urlHashMap.get(clientURL).length); // Fetch the content of the URL from the cached records
				clientOutgoing.flush();
			}
			else {
				// This is the actual request which will be sent to web server
				String webserverRequest = stringsInReq[0] + " " + stringsInReq[1] + "\n";// back slash n is shows the end of request

				// Create a new socket for connecting to destination server
				realServerSocket = new Socket(hostName, 80);
				realServerOutgoing = new BufferedOutputStream(realServerSocket.getOutputStream());
				realServerIncoming = new BufferedInputStream(realServerSocket.getInputStream());
			
				//PrintWriter pIn = new PrintWriter(realServerSocket.getOutputStream());// when request is send as String

				System.out.println(webserverRequest);
				realServerOutgoing.write(webserverRequest.getBytes());
				realServerOutgoing.flush();
				//pIn.print(webserverRequest);
				//pIn.flush();// shows the end of buffered request
			
				/*int bufCount = 0;
				while ((bufCount = realServerIncoming.read(buf, 0, 4096)) > 0) {
					System.out.println("The number of bytes read are: "+ bufCount );
					clientOutgoing.write(buf, 0, bufCount);
					clientOutgoing.flush();
				}*/
				int bufCount = 0;
				int initial =0;
				while ((bufCount = realServerIncoming.read(buf, 0, 4096)) > 0) {
						//System.out.println("The number of bytes read are: "+ bufCount );
						clientOutgoing.write(buf, 0, bufCount);
						clientOutgoing.flush();
						if(initial <= cachedContent.length) {
							System.out.println("The value of initial is: "+ initial);
							System.arraycopy(buf, 0, cachedContent, initial, bufCount);
							initial += bufCount;
						}
						
					}
				
					if(urlList.size() != 100) {
						urlList.add(clientURL);
						//System.out.println("The value to be stored in HashMap are:" +cachedContent.toString());
						urlHashMap.put(clientURL, cachedContent);
					} else {
						URL getLastInList = urlList.getLast();
						urlList.removeLast();
						urlList.addFirst(clientURL);
						urlHashMap.remove(getLastInList);
					}
					
					//Printing out the cached result..
					System.out.println("Data Transfer done for " + webserverRequest);	
					
					System.out.println("Printing all the values stored in hashmap: ");
					Set urlSet = urlHashMap.entrySet();
					Iterator it = urlSet.iterator();
					
					while(it.hasNext()) {
						@SuppressWarnings("unchecked")
						Map.Entry<URL, byte[]> mapEntry = (Entry<URL, byte[]>) it.next();
						System.out.println("The key stored in the HashMap are:  "+ mapEntry.getKey());
						
						/*for( int i =0; i<mapEntry.getValue().length; i++) {
							System.out.println( mapEntry.getValue()[i]);
						}*/
					}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {

			try {
				if(realServerSocket != null) {
					realServerSocket.close();
				}
				cclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
