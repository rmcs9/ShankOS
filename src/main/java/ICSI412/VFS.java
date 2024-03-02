package ICSI412;


public class VFS implements Device{
	
	private int[] vfsID = new int[20];
	private Device[] vfsDevice = new Device[20];
	private RandomDevice random;
	private FakeFileSystem ffs;

	public VFS(){
		random = new RandomDevice();
		ffs = new FakeFileSystem();
		//initialize all the vfsID's to -1
		for(int i = 0; i < vfsID.length; i++){
			vfsID[i] = -1;
		}
	}

	public int Open(String s){
		//RANDOM DEVICES
		if(s.startsWith("random")){
			//open the random device
			int id = random.Open(s.substring(7));
			//find an empty index in the vfs array
			for(int i = 0; i < vfsID.length; i++){
				if(vfsID[i] == -1){
					//set the vfs id
					vfsID[i] = id;
					//set the vfs device
					vfsDevice[i] = random;
					//return the vfs index
					return i;
				}
			}
		}
		//FILES
		else if(s.startsWith("file")){
			//open the file device
			int id = ffs.Open(s.substring(5)); 
			//find an empty index in the vfs array
			for(int i = 0; i < vfsID.length; i++){
				if(vfsID[i] == -1){
					//set the vfs id
					vfsID[i] = id;
					//set the vfs device
					vfsDevice[i] = ffs;
					//return the vfs index
					return i;
				}
			}
		}
		//return -1 on failure
		return -1;
	}
	
	public void Close(int id){
		//call close on the specified device
		vfsDevice[id].Close(vfsID[id]);
		//reset the vfsID
		vfsID[id] = -1;
		//null the vfsDevice
		vfsDevice[id] = null;
	}

	public byte[] Read(int id, int size){
		//call read on the specified device
		return vfsDevice[id].Read(vfsID[id], size);
	}

	public void Seek(int id, int to){
		//call seek on the specified device
		vfsDevice[id].Seek(vfsID[id], to);
	}

	public int Write(int id, byte[] data){
		//call write on the specified device
		return vfsDevice[id].Write(vfsID[id], data);
	}

}
