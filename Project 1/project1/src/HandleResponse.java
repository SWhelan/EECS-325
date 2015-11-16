import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 
 * The thread runnable to read the response from the server and send it to the client
 * 
 * @author Sarah Whelan
 *
 */
public class HandleResponse implements Runnable {
	private Socket server;
	private Socket client;

	public HandleResponse(Socket server, Socket client){
		this.server = server;
		this.client = client;
	}

	/**
	 * Gets the response from the server and passes it back to the client.
	 */
	@Override
	public void run() {
		try{
			// Get response from server
			InputStream in = server.getInputStream();
			int inputByte;
			// Open client's output stream
			PrintWriter out = new PrintWriter(client.getOutputStream());
			while ((byte)(inputByte = in.read()) != -1 && in != null && !server.isClosed()){
				// Immediately send each byte to the client
				out.write((byte) inputByte);
				out.flush();
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
