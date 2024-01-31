package ICSI412;


public class Driver{
	public static void main (String[] args) {
		OS.Startup(new HelloWorld());
		OS.CreateProcess(new GoodbyeWorld());
 	}
}
