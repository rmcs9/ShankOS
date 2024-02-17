package ICSI412;



public class SleepingProcess extends UserlandProcess{
	



	public void main(){
		while(true){
			System.out.println("SLEEPING PROCESS");
			try{
				Thread.sleep(50);
			}
			catch(InterruptedException e){
				throw new RuntimeException(e);
			}
			OS.Sleep(100);
			cooperate();
		}
	}

	public SleepingProcess(){
		super("SLEEPINGPROCESS");
	}
}
