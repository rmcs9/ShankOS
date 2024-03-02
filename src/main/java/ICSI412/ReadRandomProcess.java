package ICSI412;



public class ReadRandomProcess extends UserlandProcess{

	//this process reads the 2000 random numbers from the file that was written to
	//by WriteRandomProcess
	public void main(){
		//open the file with the random numbers
		int file = OS.Open("file Nums.txt");	
		//if the file opens successfully...
		if(file != -1){
			byte[] output;
			for(int i = 0; i < 2000; i++){
				//read bytes from the file
				output = OS.Read(file, 1);
				//print out the random number
				System.out.println("random num " + i + " " + (int)output[0]);
				cooperate();
			}
		}
		else{
			System.out.println("randomNums.txt failed to open...");
		}
		OS.Exit();
	}

	public ReadRandomProcess(){
		super("READRANDOMPROCESS");
	}
}
