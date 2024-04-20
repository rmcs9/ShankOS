package ICSI412;

import java.util.concurrent.Semaphore;
import java.util.Random;
import java.util.ArrayList;

public class Kernel implements Runnable, Device{
	
	private Scheduler scheduler;

	private Thread thread;

	private Semaphore semaphore;

	private VFS vfs;

	private boolean[] freeSpace;

	private FakeFileSystem swapFile;	

	private int pageNumber;
	private int swapFileID;

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
					resetTLB();
					scheduler.SwitchProcess();
				break;
				case Sleep:
					scheduler.Sleep((int) OS.params.get(0));
				break;
				case CreatePriorityProcess:
					scheduler.CreateProcess((UserlandProcess)OS.params.get(0), (OS.Priority)OS.params.get(1));
				break;
				case Open:
					OS.returnVal = Open((String) OS.params.get(0));
				break;
				case Close:
					Close((int) OS.params.get(0));
				break;
				case Read:
					OS.returnVal = Read((int) OS.params.get(0), (int) OS.params.get(1));
				break;
				case Write:
					OS.returnVal = Write((int) OS.params.get(0), (byte[]) OS.params.get(1));
				break;
				case Seek:
					Seek((int) OS.params.get(0), (int) OS.params.get(1));
				break;
				case Exit:
					resetTLB();
					ExitFreeMemory();
					scheduler.Exit();	
				break;
				case GetPID:
					OS.returnVal = scheduler.GetPID();	
				break;
				case GetPIDByName:
					OS.returnVal = scheduler.GetPIDByName((String) OS.params.get(0));	
				break;
				case SendMessage:
					SendMessage((KernelMessage) OS.params.get(0));
				break;
				case WaitForMessage:
					OS.returnVal = WaitForMessage();
				break;
				case FetchMessage:
					OS.returnVal = FetchMessage();
				break;
				case GetMapping:
					GetMapping((int) OS.params.get(0));
				break;
				case AllocateMemory:
					OS.returnVal = AllocateMemory((int) OS.params.get(0));
				break;
				case FreeMemory:
					OS.returnVal = FreeMemory((int) OS.params.get(0), (int) OS.params.get(1));
				break;
			}
			//start the new current process
			System.out.println("currently running: " + scheduler.getCurrentlyRunning().getPname());
			scheduler.getCurrentlyRunning().start();
		}
	}

	public Scheduler getScheduler(){
		return scheduler;
	}

	public Kernel(){
		thread = new Thread(this, "KERNEL");
		semaphore = new Semaphore(0);
		scheduler = new Scheduler(this);
		vfs = new VFS();
		freeSpace = new boolean[1024];
		swapFile = new FakeFileSystem();
		swapFileID = swapFile.Open("swapfile.txt");
		pageNumber = 0;
		thread.start();
	}

//--------------------- DEVICES -------------------------------------

	public void freeDevices(){
		PCB crp = scheduler.getCurrentlyRunning();
		//iterate through all of this processes devices
		for(int i = 0; i < crp.fileDescriptors.length; i++){
			//if there a device open at this index
			if(crp.fileDescriptors[i] != -1){
				//close the device
				vfs.Close(crp.fileDescriptors[i]);
				//null out the fileDescriptor entry
				crp.fileDescriptors[i] = -1;
			}
		}
	}

	public int Open(String s){
		PCB crp = scheduler.getCurrentlyRunning();
		//find a file descriptor thats not in use
		for(int i = 0; i < crp.fileDescriptors.length; i++){
			if(crp.fileDescriptors[i] == -1){
				//call vfs open to obtain the vfsID
				int vfsID = vfs.Open(s); 
				//upon failure, return -1
				if(vfsID == -1){
					return -1;
				}
				//on success... set the file descriptor -> vfsID in PCB array
				crp.fileDescriptors[i] = vfsID;
				//return the file descriptor
				return i;
			}
		}
		return -1;
	}

	public void Close(int id){
		PCB crp = scheduler.getCurrentlyRunning();
		//call vfs close on the vfs ID associated with the File descriptor
		vfs.Close(crp.fileDescriptors[id]);
		//reset the file descriptor for further use
		crp.fileDescriptors[id] = -1;
	}

	public byte[] Read(int id, int size){
		//call vfs read on the vfsID associated with the provided file descriptor
		return vfs.Read(scheduler.getCurrentlyRunning().fileDescriptors[id], size);
	}

	public int Write(int id, byte[] data){
		//call vfs write on the vfsID associated with the provided file descriptor
		return vfs.Write(scheduler.getCurrentlyRunning().fileDescriptors[id], data);
	}

	public void Seek(int id, int size){
		//call vfs seek on the vfsID assoiated with the provided file descriptor
		vfs.Seek(scheduler.getCurrentlyRunning().fileDescriptors[id], size);
	}


//KERNEL MESSAGE ASSIGNMENT!!!!-----------------------------------------------------------
	

	private void SendMessage(KernelMessage km){
		//copy the incoming message
		KernelMessage copy = new KernelMessage(km);
		//find the senders PID
		copy.senderPID = scheduler.GetPID();
		//find the targets PCB
		PCB target = scheduler.pidFetch(copy.targetPID);
		//if the target exists, 
		if(target != null){
			//add the message to its queue
			target.msgQueue.add(copy);
			//if the target is dequeued and waiting for a message...
			if(scheduler.PCBisWaiting(target.PID)){
				//wake up the target
				scheduler.msgWake(target.PID);
			}
		}
	}

	private KernelMessage WaitForMessage(){
		//if the current process has a message waiting
		if(!scheduler.getCurrentlyRunning().msgQueue.isEmpty()){
			//return the waiting message
			return scheduler.getCurrentlyRunning().msgQueue.remove();	
		}
		//if there are no messages for this process
		else{
			//deschedule the process
			scheduler.waitForMSG();
			return null;
		}
	}

	private KernelMessage FetchMessage(){
		//if the process has a message waiting
		if(!scheduler.getCurrentlyRunning().msgQueue.isEmpty()){
			//return the message
			return scheduler.getCurrentlyRunning().msgQueue.remove();	
		}
		else{
			throw new RuntimeException("ATTEMPTING TO FETCH A MESSAGE FOR A PROCESS WITH NO MESSAGES");
		}
	}

//------------------------- PAGING ---------------------------------
	

	private void GetMapping(int virPage){
		PCB current = scheduler.getCurrentlyRunning();
		Random rand = new Random();
		//get a random int from 0-1 
		int TLBindex = rand.nextInt(2);
		//get the physical page mapped to by the provided virtual page
		VirtualToPhysicalMapping mapObj = current.getMappedPage(virPage);
		//if memory was not allocated for this virtual page, set the tlb to -1 and return
		if(mapObj == null){
			UserlandProcess.TLB[TLBindex][0] = virPage;
			UserlandProcess.TLB[TLBindex][1] = -1;
			return;
		}
		//if the virtual page was allocated, but has not yet been used...
		if(mapObj.physPageNum == -1){
			//attempt to find a physcial page to map to
			for(int i = 0; i < 1024; i++){
				if(!freeSpace[i]){
					mapObj.physPageNum = i;
					freeSpace[i] = true;
					break;
				}
			}
			//if there are no physical pages to map to, we must perform a page swap
			if(mapObj.physPageNum == -1){
				System.out.println("--------- PAGE SWAPPING TRIGGERED ---------");
				//select a random process to steal from
				PCB victimProcess = scheduler.getRandomProcess();
				//find the specific page from this process to steal
				VirtualToPhysicalMapping pageToSteal = null;
				for(int i = 0; i < 100; i++){
					if(victimProcess.getMappedPage(i) != null){
						if(victimProcess.getMappedPage(i).physPageNum != -1){
							pageToSteal = victimProcess.getMappedPage(i);
							break;
						}
					}
				}
				if(pageToSteal == null){
					throw new RuntimeException("could not find a page to steal in victimProcess");
				}
				System.out.println("VICTIM PROCESS: " + victimProcess.getPname());
				System.out.println("STEALING PHYSICAL PAGE: " + pageToSteal.physPageNum);
				//obtain the victimProcesses page data
				byte[] stolenPageData = UserlandProcess.writeOutPage(pageToSteal.physPageNum);
				//write the victim data to the swap file
				if(swapFile.Write(swapFileID, stolenPageData) == -1){
					throw new RuntimeException("Failed to write page data to swap file...");
				}
				//map the stealing processes physical page to the stolen page
				mapObj.physPageNum = pageToSteal.physPageNum;
				//map the victimprocesses page to -1
				pageToSteal.physPageNum = -1;
				//set the victims disk location
				pageToSteal.diskPageNum = pageNumber;
				System.out.println("STOLEN PAGE DATA WRITTEN TO: " + pageToSteal.diskPageNum);
				System.out.println("-------------------------------------------");
				//increment the swap file pointer
				pageNumber++;
			}
		}
		//if the stealing page was written to disk
		if(mapObj.diskPageNum != -1){
			//load the page in from the swap file
			swapFile.Seek(swapFileID, mapObj.diskPageNum * 1024);
			byte[] data = swapFile.Read(swapFileID, 1024);
			UserlandProcess.loadInPage(mapObj.physPageNum, data);
			swapFile.Seek(swapFileID, pageNumber * 1024);
		}
		//update the TLB based on the random number
		UserlandProcess.TLB[TLBindex][0] = virPage;
		UserlandProcess.TLB[TLBindex][1] = mapObj.physPageNum;
	}

	private int AllocateMemory(int size){
		PCB current = scheduler.getCurrentlyRunning();
		//find the amount of pages for this allocate
		int pages = size / 1024;
		//find a contigous block in the PCB
		int virBlockStart = current.findBlock(pages);
		return virBlockStart * 1024;
	}

	private boolean FreeMemory(int pointer, int size){
		PCB current = scheduler.getCurrentlyRunning();
		//find the starting page and the number of pages being freed
		int startPage = pointer / 1024;
		int pages = size / 1024;
		//if the start page and pages to be freed exceeds the max page..
		//return false
		if(startPage + pages > 1024){
			return false;
		}
		//free the block
		int[] physAddrs = current.freeBlock(startPage, pages);
		//reset the free space	
		for(int i = 0; i < physAddrs.length; i++){
			freeSpace[physAddrs[i]] = false;
		}

		return true;
	}

	private void ExitFreeMemory(){
		PCB current = scheduler.getCurrentlyRunning();
		System.out.println("FREEING MEMORY ON PROCESS: " + current.getPname());
		//find all the physical addressses being used by this process
		ArrayList<Integer> physAddrs = current.clearMemory();
		//reset all the phys addresses in free space
		for(int addr : physAddrs){
			freeSpace[addr] = false;
		}
	}

	private void resetTLB() {
		UserlandProcess.TLB[0][0] = -1;
		UserlandProcess.TLB[0][1] = -1;
		UserlandProcess.TLB[1][0] = -1;
		UserlandProcess.TLB[1][1] = -1;
	}
}
