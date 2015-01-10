class TestThread1 extends Thread
{
private String name;

   public TestThread1( String args[] ) {
       name = args[0];
       SysLib.cout( "TestThread1: " + this + "constructor invoked " 
		    + name + "\n" );
   }

    public TestThread1( ) {
	name = "none";
	SysLib.cout( "TestThread1: " + this + " constructor invoked " 
		     + name + "\n" );
    }
   
   public void run() {
	/* 
	 * The thread does something
  	 **/
       SysLib.cout( "TestThread1: current pri=" + getPriority( ) + "\n" );
       SysLib.cout( "I am thread " + name + "\n" );
       if ( name != null && name != "none" ) {
	   String[] args = SysLib.stringToArgs( "TestThread1" );
	   SysLib.exec( args );
	   SysLib.join( );
       }
       for (int i = 0; i < 10; i++) {
	   SysLib.cout( "I am thread " + name + "\n" );
	   SysLib.sleep( 300 );
       }

       SysLib.sync( );
       SysLib.exit( );
   }
}
