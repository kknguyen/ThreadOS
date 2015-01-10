// Kevin

public class Inode {
    private final static int iNodeSize = 32;       // fix to 32 bytes
    private final static int directSize = 11;      // # direct pointers

    public int length;                             // file size in bytes
    public short count;                            // # file-table entries pointing to this
    public short flag;                             // 0 = unused, 1 = used, ...
    public short direct[] = new short[directSize]; // direct pointers
    public short indirect;                         // a indirect pointer

    Inode( ) {                                     // a default constructor
        length = 0;
        count = 0;
        flag = 1;
        for ( int i = 0; i < directSize; i++ )
			direct[i] = -1;
        indirect = -1;
    }

    Inode( short iNumber ) {                      // retrieving inode from disk
		int blockNumber = 1 + iNumber / 16;
		byte[] data = new byte[512];
		SysLib.rawread( blockNumber, data );	  // saving data to new data entry
		int offset = ( iNumber % 16 ) * 32;
		
		// setting up the proper offset for each information
		// that is stored in the inode
		length = SysLib.bytes2int( data, offset );
		offset += 4;
		count = SysLib.bytes2short ( data, offset );
		offset += 2;
		flag = SysLib.bytes2short( data, offset );
		offset += 2;
		
		// direct blocks
		for ( int i = 0; i < 11; i++ ) {
			direct[i] = SysLib.bytes2short( data, offset );
			offset += 2;
		}
		indirect = SysLib.bytes2short( data, offset );  // indirect block
	}

    void toDisk( short iNumber ) {                 // save to disk as the i-th inode
	
		// size of one Inode
		byte[] nodeByte = new byte[32];
		int offset = 0;
		
		// setting up the proper offset for each information
		// that is stored in the inode
		SysLib.int2bytes( length, nodeByte, offset );
		offset += 4;
		SysLib.short2bytes( count, nodeByte, offset );
		offset += 2;
		SysLib.short2bytes( flag, nodeByte, offset );
		offset += 2;
		
		// direct blocks
		for ( int i = 0; i < 11; i++ ) {
			SysLib.short2bytes( direct[i], nodeByte, offset );
			offset += 2;
		}
		SysLib.short2bytes( indirect, nodeByte, offset );  // indirect block
		offset += 2;
		
		int blockNumber = 1 + iNumber / 16;
		
		// read data into new byte array and then write to disk
		byte[] data = new byte[512];
		SysLib.rawread( blockNumber, data );
		offset = ( iNumber % 16 ) * 32;
		for ( int i = 0; i < 32; i++ ) {
			data[i] = nodeByte[i];
		}
		SysLib.rawwrite( blockNumber, data );
    }
	
	// return index of the inode
	short getIndexBlockNumber( ) {
		return indirect;
	}
	
	// set index of inode
	public boolean setIndexBlock( short indexBlockNumber ) {
		if ( indexBlockNumber <= 1 ) {
			return false;
		}
		indirect = indexBlockNumber;
		return true;
	}
	
	// find the inode
	public short findTargetBlock ( int offset ) {
		int i = offset / 512;
		if ( i < 11 ) {
			return direct[i];
		}
		if ( indirect  < 0 ) {
			return -1;
		}
		
		// check indirect block
		byte[] abyte = new byte[512];
		SysLib.rawread( indirect, abyte );
		int index = i - 11;
		return SysLib.bytes2short( abyte, index * 2 );
	}
	
	// free the inode
	byte[] freeBlock( ) {
		if (indirect >= 0) {
		
			// copy a blank block over the old inode
			byte[] emptyByte = new byte[512];
			SysLib.rawread( indirect, emptyByte );
			indirect = -1;
			return emptyByte;
		}
		else {
			return null;
		}
	}
	
	public int registerTargetBlock(int paramInt, short paramShort) {
		int i = paramInt / 512;
		if (i < 11) {
			if (this.direct[i] >= 0) {
				return -1;
			}
			if ((i > 0) && (this.direct[(i - 1)] == -1)) {
				return -2;
			}
			direct[i] = paramShort;
			return 0;
		}
		if (this.indirect < 0) {
			return -1;
		}
		byte[] arrayOfByte = new byte[512];
		SysLib.rawread(this.indirect, arrayOfByte);
		int j = i - 11;
		if ( SysLib.bytes2short( arrayOfByte, j * 2) > 0 ) {
			return -1;
		}
		SysLib.short2bytes( paramShort, arrayOfByte, j * 2 );
		SysLib.rawwrite( indirect, arrayOfByte );
		return 0;
	}
  
	public int registerIndexBlock(short paramShort) {
		for (int i = 0; i < 11; i++) {
			if (this.direct[i] == -1) {
				return -1;
			}
		}
		if (this.indirect != -1) {
			return -1;
		}
		indirect = paramShort;
		byte[] arrayOfByte = new byte[512];
		for (int j = 0; j < 256; j++) {
			SysLib.short2bytes((short)-1, arrayOfByte, j * 2);
		}
		SysLib.rawwrite(paramShort, arrayOfByte);
		return 0;
	}
}