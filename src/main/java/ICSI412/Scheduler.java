package ICSI412;

import java.util.Timer;
import java.util.TimerTask;
import java.util.LinkedList;

public class Scheduler{
	
	private LinkedList<UserlandProcess> processes;

	private Timer timer;

	public UserlandProcess currentProcess;

	private static int pid = 0;
	
	public int CreateProcess(UserlandProcess up){
		//add the new process to the processList
		processes.addLast(up);
		//if there is no process currenlty running, call switchProcess
		if(currentProcess == null){
			SwitchProcess();
		}
		//return this processes PID
		return pid++;
	}

	public void SwitchProcess(){
		//if there is currently a process running and it is not done...
		if(currentProcess != null && !currentProcess.isDone()){
			//add it to the back of the list
			processes.addLast(currentProcess);
		}
		//grab the new current process from the front of the list
		currentProcess = processes.removeFirst();	
	}
	
	public Scheduler(){
		processes = new LinkedList<UserlandProcess>();
		timer = new Timer();
		//timer task which calls requestStop on the current process
		TimerTask interrupt = new TimerTask(){
			public void run(){
				if(currentProcess != null){
					currentProcess.requestStop();
				}
			}
		};
		//interrupt is scheduled to be executed every 250 ms
		timer.scheduleAtFixedRate(interrupt, 0, 250);
	}

}
