package ICSI412;



public interface Device{
	//Open a device
	int Open(String s);
	//close a device	
	void Close(int id);
	//read from a device
	byte[] Read(int id, int size);
	//seek on a device
	void Seek(int id, int to);
	//write to a device
	int Write(int id, byte[] data);

}
