// Kevin

public class SuperBlock {

	private final int defaultInodeBlocks = 48;
	public int totalBlocks; // the number of disk blocks
    public int totalInodes; // the number of inodes
    public int freeList;    // the block number of the free list's head
	
	public SuperBlock ( int diskSize ) {
		byte[] superBlock = new byte[512];
		SysLib.rawread( 0, superBlock );
		totalBlocks = SysLib.bytes2int( superBlock, 0 );
		totalInodes = SysLib.bytes2int( superBlock, 4 );
		freeList = SysLib.bytes2int( superBlock, 8 );
		
		if ( totalBlocks == diskSize && totalInodes > 0 && freeList >= 2 ) {
			return;
		}
		else {
			// need to format disk
			totalBlocks = diskSize;
			format ( defaultInodeBlocks );
		}
	}
	
	public void format ( int inodeCount ) {
		totalInodes = inodeCount;
		
		// set up inodes
		for ( int i = 0; i < totalInodes; i++ ) {
			Inode node = new Inode();
			node.flag = 0;
			node.toDisk((short)i);
		}
		
		// set up free list, each inode is 32 bytes. block is 512 
		freeList = ( ( totalInodes * 32 / 512 ) + 2 );
		for ( int nextFree = freeList; nextFree < totalBlocks; nextFree++ ) {
			byte[] data = new byte[512];
			for ( int i = 0; i < 512; i++ ) {
				data[i] = 0;
			}
			SysLib.int2bytes( nextFree + 1, data, 0 );
			SysLib.rawwrite( nextFree, data );
		}
		sync( );
	}
	
	// Syncs all data with disk
	public void sync( ) {
		byte[] data = new byte[512];
		SysLib.int2bytes(freeList, data, 8);
		SysLib.int2bytes(totalBlocks, data, 0);
		SysLib.int2bytes(totalInodes, data, 4);
		SysLib.rawwrite(0, data);
	}
	
	// gets a free block from the list
	public int getFreeBlock( ) {
		int free = freeList;
		if ( free > 0 ) {
			byte[] data = new byte[512];
			SysLib.rawread( free, data );
			freeList = SysLib.bytes2int( data, 0 );
			SysLib.int2bytes(0, data, 0);
			SysLib.rawwrite(free, data);
		}
		return free;
	}
	
	// returns a free block back to the disk
	public void returnBlock( int blockNum ) {
	
		// overwrite old block with an empty one
		byte[] emptyBlock = new byte[512];
		for ( int i = 0; i < 512; i++ ) {
			emptyBlock[i] = 0;
		}
		SysLib.int2bytes(freeList, emptyBlock, 0);
		SysLib.rawwrite(blockNum, emptyBlock);
		freeList = blockNum;
	}
}