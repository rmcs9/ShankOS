package ICSI412;


public class KernelMessage{
	

	public int senderPID;
	public int targetPID;

	public int msgType;

	public byte[] data;
	
	//copy constructor 
	public KernelMessage(KernelMessage k){
		senderPID = k.senderPID;
		targetPID = k.targetPID;
		msgType = k.msgType;
		data = k.data;
	}

	public KernelMessage(){}

	public String toString(){
		return "KERNEL MESSAGE TYPE: " + msgType + "\nTARGET: " + targetPID + 
				"\nSENDER: " + senderPID + "\nDATA SIZE: " + data.length;
	}
}
