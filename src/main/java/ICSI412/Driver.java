package ICSI412;


public class Driver{
	public static void main (String[] args) {
		OS.Startup(new HelloWorld());
		OS.CreateProcess(new GoodbyeWorld());
		OS.CreateProcess(new DemotingProcess(), OS.Priority.RealTime);
		OS.CreateProcess(new SleepingProcess(), OS.Priority.RealTime);
 	}
}
