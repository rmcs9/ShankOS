package ICSI412;


public class VirtualToPhysicalMapping{

	public int physPageNum;
	public int diskPageNum;

	public VirtualToPhysicalMapping(){
		physPageNum = -1;
		diskPageNum = -1;
	}
}
