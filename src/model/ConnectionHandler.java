//package model;
//
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.io.OutputStream;
//import java.io.PrintStream;
//import java.io.PrintWriter;
//import java.net.Socket;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Set;
//import java.util.UUID;
//
//import gui.ProcessGUI;
//
//
//public class ConnectionHandler {//extends Thread
//	private Socket conn;       // socket representing TCP/IP connection to Client
//	private InputStream is;    // get data from client on this input stream	
//	private OutputStream os;   // can send data back to the client on this output stream
//	private ObjectInputStream inFromClient;
//	private model.Process process;
//
//	
//	public ConnectionHandler(Socket conn,model.Process process){
//		this.conn = conn;	
//		this.process = process;
//	}
//	
//	public void run() {   
//		try {
//			is = conn.getInputStream();
//			os = conn.getOutputStream();				
//			//outToClient = new ObjectOutputStream(os);
//	        inFromClient = new ObjectInputStream(is);
//	        ProcessGUI GUI = process.getGUI();
//	        int UUID = process.getUUID();
//	                
//	        //get message from sender
//	        String senderUUID = (String) inFromClient.readObject();
//	        String message = (String) inFromClient.readObject();
//	        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd h:mm:ss");//[2016-03-22 12:14:01 | 1 ]
//	        
//	        if (message.equals("OKAY")) {
//	        	System.out.println("ok!!!!!!!!!");
//				GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Internal: Received OKAY from "+ senderUUID);
//			} else if (message.equals("Elect")) {	//election business logic							
//				
//				sendOK(senderUUID);
//				if(UUID != Process.UUIDList.get(Process.UUIDList.size()-1)){//not the process having biggest UUID
//					//send elect messages to those processes having bigger UUID
//					GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Internal: Trigger an election");
//					elect();
//				}
//				else{//this process is the one having biggest UUID, thus ought to send Result					
//					sendResult();						
//				}
//			}else {//result message
//				GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Internal: Received Result from "+ senderUUID);
//			}
//	        inFromClient.close();
//	        //outToClient.close();
//	        is.close();
//	        os.close();
//	        conn.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//
//	public void elect() {
//		Socket clientSocket = null;
//		ObjectOutputStream outToServer = null;
//		
//		try {
//						
//			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd h:mm:ss");
//			//System.out.println(UUIDList.size());
//			int UUID = process.getUUID();
//			for(int i=UUID+1;i<Process.UUIDList.size();i++){
//				clientSocket = new Socket("127.0.0.1",Process.portList.get(i));
//				outToServer = new ObjectOutputStream(clientSocket.getOutputStream());	
//				outToServer.writeObject(String.valueOf(UUID));
//				outToServer.writeObject("Elect");
//				process.getGUI().appead("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Elect "+UUID+" to "+ Process.UUIDList.get(i));
//				//System.out.println("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Elect "+UUID+" to "+ Process.UUIDList.get(i));
//				outToServer.close();
//		        clientSocket.close();
//			}				
//			
//	        
//	       
//		} catch (IOException e) {
//			// TODO Auto-generated catch block			
//			System.out.println("elect exception");
//		}
//	}
//
//	public void sendResult(){
//		Socket clientSocket;
//		ObjectOutputStream outToServer = null;
//		
//		try {
//			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd h:mm:ss");			
//			int UUID = process.getUUID();
//			for(int i=0;i<UUID;i++){
//				clientSocket = new Socket("127.0.0.1",Process.portList.get(i));
//				outToServer = new ObjectOutputStream(clientSocket.getOutputStream());	
//				outToServer.writeObject(String.valueOf(UUID));
//				outToServer.writeObject(String.valueOf(UUID)+" is the leader");
//				process.getGUI().appead("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Result "+UUID+" to "+i);
//				System.out.println("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Result "+UUID+" to "+ Process.UUIDList.get(i));
//				outToServer.close();
//		        clientSocket.close();
//			}				
//			
//	        
//	       
//		} catch (IOException e) {
//			// TODO Auto-generated catch block			
//			System.out.println("sendResult exception");
//		}
//	}
//	
//	public void sendOK(String sendUUID){
//		Socket clientSocket = null;
//		ObjectOutputStream outToServer = null;
//		
//		try {
//						
//			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd h:mm:ss");
//			//System.out.println(UUIDList.size());
//			int UUID = process.getUUID();
//			System.out.println(Integer.valueOf(sendUUID));
//			clientSocket = new Socket("127.0.0.1",Process.portList.get(Integer.valueOf(sendUUID)));
//			outToServer = new ObjectOutputStream(clientSocket.getOutputStream());	
//			outToServer.writeObject(String.valueOf(UUID));
//			outToServer.writeObject("OKAY");
//			System.out.println("send ok");
//			process.getGUI().appead("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send OKAY "+UUID+" to "+ sendUUID);
//							
//	        outToServer.close();
//	        clientSocket.close();
//	       
//		} catch (IOException e) {
//			// TODO Auto-generated catch block			
//			System.out.println("SendOK exception");
//			System.err.println(e);
//		}
//	}
//	
//	
//	private void cleanup() {
//		// TODO Auto-generated method stub
//		
//	}
//}
