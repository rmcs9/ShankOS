package ICSI412;

import java.util.ArrayList;

public class OS{
	
	private static Kernel kernel;

	public enum CallType{
		CreateProcess, SwitchProcess
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
		if(kernel.getScheduler().currentProcess != null){
			try{
				kernel.getScheduler().currentProcess.stop();
			}
			catch(InterruptedException e){
				throw new RuntimeException(e);
			}
		}
		else{
			while(kernel.getScheduler().currentProcess == null){
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
		try{
			kernel.getScheduler().currentProcess.stop();
		}
		catch(InterruptedException e){
			throw new RuntimeException(e);
		}	
	}

	public static void Startup(UserlandProcess init){
		//create kernel
		kernel = new Kernel();		
		//call create process for the up and idle
		CreateProcess(init);
		CreateProcess(new Idle());
	}

}