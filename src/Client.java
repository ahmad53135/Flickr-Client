import com.flickr4java.flickr.*;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Client {

    public static String apiKey ="a4b2a5ed04bb6bee75ba4d989d175d22";
    public static String sharedSecret = "a1fe6dce56b3404c";
    public static String photoSetID = "72157695761376345";
    public static String userID = "156791166@N05";
    public static int numberOfPages = 1;
    public static int picsPerPage = 1000;
    public static int maxBytePerImage = 10350;

    public static int localport = 8415;

    public static String downloadedImagesFromServer = "./Image/serverImages/";
    public static String uploadedImagesFromClient = "./Image/clientImages/";

    public static byte[] clientBufferByte;
    public static String endMessageString = "\r\r\n\n";
    public static String clientBuffer="";

    static List<String> serverResponse = new ArrayList<>();


    public Client(){

    }


    public static void main(String[] args) {



        try {
            //Encoder.load2List();//---------------------------------------------------

        } catch (Exception e) {
            System.err.println(e);
        }


            // Create an instance of Proxy and begin listening for connections
            Proxy myProxy = new Proxy(8415);
            myProxy.listen();


        /*String host = "127.0.0.1";
        int remoteport =9090;
        int localport = Client.localport;
        // Print a start-up message
        System.out.println("Starting proxy for " + host + ":" + remoteport
                + " on port " + localport);
        try {
            // And start running the server
            //runProxy(); // never returns
        }catch (Exception e){

        }*/


    }



    public static void runProxy(Socket client)           //change name from runServer to runProxy
            throws IOException {                                                            // runServer is used in Server class
        // Create a ServerSocket to listen for connections with

                //ServerSocket ss = new ServerSocket(localport);--------------------------


        final byte[] request = new byte[((Global.imageheight * Global.imageWidth / (8 * 8)) * Global.bitsPerBlock/8)];    //maximum size of an imag
        final byte[] totalRequest = new byte[50*request.length];

        //while (true) {

            //Socket client = null;


            try {
                //final OutputStream streamToClient = client.getOutputStream();
                final long[] waitingTime = {0};
                //final List<String> serverResponse = new ArrayList<>();

                        int exitFlag = 0;
                        String Response = "";
                        int bytesToCut = 0;
                ArrayList<InputStream> photoInputStreams = new ArrayList<>();
                ArrayList<Photo> photoArrayList = new ArrayList<>();
                        while (exitFlag == 0) {
                            try {

                                Transport tr = new REST();

                                Flickr f = new Flickr(Client.apiKey, Client.sharedSecret, tr);
                                //f.getPhotosInterface().delete("36693122640");

                                PhotoList list = f.getPhotosetsInterface().getPhotos(Client.photoSetID, Client.picsPerPage, Client.numberOfPages);


                                for (Iterator iterator = list.iterator(); iterator.hasNext(); ) {
                                    //streamToClient = client.getOutputStream();
                                    Photo photo = (Photo) iterator.next();

                                    File file2 = null;

                                    //if (photo.getTitle().contains(Integer.toString(Global.imageCounter) + "-S") && serverResponse.contains(photo.getTitle()) == false && serverResponse.contains(photo.getId())) {
                                    if (photo.getTitle().contains(Integer.toString(Global.imageCounter) + "-S") && serverResponse.contains(photo.getId()) == false) {
                                        photoArrayList.add(photo);
                                        file2 = new File(downloadedImagesFromServer + photo.getTitle() + "---.jpg");
                                        System.out.println("ImageAvailable=" + Long.toString(System.currentTimeMillis() - waitingTime[0]));
                                        //serverResponse.add(photo.getTitle());
                                        serverResponse.add(photo.getId());
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
                                if (exitFlag == 1) {
                                    for(int i=0; i< photoArrayList.size(); i++){
                                        String photoTitle = photoArrayList.get(i).getTitle();
                                        String[] parts = photoTitle.split("-");
                                        int index = Integer.parseInt(parts[2]);
                                        PhotosInterface photosInterface = new PhotosInterface(Client.apiKey, Client.sharedSecret, tr);
                                        InputStream inputStream = photosInterface.getImageAsStream(photoArrayList.get(i), 5);
                                        photoInputStreams.add(index,inputStream);
                                    }
                                    byte[] msg=new byte[10350];
                                    for(int i=0; i<photoInputStreams.size(); i++){
                                        ByteArrayOutputStream b = new ByteArrayOutputStream();
                                        byte[] bytes = IOUtils.toByteArray(photoInputStreams.get(i));
                                        b.write(bytes);
                                        File file2 = new File(downloadedImagesFromServer + photoArrayList.get(i).getTitle() + "-.-.jpg");;
                                        FileUtils.writeByteArrayToFile(file2, b.toByteArray());
                                        //
                                        // String msg = Decoder.image2Byte(file2.getPath());
                                        msg = Decoder.image2Byte(file2.getPath());
                                        String tmp = new String(msg,StandardCharsets.ISO_8859_1);
                                        //Response = Response + tmp;
                                        Response = tmp;

                                    }
                                    //TimeUnit.SECONDS.sleep(10);
                                    //System.out.println(Response);


                                    int newLinePos = Response.indexOf(endMessageString);
                                    clientBuffer = Response.substring(0,newLinePos);
                                    //int tt =


                                    clientBufferByte = new byte[newLinePos];
                                    System.arraycopy(msg, 0, clientBufferByte, 0, newLinePos);
                                    int s=0;
                                    return;

                                    //streamToClient.write(clientBufferByte);
                                    //streamToClient.flush();


                                    //Response = Response.substring(0, bytesToCut);
                                    //streamToClient.write(Response.getBytes(), 0, Response.length());
                                }

                            } catch (Exception e) {
                                System.out.println("Exp 1");
                                e.printStackTrace();
                            }
                        }
                        System.out.println("$$$$$$$$$$$$$$$$$Close StreamToClient$$$$$$$$$$");

            } catch (Exception e) {
                System.err.println(e);
            } /*finally {
                try {
                    if (client != null)
                        System.out.println("%%%%%%%%Client Closed%%%%%%%%%%%%%");
                } catch (Exception e) {
                }
            }*/
        //}
    }


}
