package ICSI412;

import java.util.Random;

public class VirtualMemoryPigProcess extends UserlandProcess {

	public void main(){
		//initialze a byte array of random bytes
		byte[] randomBytes = new byte[100];
		Random rand = new Random();
		rand.nextBytes(randomBytes);

		//allocate memory for 100 pages
		int startAddr = OS.AllocateMemory(102400);
		//write the random bytes to the first address of every page
		System.out.println("WRITING TO PAGES FOR: " + this.pname);	
		for(int i = startAddr, j = 0; j < 100; i += 1024, j++){
			System.out.println("Writing " + randomBytes[j] + " to vir addr: " + i);
			Write(i, randomBytes[j]);
		}
		while(true){
			System.out.println("READING PAGES FROM: " + this.pname);
			//read the bytes at the start of every page, and ensure that 
			//they match the random bytes that we wrote initially
			for(int i = startAddr, j = 0; j < 100; i += 1024, j++){
				byte val = Read(i);
				//if the bytes do not match, throw an exception
				if(val != randomBytes[j]){
					throw new RuntimeException("VALUE READ FROM MEMORY DOES NOT MATCH!\nGOT: " + val + "\nEXPECTED: " + randomBytes[j]
						+ "\nADDR: " + i);
				}
				System.out.println("VAL READ FROM ADDR: " + i + " VAL: " + val);
			}
			try{
				Thread.sleep(250);
			} catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            cooperate();
		}
	}


	public VirtualMemoryPigProcess(int num){
		super("VIRTUALMEMORYPROCESS" + num);
	}
}
