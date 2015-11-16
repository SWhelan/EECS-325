import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HandleResponse implements Runnable {
	private Socket getResponseFrom;
	private Socket sendResponseTo;

	public HandleResponse(Socket getResponseFrom, Socket sendResponseTo){
		this.getResponseFrom = getResponseFrom;
		this.sendResponseTo = sendResponseTo;
	}

	@Override
	public void run() {
		try{
			// Get response from server
			BufferedReader is = new BufferedReader(new InputStreamReader(getResponseFrom.getInputStream()));
			String isLine;
			StringBuilder builder = new StringBuilder();
			while ((isLine = is.readLine()) != null){
				builder.append(isLine + "\r\n");
				if(isLine.contains("Connection:")){
					isLine = "Connection: keep-alive";
				}
			}
			builder.append("\r\n");

			// Send response to client
			PrintWriter out = new PrintWriter(sendResponseTo.getOutputStream());
			//System.out.println(builder.toString());
			out.write(builder.toString());
			out.flush();
			//out.close();
			//is.close();
			sendResponseTo.close();
		} catch (Exception e){
			e.printStackTrace();
			System.out.println("Exception sdlkfjdsf");
		}
	}

}
