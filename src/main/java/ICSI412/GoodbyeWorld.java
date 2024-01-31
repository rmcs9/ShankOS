package ICSI412;


public class GoodbyeWorld extends UserlandProcess{

	public void main(){
		while(true){
			System.out.println("Goodbye World!");
			try{
				Thread.sleep(50);
			}
			catch(InterruptedException e){
				throw new RuntimeException(e);
			}
			cooperate();
		}
	}

	public GoodbyeWorld(){
		super("GOODBYEWORLD");
	}

}
