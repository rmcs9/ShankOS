package ICSI412;

import java.util.concurrent.Semaphore;

public abstract class UserlandProcess implements Runnable{


	private Thread thread;

	private Semaphore semaphore;

	private boolean quantumExp;
	
	public void requestStop(){
		quantumExp = true;
	}

	public abstract void main();

	public boolean isStopped(){
		return semaphore.availablePermits() == 0;
	}

	public boolean isDone(){
		return !thread.isAlive();
	}

	public void start(){
		semaphore.release();
	}

	public void stop() throws InterruptedException{
		semaphore.acquire();
	}

	public void run(){
		//acquire semaphore
		try{
			semaphore.acquire();
		}
		catch(InterruptedException e){
			throw new RuntimeException(e);
		}
		//call UserlandProcess main()
		main();
	}

	public void cooperate(){
		if(quantumExp){
			quantumExp = false;
			OS.SwitchProcess();
		}
	}

	public UserlandProcess(String processName){
		thread = new Thread(this, processName);
		semaphore = new Semaphore(0);
		thread.start();
	}

}
