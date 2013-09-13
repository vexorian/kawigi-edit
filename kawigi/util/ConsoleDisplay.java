package kawigi.util;

/**
 *	An encapsulation of the output method from a process's standard output or error streams.
 *	
 *	This allows better encapsulation, looser coupling, and more flexibility in changing the way
 *	things are displayed with the compiling and testing output.
 **/
public interface ConsoleDisplay
{
	/**
	 *	Called to append a string to the output display
	 **/
	public void print(String s);
	/**
	 *	Called to append a line to the output display.
	 **/
	public void println(String s);
	/**
	 *	Called to clear the output display.
	 **/
	public void clear();
}
