// Kevin Nguyen 
// CSS 430 Winter 2014
// Final Project 

public class FileSystem {
	
	private Directory directory;
	private FileTable filetable;
	private SuperBlock superblock;
	
	private final int SEEK_SET = 0;
	private final int SEEK_CUR = 1;
	private final int SEEK_END = 2;
	
	// constructor
	public FileSystem( int totalBlocks ) {
	
		superblock = new SuperBlock( totalBlocks );
		directory = new Directory( superblock.totalInodes );
		filetable = new FileTable( directory );
		
		FileTableEntry myDirectory = open( "/", "r" );
		
		int directorySize = fsize( myDirectory );
		if (directorySize > 0) {
			byte[] byteArray = new byte[directorySize];
			read( myDirectory, byteArray );
			directory.bytes2directory( byteArray );	
		}
		close( myDirectory );
	}
	
	// formats file system based on number of files or inodes
	public int format( int files ) {
		if ( files > 0 ) {
			superblock.format( files );
			directory = new Directory( superblock.totalInodes );
			filetable = new FileTable ( directory );
			return 0;
		}
		else {
			return -1;
		}
	}
	
	// opens a file given the file name and mode
	// allocates a new file descriptor table entry
	public FileTableEntry open( String fileName, String mode ) {
		FileTableEntry ftEnt = filetable.falloc( fileName, mode );
		if ( mode.equals( "w" ) ) {
			if ( deallocAllBlocks( ftEnt ) == false )
				return null;
		}
		return ftEnt;
	}
	
	// deallocates all entries for a file
	public boolean deallocAllBlocks( FileTableEntry fileEntry ) {
		if ( fileEntry.inode.count == 1 ) {
			byte[] emptyByte = fileEntry.inode.freeBlock( );
			if ( emptyByte != null ) {
				short index = SysLib.bytes2short( emptyByte, 0 );
				while ( index != -1 ) {
					superblock.returnBlock( index );
				}
			}
			// freeing direct blocks
			for (int i = 0; i < 11; i++ ) {
				if ( fileEntry.inode.direct[i] != -1 ) {
					superblock.returnBlock(fileEntry.inode.direct[i]);
					fileEntry.inode.direct[i] = -1;
				}
			}
			fileEntry.inode.toDisk( fileEntry.iNumber );   // saves to disk
		}
		return false;
	}
	
	// 
	public synchronized int read( FileTableEntry fd, byte[] buffer ) {
		if ( fd.mode == "w" || fd.mode == "a" ) {
			return -1;
		}
		int i = 0;
		int j = buffer.length;
		while ( ( j > 0 ) && fd.seekPtr < fsize(fd) ) {
			int k = fd.inode.findTargetBlock(fd.seekPtr);
			if (k == -1) {
				break;
			}
			byte[] emptyByte = new byte[512];
			SysLib.rawread( k, emptyByte );
			int m = fd.seekPtr % 512;
        

			int n = 512 - m;
			int i1 = fsize(fd) - fd.seekPtr;
			int i2 = Math.min(Math.min(n, j), i1);
			System.arraycopy(buffer, m, buffer, i, i2);
			fd.seekPtr += i2;
			i += i2;
			j -= i2;
		}
		return i;
	}
	
	public synchronized int close(FileTableEntry fd) {
		fd.count -= 1;
		if (fd.count > 0) {
			return 0;
		}
		return this.filetable.ffree(fd);
	}
	
	public int delete(String fname) {
		FileTableEntry localFileTableEntry = open(fname, "w");
		short s = localFileTableEntry.iNumber;
		if((close(localFileTableEntry) == 0) && (directory.ifree(s))) {
			return 0;
		}
		return -1;
	}
	
	public synchronized int write(FileTableEntry fd, byte[] buffer) {
		if (fd.mode == "r") {
		  return -1;
		}
			int i = 0;
			int j = buffer.length;
			while (j > 0) {
			int k = fd.inode.findTargetBlock(fd.seekPtr);
			if (k == -1) {
				int m = (short)this.superblock.getFreeBlock();
				switch (fd.inode.registerTargetBlock(fd.seekPtr, (short)m)) {
				case 0: 
					break;
				case 1: 
				case 2: 
					return -1;
				case 3: 
				short s = (short)this.superblock.getFreeBlock();
				if (fd.inode.registerIndexBlock(s) == -1) {
				  return -1;
				}
				if (fd.inode.registerTargetBlock(fd.seekPtr, (short)m) != 0) {
					return -1;
				}
				break;
			  }
			  k = m;
			}
			byte[] arrayOfByte = new byte[512];
			if (SysLib.rawread(k, arrayOfByte) == -1) {
			  System.exit(2);
			}
			int n = fd.seekPtr % 512;
			int i1 = 512 - n;
			int i2 = Math.min(i1, j);
			
			System.arraycopy(buffer, i, arrayOfByte, n, i2);
			

			SysLib.rawwrite(k, arrayOfByte);
			
			fd.seekPtr += i2;
			i += i2;
			j -= i2;
			if (fd.seekPtr > fd.inode.length) {
			  fd.inode.length = fd.seekPtr;
			}
		  }
		  fd.inode.toDisk(fd.iNumber);
		  
		  return i;
	}
	
	// looks through a file. offset is how many bytes into the data block. 
	// whence is where the seek pointer is located
	public synchronized int seek(FileTableEntry fd, int offset, int whence) {
		if (whence == 0) {
			if ((offset >= 0) && (offset <= fsize(fd))) {
				fd.seekPtr = offset;
			} else {
				return -1;
			}
		}
		else if ( whence == 1 ) {
			if ((fd.seekPtr + offset >= 0) && (fd.seekPtr + offset <= fsize(fd))) {
				fd.seekPtr += offset;
			} else {
				return -1;
			}
		}
		else if (whence == 2) {
			if ((fsize(fd) + offset >= 0) && (fsize(fd) + offset <= fsize(fd))) {
				fd.seekPtr = (fsize(fd) + offset);
			} 
			else {
				return -1;
			}
		}
			return fd.seekPtr;
	}
	
	// returns size of file
	public synchronized int fsize(FileTableEntry fd) {
		return fd.inode.length;
	}
}


