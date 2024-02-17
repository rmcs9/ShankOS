package ICSI412;

import java.util.concurrent.Semaphore;

public class Kernel implements Runnable{
	
	private Scheduler scheduler;

	private Thread thread;

	private Semaphore semaphore;

	public void start(){
		semaphore.release();	
	}

	public void run(){
		while(true){
			//acquire the semaphore or wait for kernel to be started
			try{
				semaphore.acquire();
			}
			catch(InterruptedException e){
				throw new RuntimeException(e);
			}
			//switch on OS current call
			//call cooresponding method
			switch(OS.currentCall){
				case CreateProcess:
					//set OS.returnVal to the PID of the created process
					OS.returnVal = scheduler.CreateProcess((UserlandProcess)OS.params.get(0));
				break;
				case SwitchProcess:
					scheduler.SwitchProcess();
				break;
				case Sleep:
					scheduler.Sleep((int) OS.params.get(0));
				break;
				case CreatePriorityProcess:
					scheduler.CreateProcess((UserlandProcess)OS.params.get(0), (OS.Priority)OS.params.get(1));
			}
			//start the new current process
			scheduler.currentProcess.start();
		}
	}

	public Scheduler getScheduler(){
		return scheduler;
	}

	public Kernel(){
		thread = new Thread(this, "KERNAL");
		semaphore = new Semaphore(0);
		scheduler = new Scheduler();
		thread.start();
	}
}
