package main;


import gui.ProcessGUI;

public class Main2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//System.out.println("hello world");
		for (int i = 0; i < 3; i++) {
			ProcessGUI processGUI = new ProcessGUI(String.valueOf(i));
			model.Process process = new model.Process(8888+i, i, processGUI);
			process.setGUI(processGUI);
			process.run();
			System.out.print("end of main");
			model.Process.portList.add(process.getPort());
			model.Process.UUIDList.add(process.getUUID());				
		}
		System.out.print("end of main");
	}

}
