package ICSI412;

import java.util.concurrent.Semaphore;

public abstract class UserlandProcess implements Runnable{

	private static byte[] physicalMemory = new byte[1024 * 1024];
	public static int[][] TLB = new int[2][2];

	//ONLY HERE FOR THE PURPOSE OF TESTING
	public String pname;

	private Thread thread;

	private Semaphore semaphore;

	private boolean quantumExp;

	public boolean exited;
	
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

	public byte Read(int addr){
		//calculate vir page and offset
		int virPage = addr / 1024;
		int offset = addr % 1024;
		int physPage;
		//check the 2 TLB slots to see if the
		//page is cached
		if(virPage == TLB[0][0]){
			physPage = TLB[0][1];	
		}
		else if(virPage == TLB[1][0]){
			physPage = TLB[0][1];
		}
		//if the page is not found in the TLB
		else{
			//call getMapping to request the page needed
			OS.GetMapping(virPage);		
			//return a new Read call, which will check the TLB again
			return Read(addr);
		}
		//if the virtual page is not mapped,
		//kill the process
		if(physPage == -1){
			OS.Exit();
		}
		//calculate the physical address	
		int physAddr = physPage * 1024 + offset;
		//return the data located at the physical address	
		return physicalMemory[physAddr];
	}

	public void Write(int addr, byte val){
		//calculate the vir page and the offset
		int virPage = addr / 1024;
		int offset = addr % 1024;
		int physPage;
		//check the TLB for the requested page
		if(virPage == TLB[0][0]){
			physPage = TLB[0][1];
		}
		else if(virPage == TLB[1][0]){
			physPage = TLB[1][1];
		} 
		//if the page is not present in the TLB...
		else{
			//request the mapping and call Write again
			OS.GetMapping(virPage);
			Write(addr, val);
			return;
		}
		//if the mapping does not exist...
		if(physPage == -1){
			//kill the process
			OS.Exit();
		}
		//calculate the physical address and store the byte value
		//at the address
		int physAddr = physPage * 1024 + offset;
		physicalMemory[physAddr] = val;
	}

	public UserlandProcess(String processName){
		pname = processName;
		thread = new Thread(this, processName);
		semaphore = new Semaphore(0);
		exited = false;
		thread.start();
	}

}
