package ICSI412;

import java.util.Timer;
import java.util.TimerTask;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.HashMap;
import java.time.Clock;

public class Scheduler{
	
	private Timer timer;
	
	private PCB currentProcess;

	private Kernel kernel;

	private LinkedList<PCB> realtimeProcesses;
	private LinkedList<PCB> interactiveProcesses;
	private LinkedList<PCB> backroundProcesses;
	private PriorityQueue<PCB> sleepProcesses;

	private Clock sysClock;
	private Random prob = new Random();

	private HashMap<Integer, PCB> pidToPCB;
	private HashMap<Integer, PCB> waitingPCB;

	public int CreateProcess(UserlandProcess up){
		PCB thisProcess = new PCB(up, OS.Priority.Interactive);
		//add the new process to the processList
		interactiveProcesses.addLast(thisProcess);
		
		pidToPCB.put(thisProcess.PID, thisProcess);
		//if there is no process currenlty running, call switchProcess
		if(currentProcess == null){
			SwitchProcess();
		}
		//return this processes PID
		return thisProcess.PID;
	}

	public int CreateProcess(UserlandProcess up, OS.Priority p){
		PCB thisProcess = new PCB(up, p);
		//add the new process to its process list
		switch(p){
			case RealTime:
				realtimeProcesses.addLast(thisProcess);
			break;
			case Interactive:
				interactiveProcesses.addLast(thisProcess);
			break;
			case Backround:
				backroundProcesses.addLast(thisProcess);
			break;
		}

		pidToPCB.put(thisProcess.PID, thisProcess);
		//if there are no processes running, call SwitchProcess
		if(currentProcess == null){
			SwitchProcess();
		}
		return thisProcess.PID;
	}

	public void SwitchProcess(){
		//if there is currently a process running and it is not done...
		if(currentProcess != null && !currentProcess.isDone()){
			//add it to the back of the list
			switch(currentProcess.priority){
				case RealTime:
					realtimeProcesses.addLast(currentProcess);
				break;
				case Interactive:
					interactiveProcesses.addLast(currentProcess);
				break;
				case Backround:
					backroundProcesses.addLast(currentProcess);
				break;
			}
		}
		//wake processes
		wakeProcesses();
		//pick a new process
		pickProcess();	
	}

	private void wakeProcesses(){
		//if the sleep list is not empty...
		if(!sleepProcesses.isEmpty()){
			while(true){
				if(sleepProcesses.isEmpty()){
					break;
				}
				//grab the first process off the sleep list
				PCB wake = sleepProcesses.poll();
				//if that processes sleep period has expired
				if(wake.sleepTime <= sysClock.millis()){
					System.out.println("Process: " + wake.getPname() + " has been woken up");
					wake.sleepTime = 0;
					//add it back to its process list
					switch(wake.priority){
						case RealTime:
							realtimeProcesses.addLast(wake);
						break;
						case Interactive:
							interactiveProcesses.addLast(wake);
						break;
						case Backround:
							backroundProcesses.addLast(wake);
						break;
					}
				}
				//if the process is not ready to be woken up
				else{
					//add it back to the sleep list and exit
					sleepProcesses.add(wake);
					break;
				}
			}
		}
	}

	private void pickProcess(){
		//grab the new current process from the front of the list
		int chance = prob.nextInt(10);
		//if there are realtime processes
		if(!realtimeProcesses.isEmpty()){
			//if the random number is 0 (1/10)
			if(chance == 0 && !backroundProcesses.isEmpty()){
				//pick a backround process
				currentProcess = backroundProcesses.removeFirst();
			}
			//if the random number is 1 - 3 (3/10) 
			else if((chance > 0 && chance < 4) && !interactiveProcesses.isEmpty()){
				//pick an interactive process
				currentProcess = interactiveProcesses.removeFirst();
			}
			//if the random number is 4 - 9 (6/10)
			else{
				//pick a realtime process
				currentProcess = realtimeProcesses.removeFirst();
			}
		}
		//if there are no realtime processes but there are interactive processes
		else if(!interactiveProcesses.isEmpty()){
			//if the random number is 0 - 2 
			if(chance < 3 && !backroundProcesses.isEmpty()){
				//pick a backround process
				currentProcess = backroundProcesses.removeFirst();
			}
			//if the random number is 3 - 9
			else{
				//pick an interactive process
				currentProcess = interactiveProcesses.removeFirst();
			}
		}
		//if there are no interactive processes but there are backround processes
		else if(!backroundProcesses.isEmpty()){
			//pick a backround process
			currentProcess = backroundProcesses.removeFirst();
		}
	}

	public void Sleep(int milliseconds){
		//calculate what time the process will wake up
		currentProcess.sleepTime = sysClock.millis() + milliseconds;
		//add the current process to the sleep list
		sleepProcesses.add(currentProcess);
		//pick new process to run
		pickProcess();
	}

	public PCB getCurrentlyRunning(){
		return currentProcess;
	}

	public void Exit(){
		pidToPCB.remove(currentProcess.PID);
		//set the current processes exit flag
		currentProcess.exit();
		System.out.println("freeing devices on " + currentProcess.getPname());
		//free the current processes devices
		kernel.freeDevices();
		//switch processes
		SwitchProcess();
	}

	//KERNEL MESSAGE ASSIGNMENT!!!!---------------------------------------------------------------
	
	public int GetPID(){
		//grabs the currently running PCB's PID
		return currentProcess.PID;
	}

	public int GetPIDByName(String pname){
		//search every alive process and attempt to match
		//its name to pname
		for(PCB prc : pidToPCB.values()){
			if(pname.equals(prc.getPname())){
				return prc.PID;
			}
		}
		//if there is no alive process with a name that matches pname,
		//return -1
		return -1;
	}

	public PCB pidFetch(int pid){
		//check the pidToPCB hashmap for a pcb with the same pid as "pid" 
		return pidToPCB.containsKey(pid) ? pidToPCB.get(pid) : null;
	}

	public void waitForMSG(){
		//put the currently running process in the message wait queue
		waitingPCB.put(currentProcess.PID, currentProcess);	
		//pick a new process to run
		pickProcess();
	}

	public void msgWake(int pid){
		//fetch the PCB that needs to be woken up 
		PCB waitingProcess = waitingPCB.get(pid);
		//place the PCB back into its runnable queue
		if(waitingProcess != null){
			switch(waitingProcess.priority){
				case RealTime:
					realtimeProcesses.addLast(waitingProcess);
				break;
				case Interactive:
					interactiveProcesses.addLast(waitingProcess);
				break;
				case Backround:
				backroundProcesses.addLast(waitingProcess);
				break;
			}
			//remove the PCB from the wait queue
			waitingPCB.remove(pid);
		}
		else{
			throw new RuntimeException("attempting to wake a process that is not waiting for a msg");
		}
		
	}

	public boolean PCBisWaiting(int pid){
		return waitingPCB.containsKey(pid);
	}
	
	public Scheduler(Kernel k){
		kernel = k;
		realtimeProcesses = new LinkedList<PCB>();
		interactiveProcesses = new LinkedList<PCB>();
		backroundProcesses = new LinkedList<PCB>();
		//construct a new priority queue with a comparator object that sorts the PCB's by their sleep time
		sleepProcesses = new PriorityQueue<PCB>((PCB1, PCB2) -> Long.compare(PCB1.sleepTime, PCB2.sleepTime));
		pidToPCB = new HashMap<>();
		waitingPCB = new HashMap<>();
		timer = new Timer();
		sysClock = Clock.systemDefaultZone();
		//timer task which calls requestStop on the current process
		TimerTask interrupt = new TimerTask(){
			public void run(){
				if(currentProcess != null) {
					currentProcess.requestStop();
				}
			}
		};
		//interrupt is scheduled to be executed every 250 ms
		timer.scheduleAtFixedRate(interrupt, 0, 250);
	}


	//collects all processes with valid pages for swapping
	//and returns one at random to the kernel
	public PCB getRandomProcess(){
		LinkedList<PCB> stealList = new LinkedList<>();	
		//collect all processes that have pages into a list...
		for(PCB process : pidToPCB.values()){
			if(process.hasPage()){
				stealList.add(process);
			}
		}
		//use a random integer between 0 - list size [not inclusive]
		//to select a random process from the list
		Random rand = new Random();	
		return stealList.get(rand.nextInt(stealList.size()));
	}

}
