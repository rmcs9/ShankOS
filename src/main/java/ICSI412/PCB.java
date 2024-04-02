package ICSI412;

import java.util.LinkedList;

public class PCB{
	
	private UserlandProcess up;
	
	public long sleepTime = 0;

	public OS.Priority priority;

	private static int nextPID = 0;

	public int timeouts = 0;
	
	public int PID;

	public int[] fileDescriptors;

	public LinkedList<KernelMessage> msgQueue;
	
	public PCB(UserlandProcess u, OS.Priority p){
		PID = ++nextPID;
		up = u;
		priority = p;
		sleepTime = 0;
		fileDescriptors = new int[10];
		for(int i = 0; i < 10; i++){
			fileDescriptors[i] = -1;
		}
		msgQueue = new LinkedList<>();
	}

	public void start(){
		up.start();
	}

	public void requestStop(){
		up.requestStop();
		//increment the timeout counter
		timeouts++;
		//if the process has timed out more than 5 times
		if(timeouts >= 5){
			//reset the timeout counter
			timeouts = 0;
			//if the priority is not backround...
			if(priority != OS.Priority.Backround){
				//demote the process
				demote();
			}
		}
	}

	private void demote(){
		//if the process is realtime...
		if(priority == OS.Priority.RealTime){
			//demote to interactive
			priority = OS.Priority.Interactive;
			System.out.println("DEMOTING PROCESS: " + up.pname + " --> interactive"); 
		}
		//if the process is interactive
		else{
			//demote to backround
			priority = OS.Priority.Backround;
			System.out.println("DEMOTING PROCESS: " + up.pname + " --> backround"); 
		}
	}

	public void stop(){
		try{
			up.stop();
			while(!up.isStopped()){
				Thread.sleep(10);
			}
		}
		catch(InterruptedException e){
			throw new RuntimeException(e);
		}
	}

	public void cooperate(){
		up.cooperate();
	}

	public String getPname(){
		return up.pname;
	}

	public boolean isDone(){
		return up.exited || up.isDone();
	}

	public void exit(){
		up.exited = true;
	}
}
