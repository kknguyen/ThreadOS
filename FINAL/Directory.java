// Kevin Nguyen 
// CSS 430 Winter 2014
// Final Project 

public class Directory {

    private static int maxChars = 30; // max characters of each file name
    // Directory entries
    private int[] fsize;        // each element stores a different file size.
    private char[][] fnames;    // each element stores a different file name.

    public Directory( int maxInumber ) { // directory constructor
        fsize = new int[maxInumber];     // maxInumber = max files
        for ( int i = 0; i < maxInumber; i++ )
             fsize[i] = 0;                 // all file size initialized to 0
        fnames = new char[maxInumber][maxChars];
        String root = "/";                // entry(inode) 0 is "/"
        fsize[0] = root.length( );        // fsize[0] is the size of "/".
        root.getChars( 0, fsize[0], fnames[0], 0 ); // fnames[0] includes "/"
    }

	// assumes data[] received directory information from disk
	// initializes the Directory instance with this data[]
    public void bytes2directory( byte[] data ) {
		int dataOffset = 0;
		for ( int i = 0; i < fsize.length; i++ ) {
			fsize[i] = SysLib.bytes2int( data, dataOffset );
			dataOffset += 4;
		}
		for ( int k = 0; k < fnames.length; k++ ) {
			String fileName = new String( data, dataOffset, maxChars * 2 );
			fileName.getChars( 0, fsize[k], fnames[k], 0 );
			dataOffset += maxChars * 2;
		}
    }
	
	// converts and return Directory information into a plain byte array
    // this byte array will be written back to disk
    // note: only meaningful directory information should be converted
    // into bytes.
    public byte[] directory2bytes( ) {
        int dataOffset = 0;
		int arrLength = fsize.length * ( 4 + maxChars * 2 );
		byte[] returnByte = new byte[arrLength];
		for ( int i = 0; i < fsize.length; i ++ ) {
			SysLib.int2bytes( fsize[i], returnByte, dataOffset );
			dataOffset += 4;
		}
		for ( int k = 0; k < fnames.length; k++ ) {
			String fileName = new String( fnames[k], 0, fsize[k] );
			byte[] str2byte = fileName.getBytes();
			System.arraycopy( str2byte, 0, returnByte, dataOffset, str2byte.length );
			dataOffset += maxChars * 2;
		}
		return returnByte;
	}

	// filename is the one of a file to be created.
    // allocates a new inode number for this filename
    public short ialloc( String filename ) {
        short inodeValue = -1;
		for ( int i = 0; i < fsize.length; i++ ) {
			if ( fsize[i] == 0 ) {
				inodeValue =(short)i;
				if ( filename.length() < maxChars ) {
					fsize[i] = filename.length();
				}
				else {
					fsize[i] = maxChars;
				}
				filename.getChars( 0, fsize[i], fnames[i], 0 );
			}
		}
		return inodeValue;
    }

	// deallocates this inumber (inode number)
    // the corresponding file will be deleted.
    public boolean ifree( short iNumber ) {
		if ( fsize[iNumber] > 0 ) {
			fsize[iNumber] = 0;
			return true;
		}
		return false;
    }

	// returns the inumber corresponding to this filename
     public short namei( String filename ) {
		for ( short i = 0; i < fsize.length; i++ ) {
			String checkName = new String( fnames[i], 0, fsize[i] );
			if ( fsize[i] > 0 && checkName.equals(filename) ) {
				return i;
			}
		}
		return -1;
    }
}
