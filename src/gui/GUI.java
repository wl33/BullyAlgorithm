package gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Menu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import model.Process;





public class GUI extends JFrame implements ActionListener{
	private static int DEFAULT_FRAME_WIDTH = 650;
	private static int DEFAULT_FRAME_HEIGHT = 300;
	private JPanel panel;
	private JTextArea numberOfProcess;
	private JTextArea timeout;
	private JTextArea startProcess;
	private JTextArea crashProcesses;
	private JTextArea TimeoutProcesses;
	private JTextArea OmissionProcesses;
	private JButton runButton;
	private List<model.Process> processes;
	private JMenuBar jMenuBar;
	
	public GUI(){
		processes = new ArrayList<model.Process>();
		init();
		
        setTitle("Bully Algorithm");
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ); 
        setSize( DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT ); // set frame size
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible( true ); // display frame
        
        
        addWindowListener(new WindowAdapter() {
        	@Override
        	public void windowClosing(WindowEvent e) {
        		// TODO Auto-generated method stub
        		for(Process p : processes){
        			p.close();
        		}
        		super.windowClosing(e);
        	}
		});         
	}

	private void init(){
		numberOfProcess = new JTextArea();
		timeout = new JTextArea();
		startProcess = new JTextArea();
		crashProcesses = new JTextArea();
		TimeoutProcesses = new JTextArea();
		OmissionProcesses = new JTextArea();
		jMenuBar = new JMenuBar();
		setMenuBar();
		
		runButton = new JButton("Start an Election!");
		runButton.addActionListener(this);
		
		panel = new JPanel(new GridLayout(6, 6));       
		JLabel label1 = new JLabel("Input the number of processes:");
		JLabel label2 = new JLabel("Input timeout(ms) (default:100ms):");
		JLabel label3 = new JLabel("Input a process starting an election");
		JLabel label4 = new JLabel("Part2: Crash Processes(Process starts from 0): ");
		JLabel label5 = new JLabel("Part2: Timeout Processes(Process starts from 0): ");
		JLabel label6 = new JLabel("Part2: Omission Processes(Process starts from 0): ");
		
		panel.add(label1);
		panel.add(numberOfProcess);
		panel.add(label2);
		panel.add(timeout);
		panel.add(label3);
		panel.add(startProcess);
		panel.add(label4);
		panel.add(crashProcesses);
		panel.add(label5);
		panel.add(TimeoutProcesses);
		panel.add(label6);
		panel.add(OmissionProcesses);
		
		//panel.add(runButton);
		add(jMenuBar, BorderLayout.NORTH);
		add(panel,BorderLayout.CENTER);
		add(runButton, BorderLayout.SOUTH);
		
		
	}

	private void setMenuBar() {
		// TODO Auto-generated method stub
		JMenu menu1 = new JMenu("Menu");
		
		JMenuItem load  = new JMenuItem("load");
		load.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JFileChooser jFileChooser = new JFileChooser();						
                int i = jFileChooser.showOpenDialog(null);
                if(i== jFileChooser.APPROVE_OPTION){ //open file
                    String path = jFileChooser.getSelectedFile().getAbsolutePath();
                    String name = jFileChooser.getSelectedFile().getName();
//                    System.out.println(path);
//                    System.out.println(name);                                      	
                    try {
            			File file = new File(path);
            			
            			BufferedReader br = new BufferedReader(new FileReader(file));
            			String line="";
            		    
            		    for (int j = 0; j < 6; j++) {
            		    	line = br.readLine();
            		    	line = line.split(":")[1].trim();
            		        switch (j) {
							case 0:
								numberOfProcess.setText(line);
								break;
                            case 1:
								timeout.setText(line);
								break;
							case 2:
								startProcess.setText(line);
								break;
                            case 3:
                            	crashProcesses.setText(line);
                            	break;
                            case 4:
                            	TimeoutProcesses.setText(line);
                                break;
                            case 5:
                            	OmissionProcesses.setText(line);
                            	break;
							default:
								break;
							}
						}
            		    
            		    
            			br.close();
     
            		} catch (IOException ex) {
            			// TODO Auto-generated catch block
            			System.out.println("something wrong with the files");
            			ex.printStackTrace();
            		}
                    
                }else{//no select any file 
                	return;
                }
			}
			
		});
			
		menu1.add(load);
	    jMenuBar.add(menu1);
			
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		for(Process p : processes){
			p.close();
		}
		processes.clear();
		
		if (numberOfProcess.getText().trim().equals("") || timeout.getText().trim().equals("") || startProcess.getText().trim().equals("")) {
			System.err.println("input necessary information");
		}else{			
			
			for (int i = 0; i < Integer.valueOf(numberOfProcess.getText().trim()); i++) {
				
				
				ProcessGUI processGUI = new ProcessGUI("Process "+String.valueOf(i));
				
				List<Integer> portList = new ArrayList<Integer>();
				List<Integer> UUIDList = new ArrayList<Integer>();
				for (int k = 0; k < Integer.valueOf(numberOfProcess.getText().trim()); k++) {
					portList.add(8899+k);
					UUIDList.add(k);
				}
				
				final model.Process process = new model.Process(8899+i, i, processGUI,portList,UUIDList);
				process.setGUI(processGUI);				
						
				processes.add(process);			
			}
			
			if(!crashProcesses.getText().trim().equals("")){
				String[] crashP = crashProcesses.getText().trim().split(" ");
				for (int k = 0; k < crashP.length; k++) {
					processes.get(Integer.valueOf(crashP[k])).setCrash(true);;
				}
			}
			
			//set who is Timeout process
			if(!TimeoutProcesses.getText().trim().equals("")){
				String[] timeoutP = TimeoutProcesses.getText().trim().split(" ");
				for (int k = 0; k < timeoutP.length; k++) {
					processes.get(Integer.valueOf(timeoutP[k])).setTimeOut(true);
				}
			}
			
			//set who is Omission process
			if(!OmissionProcesses.getText().trim().equals("")){
				String[] omissionP = OmissionProcesses.getText().trim().split(" ");
				for (int k = 0; k < omissionP.length; k++) {
					processes.get(Integer.valueOf(omissionP[k])).setOmission(true);
				}
			}
			
			//set timeout
			int timeoutMinSecond = 0;
            if(timeout.getText().trim().equals("")){
            	//set default 50ms
            	timeoutMinSecond = 100;
            }else{
            	timeoutMinSecond = Integer.valueOf((timeout.getText().trim())); 
            }
            for (int i = 0; i < processes.size(); i++) {
				processes.get(i).setTimeout(timeoutMinSecond);
			}
            
			
			for (final Process process : processes) {
				Thread thread = new Thread(new Runnable() {				
					@Override
					public void run() {
						// TODO Auto-generated method stub
						process.run();
					}
				});
				thread.start();
			}
			
			//start election
		    model.Process stProcess = processes.get(Integer.valueOf(startProcess.getText()));
			stProcess.elect();
		
			
		}
		
	}
}
