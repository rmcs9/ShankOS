package ICSI412;


public class HelloWorld extends UserlandProcess{
	
	public void main(){
		while(true){
			System.out.println("Hello World!");
			try{
				Thread.sleep(50);
			}
			catch(InterruptedException e){
				throw new RuntimeException(e);
			}
			cooperate();
		}
	}

	public HelloWorld(){
		super("HELLOWORLD");
	}
}
