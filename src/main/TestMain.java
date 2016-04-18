package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TestMain {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		
		final ServerSocket s = new ServerSocket(5555) ;
		Thread thread = new Thread(new Runnable() {
			public void run() {
			}
		});
		
		try {
			s.close();
			System.out.println("kkk");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("ddd");
			e.printStackTrace();
		}
		//System.out.println("ddd");
	    
	}

}
