import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class HandleRequest implements Runnable{
	private Socket client;
	private Socket externalSocket;
	private static HashMap<String, CacheEntry> cache = new HashMap<String, CacheEntry>();
	public HandleRequest(Socket client){
		this.client = client;
	}

	@Override
	public void run() {
		try {
			// Get request from client
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String inputLine;
			int sourcePort = client.getPort();
			StringBuilder builder = new StringBuilder();
			String host = "";
			boolean dontInclude = false;
			while ((inputLine = in.readLine())!= null && !inputLine.equals("")) {
				if(inputLine.contains("Host:")){
					host = inputLine.split(" ")[1];
				}
				if(inputLine.contains("Connection:")){
					inputLine = "Connection: close";
				}
				if(inputLine.contains("Accept-Encoding: gzip, deflate")){
					dontInclude = true;
				}
				if(!dontInclude){
					builder.append(inputLine + "\r\n");
				}
				dontInclude = false;
			}
			builder.append("\r\n");
			//System.out.println(builder.toString());
			// Forward Request to server
			String hostNameOrIp;
			CacheEntry entry = cache.get(host);
			if(entry != null && entry.lessThan30Sec()){
				hostNameOrIp = entry.getIp();
			} else {
				if(entry != null){
					cache.remove(host);
				}
				hostNameOrIp = host;
			}
			System.out.println(hostNameOrIp);
			externalSocket = new Socket(hostNameOrIp, 80);
			String ip = externalSocket.getInetAddress().getHostAddress();
			if(!cache.containsKey(host)){
				System.out.println("saving: " + ip);
				cache.put(host, new CacheEntry(ip));
			}
			PrintWriter os = new PrintWriter(externalSocket.getOutputStream());
			os.write(builder.toString());
			os.flush();
			//in.close();
			//os.close();
			(new Thread(new HandleResponse(externalSocket, client))).start();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
