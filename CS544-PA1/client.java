/*
 * Author  		: Kaveri Krishnaraj
 * Drexel ID	: 13053051
 * Email		: kk698@drexel.edu
 * Title		: Client.java
 * Purpose		: CS544 Programming Assignment 1.
 * Professor	: Dr Maxwell M R Young
 * Date			: 04/29/2014
 *
 * This java class implements a Client that connects to the Server running on port #6124.
 * Once it receives a random port number from Server, it initiates a file transfer over the random part using UDP protocol.
 *
 */
import java.net.*;
import java.io.*;

public class client {
	static int port;
	private static BufferedReader bread;
	static String serverName;
	static String fileName = null;
   static File file = null;
	public static void main(String [] args) throws Exception
	{
	if (args.length > 0) 
		{
			serverName = args[0];
			port = Integer.parseInt(args[1]);
			fileName = args[2];
			file = new File(fileName);
		   	
		}

	      Socket client ;
		//Get the IP address for UDP
	     InetAddress IPAddress = InetAddress.getByName(serverName); 
	     InputStream is = null; 
		    InputStreamReader isr = null;
		    BufferedReader br = null;
	
	      try
	      {
	         System.out.println("Connecting to " + serverName
	           						+ " on port " + port);
				//opening a tcp socket
	         client = new Socket(serverName, port);
				//open an output stream to the socket
	         OutputStream output = client.getOutputStream();
	         //open a data output stream to send data over the output stream
				DataOutputStream out =
	                       new DataOutputStream(output);
				//client sends an integer to initiate the negotiation
	         out.write(10);
				//open an input stream to the socket
	         InputStream input = client.getInputStream();
				//open a data input stream to recieve data over the input stream
	         DataInputStream in =
	                        new DataInputStream(input);
				//Recieve the random port from the server
	         int serverPort=in.readInt();
	         System.out.println("Server reponds with random port " + serverPort);
	         //close the tcp socket
				client.close();
	          //open a UDP socket
	          DatagramSocket clientSocket = new DatagramSocket();
	         
	          is = new FileInputStream(file);
				 
	          isr = new InputStreamReader(is);
	           
	          br = new BufferedReader(isr);
	        
	        	   
	               
	        	   String line = null;
					//byte array with the data to be sent
	        	   byte[] sendByte = new byte[16];
	        	   while((line = br.readLine())!=null)
	        	      {
	        	    	  for(int i=0;i<line.length();i+=16)
	        	    	  {
	        	    	  //byte array with the data recieved
	        	        byte[] receiveByte = new byte[16];

	        	        int b = 0;
						  //end of file is reached
	        	        if(i+16 > line.length())
	        	         
	        	        {
							//split the part of the line from the last index till
							//the end of the line  
	        	        	 b = line.length()-i;
							sendByte = line.substring(i, i+b).getBytes();
					     }
						else						 
	        	     	  sendByte = line.substring(i, i+16).getBytes();
	        	        
	        	      
	        	   //create a packet to be sent over UDP
	        	   DatagramPacket sendPacket = new DatagramPacket(sendByte,sendByte.length, IPAddress,serverPort);
	            //send the packet  
	            clientSocket.send(sendPacket);
	            //recieve a packet over UDP  
	        	   DatagramPacket receivePacket = new DatagramPacket(receiveByte, receiveByte.length);
		           clientSocket.receive(receivePacket);
		           String recievedChunk = new String(receivePacket.getData());
		       
		           System.out.println("Client Recieved:" + recievedChunk);  
	        	    	  
	        	    	  
	        	      }
	        	  
	               
	          
	         }
					//send an extra packet to indicate the end of file
	        	   String k = "FFFFFFFFFFFFFFFF";
		       	    
	        	   sendByte = k.getBytes();
	        	   
	        	   
	        	   
	        	   DatagramPacket sendPacket = new DatagramPacket(sendByte, sendByte.length, IPAddress,serverPort);
		              
		             
	               clientSocket.send(sendPacket);
	               clientSocket.close();
	               
	      }   
	     
	           
	       
	 
	      catch(IOException e)
	      {
	         e.printStackTrace();
	      }
	   }
}
	
