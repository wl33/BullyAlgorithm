package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sound.midi.MidiDevice.Info;
import javax.xml.crypto.Data;

import gui.ProcessGUI;



public class Process {
	
	private int port;
	private int UUID;
	public static List<Integer> portList;
	public static List<Integer> UUIDList;
	private ServerSocket ss; // listen for client connection requests on this server socket
	private InputStream is;
	private OutputStream os;
	private ObjectInputStream inFromClient;
	private ObjectOutputStream outToClient;
	private ProcessGUI GUI;
	
	public Process(int port, int UUID,ProcessGUI GUI){
		this.port = port;
		this.UUID = UUID;		
		this.GUI = GUI;
		if (portList==null) {
			portList = new ArrayList<Integer>();
		}
		if (UUIDList==null) {
			UUIDList = new ArrayList<Integer>();
		}
	}
	
	public void run(){
		try {
			ss = new ServerSocket(this.port);
			System.out.println("Process"+ UUID +" is running.");
			while(true){
				Socket s = ss.accept();					
				ConnectionHandler conn = new ConnectionHandler(s,this);
				conn.start();						
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public void elect() {
		Socket clientSocket;
		ObjectOutputStream outToServer = null;
		
		try {
						
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd h:mm:ss");
			//System.out.println(UUIDList.size());
			for(int i=UUID+1;i<UUIDList.size();i++){
				clientSocket = new Socket("127.0.0.1",portList.get(i));
				outToServer = new ObjectOutputStream(clientSocket.getOutputStream());	
				outToServer.writeObject(String.valueOf(UUID));
				outToServer.writeObject("Elect");
				GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Elect "+UUID+" to "+UUIDList.get(i));
				System.out.println("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Elect "+UUID+" to "+UUIDList.get(i));
				outToServer.close();
			    clientSocket.close();
			}				
			
	        
	       
		} catch (IOException e) {
			// TODO Auto-generated catch block			
			System.out.println("elect exception");
		}
	}

	public void sendResult(){
		Socket clientSocket;
		ObjectOutputStream outToServer = null;
		
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd h:mm:ss");
			//System.out.println(UUIDList.size());
			for(int i=0;i<UUID;i++){
				clientSocket = new Socket("127.0.0.1",portList.get(i));
				outToServer = new ObjectOutputStream(clientSocket.getOutputStream());	
				outToClient.writeObject(String.valueOf(UUID));
				outToClient.writeObject(String.valueOf(UUID)+" is the leader");
				GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Result "+UUID+" to "+i);
				System.out.println("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Elect "+UUID+" to "+UUIDList.get(i));
				outToServer.close();
			}				
	       
		} catch (IOException e) {
			// TODO Auto-generated catch block			
			System.out.println("sendResult exception");
		}
	}
	
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getUUID() {
		return UUID;
	}

	public void setUUID(int uUID) {
		UUID = uUID;
	}

	public ProcessGUI getGUI() {
		return GUI;
	}

	public void setGUI(ProcessGUI gUI) {
		GUI = gUI;
	}
	
	
	
}
