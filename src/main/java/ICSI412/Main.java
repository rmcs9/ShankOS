package ICSI412;


public class Main{
	public static void main (String[] args) {
		//hello world process
		OS.Startup(new HelloWorld());
		//goodbye world process
		OS.CreateProcess(new GoodbyeWorld());
		//demoting process - starts at realtime, demotes to backround
		OS.CreateProcess(new DemotingProcess(), OS.Priority.RealTime);
		//sleeping process - starts at realtime, sleeps, never gets demoted
		OS.CreateProcess(new SleepingProcess(), OS.Priority.RealTime);
		//write random process - writes random numbers to a file, opens a process that reads
		//random numbers from a file
		OS.CreateProcess(new WriteRandomProcess(), OS.Priority.Interactive);
		//print random numbers process - opens up a random device, prints 
		//random devices
		OS.CreateProcess(new PrintRandomProcess(), OS.Priority.Interactive);
		//ping and pong message processes - sends messages back and forth, 
		//incrementing "what" each time
		OS.CreateProcess(new PingProcess(), OS.Priority.Interactive);
		OS.CreateProcess(new PongProcess(), OS.Priority.Interactive);
 	}
}
