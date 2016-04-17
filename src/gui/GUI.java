package gui;

import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import model.Process;



public class GUI extends JFrame implements ActionListener{
	private static int DEFAULT_FRAME_WIDTH = 500;
	private static int DEFAULT_FRAME_HEIGHT = 300;
	private JPanel panel;
	private JTextArea numberOfProcess;
	private JTextArea timeout;
	private JTextArea startProcess;
	private JTextArea crashProcesses;
	private JTextArea TimeoutProcesses;
	private JButton runButton;
	private List<model.Process> processes;
	
	
	public GUI(){
		processes = new ArrayList<model.Process>();
		init();
		
        setTitle("Bully Algorithm");
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ); 
        setSize( DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT ); // set frame size 
        setVisible( true ); // display frame 
                
	}

	private void init(){
		numberOfProcess = new JTextArea();
		timeout = new JTextArea();
		startProcess = new JTextArea();
		crashProcesses = new JTextArea();
		TimeoutProcesses = new JTextArea();
		
		runButton = new JButton("Start an Election!");
		runButton.addActionListener(this);
		
		panel = new JPanel(new GridLayout(6, 6));       
		JLabel label1 = new JLabel("Input the number of participants:");
		JLabel label2 = new JLabel("Input timeout:");
		JLabel label3 = new JLabel("Input participant starting an election");
		JLabel label4 = new JLabel("Part2: Crashed Processes(Process starts from 0): ");
		JLabel label5 = new JLabel("Part2: Timeout Processes(Process starts from 0): ");
		
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
		
		panel.add(runButton);
		add(panel);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
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
			
			//set Timeout process
			String[] timeoutP = TimeoutProcesses.getText().trim().split(" ");
			for (int k = 0; k < timeoutP.length; k++) {
				processes.get(Integer.valueOf(timeoutP[k])).setTimeOut(true);
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
