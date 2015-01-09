/*
 * Author  		: Kaveri Krishnaraj
 * Drexel ID	: 13053051
 * Email		: kk698@drexel.edu
 * Title		: Client.java
 * Purpose		: CS544 Programming Assignment 1.
 * Professor	: Dr Maxwell M R Young
 * Date			: 04/29/2014
 *
 * This java class implements a Server that runs on port# 6124.
 * It waits for client to establish connection over TCP socket and accepts file from Client through UDP datagrams.
 *
 */
import java.net.*;
import java.util.Random;
import java.io.*;

public class server 
{
	
	static ServerSocket server;
	static Socket socket;
	static DatagramSocket  serverSocket;
	static int randomNum=0;

	
	
	 public static void main(String[] args) 
	    {
		 
		
    try {
    	   //open a new TCP server socket
         server = new ServerSocket(6001);
          server.setSoTimeout(10000);
          File file = new File("received.txt");
 		 FileOutputStream fop = new FileOutputStream(file);
     
			System.out.println("Waiting for client on port " + server.getLocalPort() + "..."); 
       while(true)
      {
			//server accepts the client's request
			socket = server.accept();
			//input stream to recieve the number sent by the client
			 DataInputStream in = new DataInputStream(socket.getInputStream());
			Random rand = new Random();
         //genenrate a random number between 6000 and 8000
			randomNum = rand.nextInt((8000 - 6000) + 1) + 6000;
			//output stream to send data over TCP to the client
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeInt(randomNum);
            System.out.println("Random Port" +randomNum);
            socket.close();
            //open a new UDP socket
             serverSocket = new DatagramSocket(randomNum);
               
        while(true)
        {
        	byte[] receiveByte = new byte[16];
            byte[] sendByte = new byte[16];
            byte[] writeBytes = new byte[16];
            
        
         //recieve a packet over UDP to the client
          DatagramPacket receivePacket = new DatagramPacket(receiveByte,receiveByte.length);
          serverSocket.receive(receivePacket);
          String data = new String( receivePacket.getData());
        	//compare each packet with the end of file	
          if(data.compareTo("FFFFFFFFFFFFFFFF") == 0)
          {
       	   //close socet if it's the end of file
       	   fop.close();
       	    serverSocket.close();  
       	  
          }
      	 if(data.compareTo("FFFFFFFFFFFFFFFF") != 0) 
			 		System.out.println(data);
              
              
              
  			// if file doesnt exists, then create it
  			if (!file.exists()) {
  				file.createNewFile();
  			}

  			// get the content in bytes
  			 
  			writeBytes = data.getBytes();
			//write to the file
  			fop.write(writeBytes);
  			  
  				//get the IP address from the client
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            String capitalLetters = data.toUpperCase();
            sendByte = capitalLetters.getBytes();
            
            DatagramPacket sendPacket = new DatagramPacket(sendByte,sendByte.length, IPAddress, port);
            serverSocket.send(sendPacket);
           
        	  
          
          
        	  
           }
       
      }
       
         
       
      
	     
       
      
    }
    
    catch(SocketTimeoutException s)
    {
       System.out.println("Socket timed out!");
    
    }
        catch (IOException e) {
           System.out.println("Socket Closed on " +randomNum);
        }

}
	    }

