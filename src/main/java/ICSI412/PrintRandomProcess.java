package ICSI412;



public class PrintRandomProcess extends UserlandProcess{

	public void main(){
		//open random device
		int randgen = OS.Open("random 99");
		//if the device opens successfully
		if(randgen != -1){
			byte[] rand = new byte[1];
			while(true){
				//read a random byte
				rand = OS.Read(randgen, 1);	
				//print out the random byte
				System.out.println("printing random num: " + rand[0]);
				//sleep
				try{
					Thread.sleep(50);
				}
				catch(InterruptedException e){
					throw new RuntimeException(e);
				}
				cooperate();
			}
		}
		else{
			throw new RuntimeException("failed to open random number generator in PrintRandomProcess");
		}
	}

	public PrintRandomProcess(){
		super("PRINTRANDOMPROCESS");
	}
}
