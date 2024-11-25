package ICSI412;


public class PingProcess extends UserlandProcess {

	public void main(){
		//get this processes PID
		int thisPID = OS.GetPID();
		int targetPID = -1;
		//wait until the pong process is started up 
		//to fetch its PID
		while(targetPID == -1){
			try{
				Thread.sleep(50);
			}
			catch(InterruptedException e){
				throw new RuntimeException(e);
			}
			targetPID = OS.GetPIDByName("PONG");
		}
		//create an initial message to send to pong
		KernelMessage snd = new KernelMessage();
		snd.senderPID = thisPID;
		snd.targetPID = targetPID;
		snd.data = new byte[1];
		snd.data[0] = 0;
		//send the initial message
		OS.SendMessage(snd);
		while(true){
			//wait for a message back
			KernelMessage rcv = OS.WaitForMessage();
			//print out its debug information
			System.out.println("PING: from: " + rcv.senderPID + " to: " + rcv.targetPID + " what: " + rcv.data[0]);
			//repopulate a new message
			snd = new KernelMessage();
			snd.senderPID = thisPID;
			snd.targetPID = targetPID;
			snd.data = new byte[1];
			//increment what in the new message
			snd.data[0] = ++rcv.data[0];
			//send the new message
			OS.SendMessage(snd);
		}
	}

	public PingProcess(){
		super("PING");
	}
}
