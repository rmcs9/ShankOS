package ICSI412;

import java.util.Timer;
import java.util.TimerTask;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.time.Clock;
import java.util.Random;

public class Scheduler{
	
	private Timer timer;
	
	public PCB currentProcess;

	//NEW LISTS
	private LinkedList<PCB> realtimeProcesses;
	private LinkedList<PCB> interactiveProcesses;
	private LinkedList<PCB> backroundProcesses;
	private PriorityQueue<PCB> sleepProcesses;

	private Clock sysClock;
	private Random prob = new Random();

	public int CreateProcess(UserlandProcess up){
		PCB thisProcess = new PCB(up, OS.Priority.Interactive);
		//add the new process to the processList
		interactiveProcesses.addLast(thisProcess);
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
		try{
			Thread.sleep(10);
		}
		catch(InterruptedException e){
			throw new RuntimeException(e);
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
	
	public Scheduler(){
		realtimeProcesses = new LinkedList<PCB>();
		interactiveProcesses = new LinkedList<PCB>();
		backroundProcesses = new LinkedList<PCB>();
		//construct a new priority queue with a comparator object that sorts the PCB's by their sleep time
		sleepProcesses = new PriorityQueue<PCB>((PCB1, PCB2) -> Long.compare(PCB1.sleepTime, PCB2.sleepTime));
		timer = new Timer();
		sysClock = Clock.systemDefaultZone();
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
