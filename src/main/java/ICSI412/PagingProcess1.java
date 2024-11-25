package ICSI412;


public class PagingProcess1 extends UserlandProcess{


	public void main(){
		int startAddr = OS.AllocateMemory(2048);	
		System.out.println("PAGING PROCESS 1: startAddr: " + startAddr);
		try{
			Thread.sleep(250);
		}
		catch(InterruptedException e){
			throw new RuntimeException(e);
		}
		cooperate();
		Write(startAddr + 1999, (byte)120);
		System.out.println("PAGING PROCESS 1: wrote 120 to virtual address 1999");	
		byte readRes = Read(startAddr + 1999);
		System.out.println("PAGING PROCESS 1: reading from 1999: " + readRes);
		System.out.println("PAGING PROCESS 1: attempting to free second page of allocated memory");
		boolean free = OS.FreeMemory(startAddr + 1024, 1024);
		System.out.println("PAGING PROCESS 1: " + free);
		try{
			Thread.sleep(250);
		}
		catch(InterruptedException e){
			throw new RuntimeException(e);
		}
		cooperate();

		System.out.println("PAGING PROCESS 1: NOW ATTEMPTING TO READ FROM OUTSIDE PROCESS MEMORY...");
		byte error = Read(3333);
		OS.Exit();
	}



	public PagingProcess1(){
		super("PAGINGPROCESS1");
	}
}
