package ICSI412;

import java.util.ArrayList;

public class OS{
	
	private static Kernel kernel;

	public enum CallType{
		CreateProcess, SwitchProcess, Sleep, CreatePriorityProcess,
		Open, Close, Read, Seek, Write, Exit
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
					Thread.sleep(50);
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
					Thread.sleep(50);
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
		//start the kernel
		kernel.start();
		//stop the currently running process
		kernel.getScheduler().getCurrentlyRunning().stop();
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
		//start the kernel
		kernel.start();
		//stop the currently running process
		kernel.getScheduler().getCurrentlyRunning().stop();
	}

	public static int Open(String s){
		//set the current call
		currentCall = CallType.Open;
		//reset parameters
		params = new ArrayList<Object>();
		//reset the return value
		returnVal = null;
		//add the open string to the parameters
		params.add(s);
		//start the kernel
		kernel.start();
		//stop the current process
		kernel.getScheduler().getCurrentlyRunning().stop();
		//sleep until the kernel is able to supply a file descriptor
		while(returnVal == null){
			try{
				Thread.sleep(50);
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
		//start the kernel
		kernel.start();
		//stop the currently running process
		kernel.getScheduler().getCurrentlyRunning().stop();
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
		//start the kernel
		kernel.start();
		//stop the currently running process
		kernel.getScheduler().getCurrentlyRunning().stop();
		//sleep until the kernel provides a data response for the read
		while(returnVal == null){
			try{
				Thread.sleep(50);
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
		//start the kernel
		kernel.start();
		//stop the currently running process
		kernel.getScheduler().getCurrentlyRunning().stop();
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
		//start the kernel
		kernel.start();
		//stop the currently running process
		kernel.getScheduler().getCurrentlyRunning().stop();
		//sleep until the kernel provides a return from the device
		while(returnVal == null){
			try{
				Thread.sleep(50);
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
		//start the kernel
		kernel.start();
		//stop the currently running process
		kernel.getScheduler().getCurrentlyRunning().stop();
	}
}
