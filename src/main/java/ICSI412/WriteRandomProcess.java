package ICSI412;



public class WriteRandomProcess extends UserlandProcess{
	

	//this process writes 2000 random numbers to a file
	public void main(){
		// open a new random generator and file
		int rand = OS.Open("random 56");
		int file = OS.Open("file Nums.txt");
		//if they opened successfully, continue
		if(rand != -1 && file != -1){	
			for(int i = 0; i < 500; i++){
				//read 4 random bytes
				byte[] randomNums = OS.Read(rand, 4);	
				//print the random numbers out
				for(int j = 0; j < randomNums.length; j++){
					System.out.println(randomNums[j]);
				}
				//write the random numbers to the file
				if(OS.Write(file, randomNums) == -1){
					throw new RuntimeException("failed to write to nums.txt");
				}
				cooperate();
			}
			System.out.println("RANDOM NUMBERS WRITTEN TO FILE");
			System.out.println("STARTING READ PROCESS");
			//start the read process to read from the random numbers file
			OS.CreateProcess(new ReadRandomProcess(), OS.Priority.Interactive);
		}
		else{
			System.out.println("WriteRandomProcess failed to open devices");
		}
		OS.Exit();
	}

	public WriteRandomProcess(){
		super("WRITERANDOM");
	}
}
