package ICSI412;


public class Idle extends UserlandProcess{

	public void main(){
		while(true){
			cooperate();
			try{
				Thread.sleep(50);
			}
			catch(InterruptedException e){
				throw new RuntimeException(e);
			}
		}
	}

	public Idle(){
		super("IDLE");
	}
}
