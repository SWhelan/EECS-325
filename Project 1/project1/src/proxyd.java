import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class proxyd {
	private static int DEFAULT_PORT = 5084;
	private static int PORT;
	
	public static void main (String[] args){
		// Set port number to given argument or the default
		if(!setPort(args)){
			System.out.println("The proxy was not started with appropriate arguments.");
			System.out.println("Defaulting to port" + DEFAULT_PORT + ".");
			PORT = DEFAULT_PORT;
		}
		
		// Open a server socket in a try with resources
		try (
			ServerSocket server = new ServerSocket(PORT);
		){
	    	System.out.println("The proxy is running at port " + PORT + "!");
		    while (true) {
		    	// For each new request make a new socket and spawn a thread to handle that client
		    	Socket client = server.accept();
		    	(new Thread(new HandleRequest(client))).start();
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the command line arguments are valid and attempts to set the port value
	 * to the argument value
	 * 
	 * @param args the command line arguments.
	 * @return true if the port number was successfully set
	 */
	private static boolean setPort(String[] args) {
		try {
			PORT = Integer.parseInt(args[1]);
		} catch (NumberFormatException | IndexOutOfBoundsException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
}
