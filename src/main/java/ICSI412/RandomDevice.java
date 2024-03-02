package ICSI412;

import java.util.Random;


public class RandomDevice implements Device{

	private Random[] rands = new Random[10];

	public int Open(String s){
		//if the seed is invalid throw an exception
		if(s == null || s == ""){
			throw new RuntimeException("invalid random seed provided");
		}
		//parse the seed
		int seed = Integer.parseInt(s);
		//find an empty slot in the random devices array
		for(int i = 0; i < rands.length; i++){
			if(rands[i] == null){
				//create a new random object
				rands[i] = new Random(seed);
				//return the index to the vfs
				return i;
			}
		}
		//return -1 on failure
		return -1;
	}
	
	public void Close(int id){
		//null the random object at the specified location
		rands[id] = null;
	}

	public byte[] Read(int id, int size){
		//create a new byte array to hold the random bytes
		byte[] res = new byte[size];
		//fill the array with random bytes
		rands[id].nextBytes(res);
		//return the array
		return res;
	}

	public void Seek(int id, int to){
		//create a new array of bytes
		byte[] reads = new byte[to];
		//read into the byte array but do not return
		rands[id].nextBytes(reads);
	}

	public int Write(int id, byte[] data){
		return 0;
	}
}
