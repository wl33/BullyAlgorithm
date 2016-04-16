package gui;

import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import main.Main2;



public class GUI extends JFrame implements ActionListener{
	private static int DEFAULT_FRAME_WIDTH = 500;
	private static int DEFAULT_FRAME_HEIGHT = 300;
	private JPanel panel;
	private JTextArea numberOfProcess;
	private JTextArea timeout;
	private JTextArea startProcess;
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
		runButton = new JButton("Start an Election!");
		runButton.addActionListener(this);
		
		panel = new JPanel(new GridLayout(4, 4));       
		JLabel label1 = new JLabel("Input the number of participants:");
		JLabel label2 = new JLabel("Input timeout:");
		JLabel label3 = new JLabel("Input participant starting an election");
		
		panel.add(label1);
		panel.add(numberOfProcess);
		panel.add(label2);
		panel.add(timeout);
		panel.add(label3);
		panel.add(startProcess);
		panel.add(runButton);
		add(panel);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		model.Process.portList = null;
		model.Process.UUIDList = null;
		processes.clear();
		
		if (numberOfProcess.getText().trim().equals("") || timeout.getText().trim().equals("") || startProcess.getText().trim().equals("")) {
			System.err.println("input necessary information");
		}else{					
			for (int i = 0; i < Integer.valueOf(numberOfProcess.getText().trim()); i++) {
				ProcessGUI processGUI = new ProcessGUI(String.valueOf(i));
				final model.Process process = new model.Process(8888+i, i, processGUI);
				process.setGUI(processGUI);
				
				Thread thread = new Thread(new Runnable() {				
					@Override
					public void run() {
						// TODO Auto-generated method stub
						process.run();
					}
				});
				thread.start();
				
				processes.add(process);
				model.Process.portList.add(process.getPort());
				model.Process.UUIDList.add(process.getUUID());				
			}
			model.Process stProcess = processes.get(Integer.valueOf(startProcess.getText()));
			stProcess.elect();
			
		}
		
	}
}
