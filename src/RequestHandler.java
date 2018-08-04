import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class RequestHandler implements Runnable {

	/**
	 * Socket connected to client passed by Proxy server
	 */
	Socket clientSocket;

	/**
	 * Read data client sends to proxy
	 */
	BufferedReader proxyToClientBr;

	/**
	 * Send data from proxy to client
	 */
	BufferedWriter proxyToClientBw;
	

	/**
	 * Thread that is used to transmit data read from client to server when using HTTPS
	 * Reference to this is required so it can be closed once completed.
	 */
	public static Thread httpsClientToServer;

	//public static byte[] httpsRequestByte = new byte[10350];
	public static String httpsUrl = "";
	public static String endMessageString = "\r\r\n\n";


	/**
	 * Creates a ReuqestHandler object capable of servicing HTTP(S) GET requests
	 * @param clientSocket socket connected to the client
	 */
	public RequestHandler(Socket clientSocket){
		System.out.println("RequestHandler\n\n");
		this.clientSocket = clientSocket;
		try{
			this.clientSocket.setSoTimeout(92000);
			proxyToClientBr = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			proxyToClientBw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			//httpsRequestByte = new byte[10350];
			//ToDo
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	
	/**
	 * Reads and examines the requestString and calls the appropriate method based 
	 * on the request type. 
	 */
	@Override
	public void run() {

		// Get Request from client
		String requestString;
		System.out.println("RequestHandler RUN \n\n");
		try{
			requestString = proxyToClientBr.readLine();
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("Error reading request from client");
			return;
		}

		// Parse out URL

		System.out.println("Reuest Received " + requestString);
		// Get the Request type
		String request = requestString.substring(0,requestString.indexOf(' '));

		// remove request type and space
		String urlString = requestString.substring(requestString.indexOf(' ')+1);

		// Remove everything past next space
		urlString = urlString.substring(0, urlString.indexOf(' '));

		// Prepend http:// if necessary to create correct URL
		if(!urlString.substring(0,4).equals("http")){
			String temp = "http://";
			urlString = temp + urlString;
		}




		// Check request type
		if(request.equals("CONNECT")){
			System.out.println("HTTPS Request for : " + urlString + "\n");
			handleHTTPSRequest(urlString);
		} 

		else{

			System.out.println("HTTP GET for : " + urlString + "\n");
			sendNonCachedToClient(urlString);

		}
	} 



	/**
	 * Sends the contents of the file specified by the urlString to the client
	 * @param urlString URL ofthe file requested
	 */
	private void sendNonCachedToClient(String urlString){
		System.out.println("sendNonCachedToClient-HTTP REQUESTS\n\n");

		try{

			String fileExtension=""; ////Temporary
			// Check if file is an image
			if((fileExtension.contains(".png")) || fileExtension.contains(".jpg") ||
					fileExtension.contains(".jpeg") || fileExtension.contains(".gif")){
				// Create the URL
				/*URL remoteURL = new URL(urlString);
				BufferedImage image = ImageIO.read(remoteURL);

				if(image != null) {
					// Cache the image to disk
					//ImageIO.write(image, fileExtension.substring(1), fileToCache);

					// Send response code to client
					String line = "HTTP/1.0 200 OK\n" +
							"Proxy-agent: ProxyServer/1.0\n" +
							"\r\n";
					proxyToClientBw.write(line);
					proxyToClientBw.flush();

					// Send them the image data
					ImageIO.write(image, fileExtension.substring(1), clientSocket.getOutputStream());

				// No image received from remote server
				} else {
					System.out.println("Sending 404 to client as image wasn't received from server"
							+ fileName);
					String error = "HTTP/1.0 404 NOT FOUND\n" +
							"Proxy-agent: ProxyServer/1.0\n" +
							"\r\n";
					proxyToClientBw.write(error);
					proxyToClientBw.flush();
					return;
				}*/
			}

			// File is a text file
			else {

                Global.imageCounter++;

                urlString = urlString+endMessageString;
                Encoder.byte2Image(urlString.getBytes(), urlString.getBytes().length,Integer.toString(Global.imageCounter)+"-C");



				/*
				// Create the URL
				URL remoteURL = new URL(urlString);
				// Create a connection to remote server
				HttpURLConnection proxyToServerCon = (HttpURLConnection)remoteURL.openConnection();
				proxyToServerCon.setRequestProperty("Content-Type", 
						"application/x-www-form-urlencoded");
				proxyToServerCon.setRequestProperty("Content-Language", "en-US");  
				proxyToServerCon.setUseCaches(false);
				proxyToServerCon.setDoOutput(true);
			
				// Create Buffered Reader from remote Server
				BufferedReader proxyToServerBR = new BufferedReader(new InputStreamReader(proxyToServerCon.getInputStream()));
				*/


				// Send success code to client
				String line = "HTTP/1.0 200 OK\n" +
						"Proxy-agent: ProxyServer/1.0\n" +
						"\r\n";
				proxyToClientBw.write(line);
				Client.runProxy(this.clientSocket);


				//not sure, lets test it
                proxyToClientBw.write(Client.clientBuffer);
				//ToDo in running should be placed
				proxyToClientBw.flush();






                // Read from input stream between proxy and remote server
				/*while((line = proxyToServerBR.readLine()) != null){
					// Send on data to client
					proxyToClientBw.write(line);

					// Write to our cached copy of the file
					//if(caching){
					//	fileToCacheBW.write(line);
					//}
				}
				
				// Ensure all data is sent by this point
				proxyToClientBw.flush();
				*/

				// Close Down Resources
				/*if(proxyToServerBR != null){
					proxyToServerBR.close();
				}*/
			}

			if(proxyToClientBw != null){
				proxyToClientBw.close();
			}

		} 

		catch (Exception e){
			e.printStackTrace();
		}
	}

	
	/**
	 * Handles HTTPS requests between client and remote server
	 * @param urlString desired file to be transmitted over https
	 */
	private void handleHTTPSRequest(String urlString){

		System.out.println("handleHTTPSRequest\n\n");

        URL aURL = null;
        try {
             aURL = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // Extract the URL and port of remote 
		//String url = urlString.substring(7);
        //String url = urlString;
		//String pieces[] = url.split(":");
		//url = pieces[0];
		//int port  = Integer.valueOf(pieces[1]);
        int port = aURL.getPort();

		try{
			// Only first line of HTTPS request has been read at this point (CONNECT *)
			// Read (and throw away) the rest of the initial data on the stream
			String str="";

			for(int i=0;i<5 ;i++){
				String tmp = proxyToClientBr.readLine();
				str += tmp;
				//System.out.println(str);
				str += "\r\n";
				if(tmp.equals("")){
					break;
				}
			}
            System.out.println(str);
			urlString = "CONNECT "+ urlString;


            /*Global.imageCounter++;
            Encoder.byte2Image(str.getBytes(), str.getBytes().length,Integer.toString(Global.imageCounter)+"-C");*/

            //Global.imageCounter++;
            //Encoder.byte2Image(urlString.getBytes(), urlString.getBytes().length,Integer.toString(Global.imageCounter)+"-C");
            //ToDo


			// Get actual IP associated with this URL through DNS
			//InetAddress address = InetAddress.getByName(url);
			
			// Open a socket to the remote server 
			//Socket proxyToServerSocket = new Socket(address, port);
			//proxyToServerSocket.setSoTimeout(5000);


			// Send Connection established to the client
			String line = "HTTP/1.0 200 Connection established\r\n" +
					"Proxy-Agent: ProxyServer/1.0\r\n" +
					"\r\n";
			proxyToClientBw.write(line);
			proxyToClientBw.flush();
            httpsUrl = urlString+endMessageString;
			
			
			
			// Client and Remote will both start sending data to proxy at this point
			// Proxy needs to asynchronously read data from each party and send it to the other party


			//Create a Buffered Writer betwen proxy and remote
			//BufferedWriter proxyToServerBW = new BufferedWriter(new OutputStreamWriter(proxyToServerSocket.getOutputStream()));

			// Create Buffered Reader from proxy and remote
			//BufferedReader proxyToServerBR = new BufferedReader(new InputStreamReader(proxyToServerSocket.getInputStream()));



			// Create a new thread to listen to client and transmit to server
			ClientToServerHttpsTransmit clientToServerHttps = new ClientToServerHttpsTransmit(clientSocket.getInputStream());
			
			httpsClientToServer = new Thread(clientToServerHttps);
            TimeUnit.MILLISECONDS.sleep(500);
			httpsClientToServer.start();
			
			
			// Listen to remote server and relay to client
			try {
				byte[] buffer = new byte[10350];
				int read;
				//Client.runProxy(this.clientSocket);
				do {
                    Client.runProxy(this.clientSocket);
					read = Client.clientBuffer.length();
					//if(read==0) read=-1;
					if (read > 0) {
						clientSocket.getOutputStream().write(Client.clientBufferByte, 0, read);
						Client.clientBuffer="";
                        System.out.println("Response Sent To Client");
						/*if (proxyToServerSocket.getInputStream().available() < 1) {
							clientSocket.getOutputStream().flush();
						}*/
					}
				} while (read >= 0);
			}
			catch (SocketTimeoutException e) {
				
			}
			catch (IOException e) {
				e.printStackTrace();
			}


			// Close Down Resources
			/*if(proxyToServerSocket != null){
				proxyToServerSocket.close();
			}

			if(proxyToServerBR != null){
				proxyToServerBR.close();
			}

			if(proxyToServerBW != null){
				proxyToServerBW.close();
			}*/

			/*if(proxyToClientBw != null){
				proxyToClientBw.close();
			}*/
			
			
		} catch (SocketTimeoutException e) {
			String line = "HTTP/1.0 504 Timeout Occured after 10s\n" +
					"User-Agent: ProxyServer/1.0\n" +
					"\r\n";
			try{
				proxyToClientBw.write(line);
				proxyToClientBw.flush();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} 
		catch (Exception e){
			System.out.println("Error on HTTPS : " + urlString );
			e.printStackTrace();
		}
	}

	


	/**
	 * Listen to data from client and transmits it to server.
	 * This is done on a separate thread as must be done 
	 * asynchronously to reading data from server and transmitting 
	 * that data to the client. 
	 */
	class ClientToServerHttpsTransmit implements Runnable {
		
		InputStream proxyToClientIS;
		OutputStream proxyToServerOS;
		
		/**
		 * Creates Object to Listen to Client and Transmit that data to the server
		 * @param proxyToClientIS Stream that proxy uses to receive data from client
		 * @param proxyToServerOS Stream that proxy uses to transmit data to remote server
		 */
		public ClientToServerHttpsTransmit(InputStream proxyToClientIS) {
			this.proxyToClientIS = proxyToClientIS;
			//this.proxyToServerOS = proxyToServerOS;
			System.out.println("ClientToServerHttpsTransmit\n\n");
		}

		@Override
		public void run(){
			try {
				System.out.println("Client To Server RUN\n\n");
				// Read byte by byte from client and send directly to server
				byte[] buffer = new byte[10350];
				int read;
				do {
                    System.out.println("READ: Read byte by byte from client and send directly to server");
					read = proxyToClientIS.read(buffer);
                    //System.out.println("Client To Server RUN-inside while loop\n\n");
                    System.out.println("READ= "+ read);
					if (read > 0) {
                        byte[] httpsRequestByte = new byte[10350];
					    //byte[] tmp = new byte[httpsUrl.getBytes().length + ]
                        System.arraycopy(httpsUrl.getBytes(), 0, httpsRequestByte, 0, httpsUrl.getBytes().length);
                        System.arraycopy(buffer, 0, httpsRequestByte, httpsUrl.getBytes().length, read);
                        System.arraycopy(endMessageString.getBytes(), 0, httpsRequestByte, httpsUrl.getBytes().length+read, endMessageString.getBytes().length);

                        //ToDo add endmessage
                        System.out.println();
                        Global.imageCounter++;
                        System.out.println("Sending anothere one to server. imageName= "+ Integer.toString(Global.imageCounter));
                        Encoder.byte2Image(httpsRequestByte, read+httpsUrl.getBytes().length+endMessageString.getBytes().length,Integer.toString(Global.imageCounter)+"-C");
						//proxyToServerOS.write(buffer, 0, read);
						//System.out.println("ClientToServer: "+ new String(buffer)+"\n\n\n");
						/*if (proxyToClientIS.available() < 1) {
							proxyToServerOS.flush();
						}*/
					}
				} while (read >= 0);
			}
			catch (SocketTimeoutException ste) {
				// TODO: handle exception
			}
			catch (IOException e) {
				System.out.println("Proxy to client HTTPS read timed out");
				e.printStackTrace();
			}
			}
	}



}




