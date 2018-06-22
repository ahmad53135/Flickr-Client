/*
 * 	Student:		Stefano Lupo
 *  Student No:		14334933
 *  Degree:			JS Computer Engineering
 *  Course: 		3D3 Computer Networks
 *  Date:			02/04/2017
 */

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.Transport;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


/**
 * The Proxy creates a Server Socket which will wait for connections on the specified port.
 * Once a connection arrives and a socket is accepted, the Proxy creates a RequestHandler object
 * on a new thread and passes the socket to it to be handled.
 * This allows the Proxy to continue accept further connections while others are being handled.
 * 
 * The Proxy class is also responsible for providing the dynamic management of the proxy through the console 
 * and is run on a separate thread in order to not interrupt the acceptance of socket connections.
 * This allows the administrator to dynamically block web sites in real time. 
 * 
 * The Proxy server is also responsible for maintaining cached copies of the any websites that are requested by
 * clients and this includes the HTML markup, images, css and js files associated with each webpage.
 * 
 * Upon closing the proxy server, the HashMaps which hold cached items and blocked sites are serialized and
 * written to a file and are loaded back in when the proxy is started once more, meaning that cached and blocked
 * sites are maintained.
 *
 */
public class Proxy implements Runnable {


	// Main method for the program
	public static void main(String[] args) {
		// Create an instance of Proxy and begin listening for connections
		Proxy myProxy = new Proxy(8415);
		myProxy.listen();	
	}


	private ServerSocket serverSocket;

	/**
	 * Semaphore for Proxy and Consolee Management System.
	 */
	private volatile boolean running = true;



	static ArrayList<Thread> servicingThreads;
    static List<String> serverResponse = new ArrayList<>();



	/**
	 * Create the Proxy Server
	 * @param port Port number to run proxy server from.
	 */
	public Proxy(int port) {

		// Create array list to hold servicing threads
		servicingThreads = new ArrayList<>();

		// Start dynamic manager on a separate thread.
		new Thread(this).start();	// Starts overriden run() method at bottom

		try {
			// Create the Server Socket for the Proxy 
			serverSocket = new ServerSocket(port);

			// Set the timeout
			//serverSocket.setSoTimeout(100000);	// debug
			System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "..");
			running = true;
		} 

		// Catch exceptions associated with opening socket
		catch (SocketException se) {
			System.out.println("Socket Exception when connecting to client");
			se.printStackTrace();
		}
		catch (SocketTimeoutException ste) {
			System.out.println("Timeout occured while connecting to client");
		} 
		catch (IOException io) {
			System.out.println("IO exception when connecting to client");
		}
	}


	/**
	 * Listens to port and accepts new socket connections. 
	 * Creates a new thread to handle the request and passes it the socket connection and continues listening.
	 */
	public void listen(){

		while(running){
			try {
                System.out.println("running 1111111111111111111111111111111111");
				// serverSocket.accpet() Blocks until a connection is made
				Socket socket = serverSocket.accept();
				
				// Create new Thread and pass it Runnable RequestHandler
				Thread thread = new Thread(new RequestHandler(socket));
				
				// Key a reference to each thread so they can be joined later if necessary
				servicingThreads.add(thread);
				
				thread.start();

				///////////////////////////////////

/*
                //final long[] waitingTime = {0};

                Transport tr = new REST();

                Flickr f = new Flickr(Client.apiKey, Client.sharedSecret, tr);
                int exitFlag = 0;
                String Response = "";
                ArrayList<InputStream> photoInputStreams = new ArrayList<>();
                ArrayList<Photo> photoArrayList = new ArrayList<>();
                while (exitFlag == 0) {
                    try {


                        //f.getPhotosInterface().delete("36693122640");

                        PhotoList list = f.getPhotosetsInterface().getPhotos(Client.photoSetID, Client.picsPerPage, Client.numberOfPages);


                        for (Iterator iterator = list.iterator(); iterator.hasNext(); ) {
                            //streamToClient = client.getOutputStream();
                            Photo photo = (Photo) iterator.next();

                            File file2 = null;

                            if (photo.getTitle().contains(Integer.toString(Global.imageCounter) + "-S") && serverResponse.contains(photo.getTitle()) == false) {
                                photoArrayList.add(photo);
                                file2 = new File(Client.downloadedImagesFromServer + photo.getTitle() + "---.jpg");
                                //System.out.println("ImageAvailable=" + Long.toString(System.currentTimeMillis() - waitingTime[0]));
                                serverResponse.add(photo.getTitle());
                                if (photo.getTitle().contains("Flush")) {
                                    System.out.println("--------" + photo.getTitle() + "------------");

                                    String[] parts = photo.getTitle().split("-");
                                    int totalNumImages = Integer.parseInt(parts[2]);

                                    if(photoArrayList.size() >= totalNumImages){                                     ////need to be tunded, right now based on starting from 0
                                        exitFlag = 1;
                                    }

                                }
                            } else {
                                continue;
                            }

                        }


                    } catch (Exception e) {
                        System.out.println("Exp 1");
                    }
                }

                if (exitFlag == 1) {
                    for(int i=0; i< photoArrayList.size(); i++){
                        String photoTitle = photoArrayList.get(i).getTitle();
                        String[] parts = photoTitle.split("-");
                        int index = Integer.parseInt(parts[2]);
                        PhotosInterface photosInterface = new PhotosInterface(Client.apiKey, Client.sharedSecret, tr);
                        InputStream inputStream = null;
                        try {
                            inputStream = photosInterface.getImageAsStream(photoArrayList.get(i), 5);
                        } catch (FlickrException e) {
                            e.printStackTrace();
                        }
                        photoInputStreams.add(index,inputStream);
                    }
                    byte[] msg=new byte[10350];
                    for(int i=0; i<photoInputStreams.size(); i++){
                        ByteArrayOutputStream b = new ByteArrayOutputStream();
                        byte[] bytes = IOUtils.toByteArray(photoInputStreams.get(i));
                        b.write(bytes);
                        File file2 = new File(Client.downloadedImagesFromServer + photoArrayList.get(i).getTitle() + "-.-.jpg");;
                        FileUtils.writeByteArrayToFile(file2, b.toByteArray());
                        //
                        // String msg = Decoder.image2Byte(file2.getPath());
                        msg = Decoder.image2Byte(file2.getPath());
                        String tmp = new String(msg,StandardCharsets.ISO_8859_1);
                        //Response = Response + tmp;
                        Response = tmp;


                        //ToDo belows
                        // Create new Thread and pass it Runnable RequestHandler
                        thread = new Thread(new RequestHandler(socket));

                        // Key a reference to each thread so they can be joined later if necessary
                        servicingThreads.add(thread);

                        thread.start();
                    }
                    //TimeUnit.SECONDS.sleep(10);



                    int newLinePos = Response.indexOf(Client.endMessageString);
                    Client.clientBuffer = Response.substring(0,newLinePos);
                    //int tt =


                    Client.clientBufferByte = new byte[newLinePos];
                    System.arraycopy(msg, 0, Client.clientBufferByte, 0, newLinePos);
                    exitFlag=0;
                    //return;
                }
                System.out.println("$$$$$$$$$$$$$$$$$Close StreamToClient$$$$$$$$$$");

                */
                ///////////////////////////////////



			} catch (SocketException e) {
				// Socket exception is triggered by management system to shut down the proxy 
				System.out.println("Server closed");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Saves the blocked and cached sites to a file so they can be re loaded at a later time.
	 * Also joins all of  RequestHandler threads currently servicing requests.
	 */
	private void closeServer(){
		/*System.out.println("\nClosing Server..");
		running = false;

		try{
			// Close all servicing threads
			for(Thread thread : servicingThreads){
				if(thread.isAlive()){
					System.out.print("Waiting on "+  thread.getId()+" to close..");
					thread.join();
					System.out.println(" closed");
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Close Server Socket
			try{
				System.out.println("Terminating Connection");
				serverSocket.close();
			} catch (Exception e) {
				System.out.println("Exception closing proxy's server socket");
				e.printStackTrace();
			}

*/		}



		@Override
		public void run() {
			/*Scanner scanner = new Scanner(System.in);

			String command;
			while(running){
				System.out.println("Enter new site to block, or type \"blocked\" to see blocked sites, \"cached\" to see cached sites, or \"close\" to close server.");
				command = scanner.nextLine();

				if(command.equals("close")){
					running = false;
					closeServer();
				}
			}
			scanner.close();
		*/}

	}
