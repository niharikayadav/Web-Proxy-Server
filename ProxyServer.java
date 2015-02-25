package MiddleManServer;

import java.io.*;
import java.net.*;

public class ProxyServer {
	public static void main(String[] args){
		
		try {
			@SuppressWarnings("resource")
			ServerSocket serverSock = new ServerSocket(9623);
			System.out.println("Waiting for Client on port " + serverSock.getLocalPort() + "...");
			while (true) {
				Socket connectedClient = serverSock.accept();
				ClientHandler ch = new ClientHandler(connectedClient);
				ch.start();
			}
		} catch (SocketException se) {
			se.printStackTrace();
		}
		catch (IOException e) {
			System.out.println("Exception occured, Please try and connect again!!");
			e.printStackTrace();
		}
	}
}