
import java.net.*;
import java.io.*;

public class server { 

	static  String IPadd = null;
	static String receivePort  = null;
	static  String sendPort	  = null;
	static  String outputfile	  = null;
	static int seqnum = 0;
	static packet recievePacket = null;
	static packet sendPacket = null;
	static DatagramSocket recieveSocket = null;
	static DatagramSocket sendSocket = null;
//main method which takes hostname, revcieve port , sending port and the
//output file as the arguments	
	public static void main(String[] args) {
	
	  
	  if(args.length!=0){
		  IPadd 	   = args[0];
		  receivePort  = args[1];
		  sendPort	   = args[2];
		  outputfile    = args[3];		  
	  }
	  
	  recieve(IPadd, receivePort, sendPort, outputfile);
	  
	  
  }
	//method to recieve packets from the client
  public static void recieve( String IPadd,String receivePort, String sendPort, String outputfile )
  {
	  try {
		  	recieveSocket = new DatagramSocket(Integer.parseInt(receivePort));
			//file to record to arrival sequence number of packets
			File file = new File("arrival.log");
			if(file.exists())
			{
				file.delete();
			}
		
			//file that would have the data transferred
			File file_output = new File(outputfile);
			if(file_output.exists())
			{
				file_output.delete();
			}
		
		
			System.out.println("Waiting for client on port " + receivePort + "..."); 
				
				while(true)
				{
					 sendSocket = new DatagramSocket();
					byte[] receiveClientData = new byte[1024];
					DatagramPacket recieveClientPacket = new DatagramPacket(receiveClientData, receiveClientData.length);
					//recieves packet
					recieveSocket.receive(recieveClientPacket);
						//deserializes byte array to obtain packet
						ByteArrayInputStream baos = new ByteArrayInputStream(receiveClientData);
						ObjectInputStream oos = new ObjectInputStream (new BufferedInputStream(baos));
						recievePacket = (packet)oos.readObject();
				            
						System.out.println("Expected Rn: "+ seqnum);
						recievePacket.printContents();
						//log the recieved packets sequence numbers to the
						//arrival.log file
						FileWriter fw = new FileWriter(file, true);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(String.valueOf(recievePacket.getSeqNum()));
						bw.newLine();
						bw.close();
						fw.close();			
						InetAddress ipAdd 	= InetAddress.getByName(IPadd);
												
						byte[] sendBuf = null;
						//check if the type is not an EOT
						if(recievePacket.getType()!=3){
							//write the recieved data to an output file
							FileWriter fileWritter = new FileWriter(file_output.getName(),true);
							BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
							bufferWritter.write(recievePacket.getData());
							bufferWritter.close();
							fileWritter.close();
							//check if the recieved packet's sequence number is the
							//expected value
							if(seqnum == recievePacket.getSeqNum()){
							
								 seqnum = (seqnum +1) % 8;
								    
							
							sendPacket = new packet(0, seqnum, 0,recievePacket.getData().toUpperCase()); 
							sendBuf = serialize(sendPacket);
							//send the acknowledgement
							DatagramPacket ackpacket = new DatagramPacket(sendBuf, sendBuf.length, ipAdd, Integer.parseInt(sendPort));
							sendSocket.send(ackpacket);
							sendPacket.printContents();
							}	
							}
						else{
								//send an acknowledgement to the EOT
								seqnum = (seqnum +1) % 8;
								packet p3 = new packet(2,seqnum,0,"EOT");
								sendBuf = serialize(p3);
								DatagramPacket ackpacket = new DatagramPacket(sendBuf, sendBuf.length, ipAdd, Integer.parseInt(sendPort));
								 sendSocket.send(ackpacket);
								 p3.printContents();
								recieveSocket.close();
								sendSocket.close();	
								
						}
				  
				}
				
			
			}
	
	  	catch(Exception e)
	  	{
			System.out.println("Sockets closed, exiting");
	  	}
	  
  }
  //method to serialize the byte array to packet
  public static byte[] serialize (packet recieve)
  {
	  byte[] sendBuf = null;
  
	  try{
	  ByteArrayOutputStream oSt = new ByteArrayOutputStream();
      ObjectOutputStream ooSt = new ObjectOutputStream(new BufferedOutputStream(oSt));
      ooSt.flush();
      ooSt.writeObject(recieve); 
      ooSt.flush();        
      sendBuf = oSt.toByteArray();
  }
	  catch(Exception e)
	  {
		  e.printStackTrace();
	  }

return sendBuf;
}
}
