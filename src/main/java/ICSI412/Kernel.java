package ICSI412;

import java.util.concurrent.Semaphore;

public class Kernel implements Runnable, Device{
	
	private Scheduler scheduler;

	private Thread thread;

	private Semaphore semaphore;

	private VFS vfs;

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
		thread.start();
	}

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
}
