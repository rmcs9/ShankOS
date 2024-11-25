package ICSI412;

import java.util.ArrayList;

public class OS{

	private static Kernel kernel;

	public enum CallType{
		CreateProcess, SwitchProcess, Sleep, CreatePriorityProcess,
		Open, Close, Read, Seek, Write, Exit, GetPID, GetPIDByName,
		SendMessage, WaitForMessage, FetchMessage, GetMapping,
		AllocateMemory, FreeMemory,
	}

	public enum Priority{
		RealTime, Interactive, Backround
	}

	public static CallType currentCall;

	public static ArrayList<Object> params;

	public static Object returnVal;

	public static int CreateProcess(UserlandProcess up){
		//reset the parameters:
		params = new ArrayList<Object>();
		//add new params to the param list
		params.add(up);
		//set the current call
		currentCall = CallType.CreateProcess;
		//switch to the kernel
		kernel.start();
		//stop the current process or wait for a process to start
		if(kernel.getScheduler().getCurrentlyRunning() != null){
			kernel.getScheduler().getCurrentlyRunning().stop();
		}
		else{
			while(kernel.getScheduler().getCurrentlyRunning() == null){
				try{
					Thread.sleep(20);
				}
				catch(InterruptedException e){
					throw new RuntimeException(e);
				}
			}
		}

		//cast and return the return val
		return (int)returnVal;
	}

	public static int CreateProcess(UserlandProcess up, Priority p){
		//reset the parameters
		params = new ArrayList<Object>();
		//add the UP and the priority
		params.add(up);
		params.add(p);
		//set current call
		currentCall = CallType.CreatePriorityProcess;
		//start the kernel
		kernel.start();
		//stop the current process or wait for a process to start
		if(kernel.getScheduler().getCurrentlyRunning() != null){
			kernel.getScheduler().getCurrentlyRunning().stop();
		}
		else{
			while(kernel.getScheduler().getCurrentlyRunning() == null){
				try{
					Thread.sleep(20);
				}
				catch(InterruptedException e){
					throw new RuntimeException(e);
				}
			}
		}
		//cast and return the return val
		return (int)returnVal;
	}

	public static void SwitchProcess(){
		//set the current call
		currentCall = CallType.SwitchProcess;
		PCB current = kernel.getScheduler().getCurrentlyRunning();
		//start the kernel
		kernel.start();
		//stop the currently running process
		current.stop();
	}

	public static void Startup(UserlandProcess init){
		//create kernel
		kernel = new Kernel();		
		//call create process for the up and idle
		CreateProcess(init);
		CreateProcess(new Idle());
	}

	public static void Sleep(int milliseconds){
		//set the current call
		currentCall = CallType.Sleep;
		//reset the parameters
		params = new ArrayList<Object>();
		//add the sleep time to the params
		params.add(milliseconds);
		PCB current = kernel.getScheduler().getCurrentlyRunning();
		//start the kernel
		kernel.start();
		//stop the currently running process
		current.stop();
	}


	//----------------------- DEVICES ---------------------------------------------


	public static int Open(String s){
		//set the current call
		currentCall = CallType.Open;
		//reset parameters
		params = new ArrayList<Object>();
		//reset the return value
		returnVal = null;
		//add the open string to the parameters
		params.add(s);
		PCB current = kernel.getScheduler().getCurrentlyRunning();
		//start the kernel
		kernel.start();
		//stop the current process
		current.stop();
		//sleep until the kernel is able to supply a file descriptor
		while(returnVal == null){
			try{
				Thread.sleep(20);
			}
			catch(InterruptedException e){
				throw new RuntimeException(e);
			}
		}
		//return the file descriptor of the newly opened device
		return (int) returnVal; 
	}

	public static void Close(int id){
		//set the currentCall
		currentCall = CallType.Close;
		//reset the parameters
		params = new ArrayList<Object>();
		//add the id to the parameters
		params.add(id);
		PCB current = kernel.getScheduler().getCurrentlyRunning();
		//start the kernel
		kernel.start();
		//stop the currently running process
		current.stop();
	}

	public static byte[] Read(int id, int size){
		//set the currentCall
		currentCall = CallType.Read;
		//reset the parameters
		params = new ArrayList<Object>();
		//add the id to the parameters
		params.add(id);
		//add the size to the parameters
		params.add(size);
		//reset the returnVal
		returnVal = null;
		PCB current = kernel.getScheduler().getCurrentlyRunning();
		//start the kernel
		kernel.start();
		//stop the currently running process
		current.stop();
		//sleep until the kernel provides a data response for the read
		while(returnVal == null){
			try{
				Thread.sleep(20);
			}
			catch(InterruptedException e){
				throw new RuntimeException(e);
			}
		}
		//return the read byte array
		return (byte[]) returnVal; 
	}

	public static void Seek(int id, int to){
		//set the currentCall
		currentCall = CallType.Seek;
		//reset the parameters
		params = new ArrayList<Object>();
		//add id to the parameters
		params.add(id);
		//add the size to the parameters
		params.add(to);
		PCB current = kernel.getScheduler().getCurrentlyRunning();
		//start the kernel
		kernel.start();
		//stop the currently running process
		current.stop();
	}

	public static int Write(int id, byte[] data){
		//set the currentCall
		currentCall = CallType.Write;
		//reset the paramters
		params = new ArrayList<Object>();
		//add the id to the parameters
		params.add(id);
		//add the data to the parameters
		params.add(data);
		//null the return val
		returnVal = null;
		PCB current = kernel.getScheduler().getCurrentlyRunning();
		//start the kernel
		kernel.start();
		//stop the currently running process
		current.stop();
		//sleep until the kernel provides a return from the device
		while(returnVal == null){
			try{
				Thread.sleep(20);
			}
			catch(InterruptedException e){
				throw new RuntimeException(e);
			}
		}
		//return the returnVal
		return (int) returnVal;
	}


	public static void Exit(){
		//set the currentCall
		currentCall = CallType.Exit;
		PCB current = kernel.getScheduler().getCurrentlyRunning();
		//start the kernel
		kernel.start();
		//stop the currently running process
		current.stop();
	}

	//----------------------- MESSAGES ---------------------------------------------

	public static int GetPID(){
		//set current call
		currentCall = CallType.GetPID;
		//null the return val
		returnVal = null;
		PCB current = kernel.getScheduler().getCurrentlyRunning();
		//start the kernel and stop the currently running process
		kernel.start();
		current.stop();
		//wait for the return val to be populated
		while(returnVal == null){
			try{
				Thread.sleep(20);
			}
			catch(InterruptedException e){
				throw new RuntimeException(e);
			}
		}
		//return the PID
		return (int) returnVal;
	}

	public static int GetPIDByName(String pname){
		//set the current call
		currentCall = CallType.GetPIDByName;
		//null the return val
		returnVal = null;
		params = new ArrayList<>();
		//add the pname to the params
		params.add(pname);
		PCB current = kernel.getScheduler().getCurrentlyRunning();
		//start the kernel and stop the currently running process
		kernel.start();
		current.stop();
		//wait for the return val to be populated
		while(returnVal == null){
			try{
				Thread.sleep(20);
			}
			catch(InterruptedException e){
				throw new RuntimeException(e);
			}
		}
		//return the PID
		return (int) returnVal;
	}

	public static void SendMessage(KernelMessage km){
		//set the current call
		currentCall = CallType.SendMessage;
		params = new ArrayList<>();
		//add the message to the params
		params.add(km);
		PCB current = kernel.getScheduler().getCurrentlyRunning();
		//start the kernel and stop the currently running process
		kernel.start();
		current.stop();
	}

	public static KernelMessage WaitForMessage(){
		//set the current call
		currentCall = CallType.WaitForMessage;
		//null the return val
		returnVal = null;
		PCB current = kernel.getScheduler().getCurrentlyRunning();
		//start the kernel and stop the currently running process
		kernel.start();
		current.stop();
		//if a process was descheduled and waiting for a message
		//this condition will succeed 
		if(!(returnVal instanceof KernelMessage)){
			//set the current call to fetch this processes waiting message
			currentCall = CallType.FetchMessage;
			//start the kernel and stop the process
			kernel.start();
			current.stop();
			//wait for the return val to be populated with the waiting message
			while(!(returnVal instanceof KernelMessage)){
				try{
					Thread.sleep(20);
				}
				catch(InterruptedException e){
					throw new RuntimeException(e);
				}
			}
		}
		//return the message
		return (KernelMessage) returnVal;
	}

	//------------------------- PAGING ----------------------------------------

	public static void GetMapping(int virtualPageNumber){
		//set the current call
		currentCall = CallType.GetMapping;
		//reset params
		params = new ArrayList<Object>();
		//add the virtual page num to params
		params.add(virtualPageNumber);
		PCB current = kernel.getScheduler().getCurrentlyRunning();
		//start the kernel and stop the current process
		kernel.start();
		current.stop();
	}

	public static int AllocateMemory(int size){
		//ensure the size is divisble by 1024
		if(size % 1024 != 0){
			System.out.println("attempting to allocate memory with size not divisable by 1024." +
				" size: " + size);
			OS.Exit();
			return -1;
		}
		//set the current call and params
		//clear the return val
		currentCall = CallType.AllocateMemory;
		returnVal = null;
		params = new ArrayList<Object>();
		params.add(size);
		PCB current = kernel.getScheduler().getCurrentlyRunning();
		//start the kernel and stop currently running
		kernel.start();
		current.stop();
		while(returnVal == null){
			try{
				Thread.sleep(20);
			}
			catch(InterruptedException e){
				throw new RuntimeException(e);
			}
		}
		//if the allocate fails...
		if((int) returnVal == -1){
			//exit the process
			OS.Exit();
		}
		return (int) returnVal;
	}

	public static boolean FreeMemory(int pointer, int size){
		//ensure that size and pointer addr are divisble by 1024
		if(size % 1024 != 0){
			System.out.println("attempting to free memory of invalid size. " +
				"size: " + size);
			OS.Exit();
			return false;
		}
		if(pointer % 1024 != 0){
			System.out.println("attempting to free memory from an invalid memory address. " +
				"pointer: " + pointer);
			OS.Exit();
			return false;
		}
		//set current call and params
		//reset return val
		currentCall = CallType.FreeMemory;
		returnVal = null;
		params = new ArrayList<Object>();
		params.add(pointer);
		params.add(size);
		PCB current = kernel.getScheduler().getCurrentlyRunning();
		kernel.start();
		current.stop();
		while(returnVal == null){
			try{
				Thread.sleep(20);
			}
			catch(InterruptedException e){
				throw new RuntimeException(e);
			}
		}
		//if the free fails...
		if(!(boolean)returnVal){
			//kill the process
			OS.Exit();	
		}
		return (boolean) returnVal;
	}


}
