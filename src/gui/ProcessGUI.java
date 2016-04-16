package gui;

import java.awt.BorderLayout;
import java.security.KeyStore.PrivateKeyEntry;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ProcessGUI extends JFrame{
	private static int DEFAULT_FRAME_WIDTH = 500;
	private static int DEFAULT_FRAME_HEIGHT = 300;
	private JScrollPane jScrollPane ;	
	private JTextArea record;
	private JLabel label;
	
	public ProcessGUI(String name){
		label = new JLabel("Information");
		
		record = new JTextArea();
		jScrollPane = new JScrollPane(record);       
		
        setTitle(name);
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE ); 
        setSize( DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT ); // set frame size 
        setVisible( true ); // display frame 
        
        add(label,BorderLayout.NORTH);
        add(jScrollPane,BorderLayout.CENTER);
	}
	
	public void appead (String str) {
		record.append(str+"\n");
	}
}
