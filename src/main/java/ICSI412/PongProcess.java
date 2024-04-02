package ICSI412;



public class PongProcess extends UserlandProcess{

	public void main(){
		//get this processes PID
		int thisPID = OS.GetPID();
		//get the ping processes PID
		int targetPID = OS.GetPIDByName("PING");
		while(true){
			//wait for a new message
			KernelMessage rcv = OS.WaitForMessage(); 
			//upon recieving the message, print out its debug info 
			System.out.println("PONG: from: " + rcv.senderPID + " to: " + rcv.targetPID + " what: " + rcv.data[0]);
			//create and populate a new message to send
			KernelMessage snd = new KernelMessage();
			snd.senderPID = thisPID;
			snd.targetPID = targetPID;
			snd.data = new byte[1];
			snd.data[0] = rcv.data[0];
			//send the new message to ping
			OS.SendMessage(snd);
		}
	}

	public PongProcess(){
		super("PONG");
	}
}
