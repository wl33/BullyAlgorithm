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
    private  List<Integer> portList;
	private  List<Integer> UUIDList;
	private ServerSocket ss; // listen for client connection requests on this server socket
	private ProcessGUI GUI;
	private boolean[] crashStatus;
	private int leader;
	private double crashProssibility;
	private double timeoutProssibility;
	private SimpleDateFormat formatter;
	private boolean[] receiveOK;
	private boolean isTimeOut;
	private boolean isCrash;
	
	public Process(int port, int UUID,ProcessGUI GUI, List<Integer> portList, List<Integer> UUIDList){
		this.port = port;
		this.UUID = UUID;		
		this.GUI = GUI;
		this.portList = portList;
		this.UUIDList = UUIDList;
		crashStatus = new boolean[portList.size()];
		receiveOK = new boolean[portList.size()];
		leader=-1;
		crashProssibility=-1;
		timeoutProssibility=-1;
		formatter = new SimpleDateFormat("yyyy-MM-dd h:mm:ss");//[2016-03-22 12:14:01 | 1 ]
	}
	
	public void run(){
		try {
			
			ss = new ServerSocket(this.port);
			System.out.println("Process"+ UUID +" is running.");
			while(true){
				if (Math.random()<=crashProssibility || isCrash) { //
					GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Random Error: Crash Error Accur");
				    break;
				}
				
				Socket s = ss.accept();					
				//System.out.println(new Date());
				//ConnectionHandler conn = new ConnectionHandler(s,this);
				//conn.run();			
				handleConnection(s);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	

	private void handleConnection(Socket conn){
		try {
			InputStream is=conn.getInputStream();
			ObjectInputStream inFromClient = new ObjectInputStream(is);
	                
	        //get message from sender
	        String senderUUID = (String) inFromClient.readObject();
	        String message = (String) inFromClient.readObject();
	        
	        if (isTimeOut) {
	        	GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Random Error: TimeOut Occur");
				return;
			}
	        
	        if (message.equals("OKAY")) {
	        	//System.out.println("ok!!!!!!!!!");
	        	receiveOK[Integer.valueOf(senderUUID)]=true;
				GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Internal: Received OKAY from "+ senderUUID);
			} else if (message.equals("Elect")) {	//election business logic							
				sendOK(senderUUID);
				if(UUID != UUIDList.get(UUIDList.size()-1)){//not the process having biggest UUID
					//send elect messages to those processes having bigger UUID
					if (leader!=-1) {//already know the leader, thus do not need to elect.
						GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: Already known the leader is "+leader+". No need to trigger an election");
					}else{
						GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Internal: Trigger an election");
						elect();
					}
				}
				else{//this process is the one having biggest UUID, thus ought to send Result					
					sendResult();						
				}
			}else {//result message
				leader = Integer.valueOf(senderUUID);
				GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Internal: Received Result from "+ senderUUID);
			}
	        inFromClient.close();
	        //outToClient.close();
	        is.close();
	        conn.close();
			
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		
	}
	
	public void elect() {
		Socket clientSocket = null;
		ObjectOutputStream outToServer = null;
					
			//System.out.println(UUIDList.size());
			for(int i=UUID+1;i<UUIDList.size();i++){
			   try {
				clientSocket = new Socket("127.0.0.1",portList.get(i));
				outToServer = new ObjectOutputStream(clientSocket.getOutputStream());	
				outToServer.writeObject(String.valueOf(UUID));
				outToServer.writeObject("Elect");
				GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Elect "+UUID+" to "+ UUIDList.get(i));
				//System.out.println("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Elect "+UUID+" to "+ Process.UUIDList.get(i));
				outToServer.close();
		        clientSocket.close();
		        }catch (IOException e) {
					// TODO Auto-generated catch block			
					System.out.println("elect exception");
					GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Election Error: Crash Error "+ UUIDList.get(i));
		            crashStatus[UUIDList.get(i)] = true;
		            if(isRankTopNow()){
		            	sendResult();
		            	break;
		            }
		        }
			}
			
			Thread thread = new Thread( new Runnable() {
				public void run() {
					try {
						Thread.sleep(1000);
						for(int i=UUID+1;i<UUIDList.size();i++){
							if (receiveOK[i] == false) {
								System.out.println("Process "+ i + "timeout");
								GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Election Error: "+ "Fail to receive OKAY from "+UUIDList.get(i) + " within accepted time");
							}
						}
						//do further steps
						if (isRankTopNow()) sendResult();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			thread.start();
			
		}
	
	private boolean isRankTopNow(){
		boolean isRankTopNow=true;
		for (int i = UUID+1; i < crashStatus.length; i++) {
			if(crashStatus[i]==false){
				isRankTopNow=false;
				break;
			}
		}
		if(isRankTopNow){
		    System.out.println(UUID+" is Top One now");
			return true;
		}
		
		isRankTopNow=true;
		for(int i=receiveOK.length-1; i>=UUID+1; i--){
			if (receiveOK[i]==true) {
				isRankTopNow=false;
				break;
			}
		}
		
		if(isRankTopNow){
			System.out.println(UUID+" is Top One now");
			return true;
		}
		
		return false;
		
	}
	
	
	public void sendResult(){
		leader = UUID;
		Socket clientSocket;
		ObjectOutputStream outToServer = null;
		
			for(int i=0;i<UUID;i++){
				try {
				clientSocket = new Socket("127.0.0.1",portList.get(i));
				outToServer = new ObjectOutputStream(clientSocket.getOutputStream());	
				outToServer.writeObject(String.valueOf(UUID));
				outToServer.writeObject(String.valueOf(UUID)+" is the leader");
				GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Result "+UUID+" to "+i);
				System.out.println("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Result "+UUID+" to "+ UUIDList.get(i));
				outToServer.close();
		        clientSocket.close();
		        }catch (IOException e) {
					// TODO Auto-generated catch block			
					System.out.println("sendResult exception");
					GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] SendResult Error: Crash Error "+ UUIDList.get(i));
					crashStatus[UUIDList.get(i)] = true;
		        }
			}				
			     
	}
	
	public void sendOK(String sendUUID){
		Socket clientSocket = null;
		ObjectOutputStream outToServer = null;
		try {	
			//System.out.println(UUIDList.size());
			System.out.println(Integer.valueOf(sendUUID));
			clientSocket = new Socket("127.0.0.1",portList.get(Integer.valueOf(sendUUID)));
			outToServer = new ObjectOutputStream(clientSocket.getOutputStream());	
			outToServer.writeObject(String.valueOf(UUID));
			outToServer.writeObject("OKAY");
			System.out.println("send ok");
			GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send OKAY "+UUID+" to "+ sendUUID);
							
	        outToServer.close();
	        clientSocket.close();
	       
		} catch (IOException e) {
			// TODO Auto-generated catch block			
			System.out.println("SendOK exception");
			GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] SendOK Error: Crash Error "+ sendUUID);
			crashStatus[Integer.valueOf(sendUUID)] = true;
			System.err.println(e);
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

	public double getCrashProssibility() {
		return crashProssibility;
	}

	public void setCrashProssibility(double crashProssibility) {
		this.crashProssibility = crashProssibility;
	}

	public double getTimeoutProssibility() {
		return timeoutProssibility;
	}

	public void setTimeoutProssibility(double timeoutProssibility) {
		this.timeoutProssibility = timeoutProssibility;
	}

	public boolean isTimeOut() {
		return isTimeOut;
	}

	public void setTimeOut(boolean isTimeOut) {
		this.isTimeOut = isTimeOut;
	}

	public boolean isCrash() {
		return isCrash;
	}

	public void setCrash(boolean isCrash) {
		this.isCrash = isCrash;
	}
	
	
	
}
