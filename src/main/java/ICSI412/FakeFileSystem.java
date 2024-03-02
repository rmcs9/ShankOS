package ICSI412;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FakeFileSystem implements Device{

	private RandomAccessFile[] rafs = new RandomAccessFile[10];

	public int Open(String s){
		// if the file name is invalid, throw an excpetion
		if(s == null || s == ""){
			throw new RuntimeException("Empty or null file passed to FFS Open");
		}
		//attempt to find an empty slot in the ffs array
		for(int i = 0; i < rafs.length; i++){
			if(rafs[i] == null){
				try{
					//open up a new RandomAccessFile at the free index
					//with the given file name
					rafs[i] = new RandomAccessFile(s, "rw");
				}
				catch(FileNotFoundException e){
					//return -1 on failure
					return -1;
				}
				//return the index where the RandomAccessFile is located 
				//on success
				return i;
			}
		}
		//return -1 on failure
		return -1;
	}

	public void Close(int id){
		try{
			//attempt to close the RandomAccessFile
			rafs[id].close();
		}
		catch(IOException e){
			throw new RuntimeException(e);
		}
		//null the index location in the array
		rafs[id] = null;
	}

	public byte[] Read(int id, int size) {
		//create a new byte array to hold the content from the file
		byte[] res = new byte[size];
		try{
			//read from the file into the array
			rafs[id].read(res);
		}
		catch(IOException e){
			return null;
		}	
		//return the read bytes
		return res;
	}

	public void Seek(int id, int to){
		try{
			//seek the current file
			rafs[id].seek(to);
		}
		catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public int Write(int id, byte[] data){
		try{
			//attempt to write data to the file
			rafs[id].write(data);
		}
		catch(IOException e){
			//return -1 on failure
			return -1;
		}
		//return 1 on success
		return 1;
	}

}
