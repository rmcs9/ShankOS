package ICSI412;

import java.util.LinkedList;
import java.util.ArrayList;

public class PCB{
	
	private UserlandProcess up;
	
	public long sleepTime = 0;

	public OS.Priority priority;

	private static int nextPID = 0;

	public int timeouts = 0;
	
	public int PID;

	public int[] fileDescriptors;

	public LinkedList<KernelMessage> msgQueue;

	private int[] memMap;
	
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
		memMap = new int[100];
		for(int i = 0; i < 100; i++){
			memMap[i] = -1;
		}
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

	public int getMappedPage(int virPage){
		return memMap[virPage];
	}

	public int findBlock(int amount){
		//for every page of virtual memory for this process...
		for(int i = 0; i < 100; i++){
			//if the current page is free
			if(memMap[i] == -1){
				//save the start of the block
				int save = i;
				boolean fullBlock = true;
				//look ahead 'amount' pages and ensure that all of the requested pages are free
				for(int j = 1; j != amount; j++, i++){
					if(memMap[i] != -1){
						//if one of the pages in the requested block is in use,
						//set the flag and break
						fullBlock = false;
						break;
					}
				}
				//if the block was able to stay intact,
				//return the start of the block
				if(fullBlock){
					return save;
				}
			}
		}
		//return -1 for failure
		return -1;
	}

	public void mapMemory(int virtStart, int[] physAddrs){
		//given a virtual page start, and an array of physical pages to map to...
		//iterate trhough the virtual block from the start, and map each given
		//physical address to its cooresponding spot in the block
		for(int i = 0, j = virtStart; i < physAddrs.length; i++, j++){
			memMap[j] = physAddrs[i];	
		}
	}

	public int[] freeBlock(int startPage, int pages){
		//given a virtual page to start and an amount of pages to clear
		//unmap all the virtual pages and save what physical pages they were mapped to
		//for later removal
		int[] physAddrs = new int[pages];
		for(int i = startPage, j = 0; j < pages; i++, j++){
			physAddrs[j] = memMap[i];	
			memMap[i] = -1;
		}
		return physAddrs;
	}

	public ArrayList<Integer> clearMemory(){
		//find all the physical pages this process is currently holding
		//and return them for later removal
		ArrayList<Integer> ret = new ArrayList<>();
		for(int i = 0; i < 100; i++){
			if(memMap[i] != -1){
				ret.add(memMap[i]);	
			}
		}
		return ret;
	}
}
