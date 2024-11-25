package ICSI412;


public class DemotingProcess extends UserlandProcess{

	public void main(){
		while(true){
			System.out.println("DEMOTING");

			try{
				Thread.sleep(50);
			}
			catch(InterruptedException e){
				throw new RuntimeException(e);
			}
			cooperate();
		}
	}

	public DemotingProcess(){
		super("DEMOTING");
	}
}
