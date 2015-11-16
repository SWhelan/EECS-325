import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * The thread runnable to read the request from the client and send to the server
 * 
 * @author Sarah Whelan
 *
 */
public class HandleRequest implements Runnable{
	private Socket client;
	private Socket server;
	// Strings for processing request headers 
	private static final String CONNECTION_HEADER = "Connection: keep-alive";
	private static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding: gzip, deflate";

	public HandleRequest(Socket client){
		this.client = client;
	}

	/**
	 * Gets the request from the client, processes it and passes it to the server.
	 */
	@Override
	public void run() {
		try {
			// Get request from client
			InputStream in = client.getInputStream();
			int inputByte;
			List<Byte> input = new LinkedList<Byte>();
			StringBuilder builder = new StringBuilder();
			while (in.available() > 0 && (byte)(inputByte = in.read()) != -1) {
				input.add((byte) inputByte);
				builder.append((char) (byte) inputByte);
				System.out.print((char) (byte) inputByte);
			}
			// Process Request
			String inputString = builder.toString();
			String host = getHostNameFromRequest(inputString);
			input = processRequest(input, inputString);
			// Get from cache or use host name
			host = Cache.getHostNameOrIp(host);
			
			if(!host.equals("")){
				// Connect to the host
				server = new Socket(host, 80);
				// Add to cache if not already there
				Cache.add(host, server.getInetAddress().getHostAddress());
				// Forward Request to server
				PrintWriter os = new PrintWriter(server.getOutputStream());
				Iterator<Byte> iter = input.iterator();
				while(iter.hasNext()){
					os.write(iter.next());
					os.flush();
				}
				// Send response back to client in a new thread
				(new Thread(new HandleResponse(server, client))).start();
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Attempts to find the host name to connect to in the request headers
	 * 
	 * @param inputString the request in string form
	 * @return hostname of request
	 */
	private String getHostNameFromRequest(String inputString) {
		String host = "";
		if(inputString.contains("Host: ")){
			host = inputString.split("Host: ")[1];
			host = host.substring(0, host.indexOf('\r'));
		}
		return host;
	}

	/**
	 * Changes the Connection: keep alive request header to Connection: close
	 * and removes the Accept-Encoding: gzip, deflate header
	 * 
	 * @param inputList
	 * @param inputString
	 * @return modified request in a list of bytes
	 */
	private List<Byte> processRequest(List<Byte> inputList, String inputString){
		List<Byte> input = inputList;
		if(inputString.contains(CONNECTION_HEADER)){
			int startIndex = inputString.split(CONNECTION_HEADER)[0].length()+12;
			int endIndex = startIndex + 10;
			for(int i = endIndex; i >= startIndex; i--){
				input.remove(i);
			}
			input.add(startIndex, (byte) 'e');
			input.add(startIndex, (byte) 's');
			input.add(startIndex, (byte) 'o');
			input.add(startIndex, (byte) 'l');
			input.add(startIndex, (byte) 'c');
		}
		
		if(inputString.contains(ACCEPT_ENCODING_HEADER)){
			int startIndex = inputString.split(ACCEPT_ENCODING_HEADER)[0].length();
			int endIndex = startIndex + ACCEPT_ENCODING_HEADER.length()+1;
			for(int i = endIndex; i >= startIndex; i--){
				input.remove(i);
			}
		}
		return input;
	}
}
