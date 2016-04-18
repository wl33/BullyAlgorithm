package model;

import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

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
	private boolean isOmission;
	private int timeout;
	private boolean[] timeoutStatus;
 	private FileWriter fw;
 	private String logMessage;
 	private BufferedWriter bw;
    private PrintWriter out;
    private boolean closeByMainGUI;
    private boolean closeBySelfGUI;
	
	public Process(int port, int UUID,ProcessGUI GUI, List<Integer> portList, List<Integer> UUIDList){
		this.port = port;
		this.UUID = UUID;		
		this.GUI = GUI;
		this.portList = portList;
		this.UUIDList = UUIDList;
		crashStatus = new boolean[portList.size()];
		timeoutStatus = new boolean[portList.size()];
		receiveOK = new boolean[portList.size()];
		leader=-1;
		crashProssibility=-1;
		timeoutProssibility=-1;
		formatter = new SimpleDateFormat("yyyy-MM-dd h:mm:ss");//[2016-03-22 12:14:01 | 1 ]
		setFileWriter();
		
	}

	public void run(){
		try {
			
			ss = new ServerSocket(this.port);
			
			System.out.println("Process"+ UUID +" is running.");
			GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Status: Start Running");
			logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Status: Start Running";
			log(logMessage);
			
			if (!isCrash && !isTimeOut && !isOmission) {
				detectLeaderCrash();
			}
					
			while(true){
				if (isCrash) { //
					GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Random Error: Crash Error Occur");
					logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Error: Crash Error";
					log(logMessage);
					close();
				    break;
				}		
				
				Socket s = ss.accept();	
							
				handleConnection(s);
			}
			closeLog();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			if(!isCrash){
				GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Status: Close Server");
				logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Status: Close Server";
				log(logMessage);
				
				closeLog();	
			}
			System.out.println(UUID+" log out");
		} 
	}

	private void setRandomOccurError() {
		// TODO Auto-generated method stub
		if(isCrash){
			Thread t1 = new Thread(new Runnable() {
				public void run() {
					while(true){
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
			t1.start();
			return;
		}
		if (isTimeOut) {
			
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
	        	
				if (message.equals("Elect")) {
					try {
						Thread.sleep(timeout+(int)(Math.random()*10)+20);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					sendOK(senderUUID);
				}
				inFromClient.close();
				is.close();
				conn.close();
	        	return;
			}
	        
	        if(isOmission){
	        	GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Internal: Received "+message+" from "+ senderUUID);
	        	logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Internal: Received "+message+" from "+ senderUUID;
    	    	log(logMessage);
    	    	if(message.equals("Elect")){
    	    		GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Omission Error: Not Reply");
    	        	logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Omission Error: Not Reply";
        	    	log(logMessage);
    	    	}
	        	inFromClient.close();
				is.close();
				conn.close();
	        	return;
	        }
	        
	        if(message.equals("Detect")){
	        	inFromClient.close();
				is.close();
				conn.close();
	        	return;
	        }
	                
	        if (message.equals("OKAY")) {
	        	if (timeoutStatus[Integer.valueOf(senderUUID)]) {
	        		GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Timeout: Received Timeout OKAY from "+ senderUUID);   
	        		logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Timeout: Received Timeout OKAY from "+ senderUUID;
	    			log(logMessage);
	        	}else {
	        		receiveOK[Integer.valueOf(senderUUID)]=true;
	        		GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Internal: Received OKAY from "+ senderUUID);
	        		logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Internal: Received OKAY from "+ senderUUID;
	    	    	log(logMessage);
	        	}
			} else if (message.equals("Elect")) {	//election business logic							
				sendOK(senderUUID);
				if(UUID != UUIDList.get(UUIDList.size()-1)){//not the process having biggest UUID
					//send elect messages to those processes having bigger UUID
//					if (leader!=-1) {//already know the leader, thus do not need to elect.
//						GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: Already known the leader is "+leader+". No need to trigger an election");
//						logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: Already known the leader is "+leader+". No need to trigger an election";
//		    	    	log(logMessage);
//					}else{
//						GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Internal: Trigger an election");
//						logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Internal: Trigger an election";
//		    	    	log(logMessage);
//						elect();
//					}
					
					GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Internal: Trigger an election");
					logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Internal: Trigger an election";
	    	    	log(logMessage);
					elect();
				}
				else{//this process is the one having biggest UUID, thus ought to send Result					
					sendResult();						
				}
			}else {//result message
				leader = Integer.valueOf(senderUUID);
				GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Internal: Received Result from "+ senderUUID);
				logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Internal: Received Result from "+ senderUUID;
    	    	log(logMessage);
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
	
	private void detectLeaderCrash() {
		// TODO Auto-generated method stub
		
		Thread thread = new Thread(new Runnable() {
			public void run() {
				while(true){
					while(true){
						try {
							Thread.sleep(1000);
							if(leader!=-1) break;
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					//already have a leader;
					while(true){
						try {
							Thread.sleep(1000+(int)(Math.random()*3000));
							if (isCrash) {
								break;
							}
							if(!sendDetectiveMessage()){
								break;			
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			    }
				
			}
		});
		thread.start();
		
	}
	
	
	private boolean sendDetectiveMessage() {
		Socket clientSocket = null;
		ObjectOutputStream outToServer = null;
		try {	
			//System.out.println(UUIDList.size());
			clientSocket = new Socket("127.0.0.1",portList.get(Integer.valueOf(leader)));
			outToServer = new ObjectOutputStream(clientSocket.getOutputStream());	
			outToServer.writeObject(String.valueOf(UUID));
			outToServer.writeObject("Detect");			
	        outToServer.close();
	        clientSocket.close();
	       return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block			
			System.out.println(UUID+" find leader "+leader+" crash, thus invoke election");
			logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Find leader ("+ leader+") crash";
			GUI.appead(logMessage);		
	    	log(logMessage);
			
			crashStatus[Integer.valueOf(leader)] = true;
			leader=-1;
			elect();
			return false;
		}
	}
	
	
	public void elect() {
		Socket clientSocket = null;
		ObjectOutputStream outToServer = null;
					
			//System.out.println(UUIDList.size());
			for(int i=UUID+1;i<UUIDList.size();i++){
			   try {
				GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Elect "+UUID+" to "+ UUIDList.get(i));
				logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Elect "+UUID+" to "+ UUIDList.get(i);
	    	    log(logMessage);   
				   
				clientSocket = new Socket("127.0.0.1",portList.get(i));
				outToServer = new ObjectOutputStream(clientSocket.getOutputStream());	
				outToServer.writeObject(String.valueOf(UUID));
				outToServer.writeObject("Elect");
				
				//System.out.println("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Elect "+UUID+" to "+ Process.UUIDList.get(i));
				outToServer.close();
		        clientSocket.close();
		        }catch (IOException e) {
					// TODO Auto-generated catch block			
					System.out.println("elect exception");
					GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Election Error: Crash Error "+ UUIDList.get(i));
					logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Election Error: Crash Error "+ UUIDList.get(i);
	    	    	log(logMessage);
					
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
						Thread.sleep(timeout);
						for(int i=UUID+1;i<UUIDList.size();i++){
							if (receiveOK[i] == false) {
								System.out.println("Process "+ i + "timeout");
								GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Election Error: "+ "Fail to receive OKAY from "+UUIDList.get(i) + " within timeout ("+timeout+"ms)");
								logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Election Error: "+ "Fail to receive OKAY from "+UUIDList.get(i) + " within timeout("+timeout+"ms)";
				    	    	log(logMessage);
								
								timeoutStatus[i] = true;
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
				outToServer.writeObject("Result");
				
				GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Result "+UUID+" to "+i);
				logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Result "+UUID+" to "+i;
    	    	log(logMessage);
				
				System.out.println("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send Result "+UUID+" to "+ UUIDList.get(i));
				outToServer.close();
		        clientSocket.close();
		        }catch (IOException e) {
					// TODO Auto-generated catch block			
					System.out.println("sendResult exception");
					GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] SendResult Error: Crash Error "+ UUIDList.get(i));
					logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] SendResult Error: Crash Error "+ UUIDList.get(i);
	    	    	log(logMessage);
					
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
			if(isTimeOut){
				GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Timeout On Purpose: send delayed OKAY "+UUID+" to "+ sendUUID);
				logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Timeout On Purpose: send delayed OKAY "+UUID+" to "+ sendUUID;
    	    	log(logMessage);
			}
			else {
				GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send OKAY "+UUID+" to "+ sendUUID);
				logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] Algorithm: send OKAY "+UUID+" to "+ sendUUID;
    	    	log(logMessage);
			}			
	        outToServer.close();
	        clientSocket.close();
	       
		} catch (IOException e) {
			// TODO Auto-generated catch block			
			System.out.println("SendOK exception");
			GUI.appead("["+formatter.format(new Date())+" | "+ UUID + " ] SendOK Error: Crash Error "+ sendUUID);
			logMessage = "["+formatter.format(new Date())+" | "+ UUID + " ] SendOK Error: Crash Error "+ sendUUID;
	    	log(logMessage);
			
			crashStatus[Integer.valueOf(sendUUID)] = true;
			System.out.println(e);
		}
	}
	
	public void close(){
		try {
			ss.close();
			if(!closeBySelfGUI)
			     GUI.dispatchEvent(new WindowEvent(GUI, WindowEvent.WINDOW_CLOSING));
			System.out.println("");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("force to kill socket");
		}
	}
	
	private void closeLog() {
		// TODO Auto-generated method stub
		try {
			out.close();
			bw.close();
			fw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void setFileWriter() {
		// TODO Auto-generated method stub
		File file = new File("logs\\process"+UUID+".txt");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("fail to creat log");
			}
		}
		try {
			fw = new FileWriter("logs\\process"+UUID+".txt", true);
			bw = new BufferedWriter(fw);
		    out = new PrintWriter(bw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("fail to creat filewrite");
			e.printStackTrace();
		}	
		
	    
	}
	
	private void log(String message2) {
		if(message2.contains("Status"))				
			out.append("*********************"+ message2+"*********************\n");
		else
			out.append(message2+"\n");
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

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean isOmission() {
		return isOmission;
	}

	public void setOmission(boolean isOmission) {
		this.isOmission = isOmission;
	}

	public boolean isCloseByMainGUI() {
		return closeByMainGUI;
	}

	public void setCloseByMainGUI(boolean closeByMainGUI) {
		this.closeByMainGUI = closeByMainGUI;
	}

	public boolean isCloseBySelfGUI() {
		return closeBySelfGUI;
	}

	public void setCloseBySelfGUI(boolean closeBySelfGUI) {
		this.closeBySelfGUI = closeBySelfGUI;
	}

    
	
	
	
	
}
