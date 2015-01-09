import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;


public class client implements Runnable {

	static String hostname = null;
	
	static String fileName = null;
	final int windowSize = 7;
	private static int Sf = 0;
	private static int Sn = 0;
	// use a list to cache the packets sent but unACKed
	private static LinkedList<packet> cache = new LinkedList<packet>();
	Timer timer = new Timer();
	static int out =0;
	int delay = 1000;
	static packet send_packet= null;
	static packet recievePacket = null;
	//flag to check if all the acks are recieved
	static boolean flag = false;
	static File file_seq = null;
	static File file_ack=null;
	//flag to check if the last payload is sent
	static boolean eot_flag = false;
	static FileInputStream file = null;
	static DatagramPacket datapacket = null;
	static String port_recieving = null;
	static String port_sending = null;
	static InetAddress ip = null;
	static DatagramSocket ack_clientSocket = null ;
	static DatagramSocket dataSocket = null;

	String payLoad = "";
	
	// main method : It expects four parameters : hostname, server port
	// number, reciever port number and file to be transffered 
	
	public static void main(String[] args) {
		if(args.length>1)
		{
			hostname 	= args[0];
			port_sending 	= args[1];
			port_recieving = args[2];
			fileName	= args[3];
		}
		try {
			ack_clientSocket = new DatagramSocket(Integer.parseInt(port_recieving));
			dataSocket = new DatagramSocket();
			ip = InetAddress.getByName(hostname);
			file = new FileInputStream(fileName);
			//file for logging in the sequence numbers
			file_seq = new File("seqnum.log");
			if(file_seq.exists())
			{
				file_seq.delete();
			}
			//file for logging in the acknowledgement numbers
			file_ack = new File("ack.log");
			if(file_ack.exists())
			{
				file_ack.delete();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Socket Closed");
		} 
		
		client client = new client();
		client.sending( hostname, port_sending, fileName );
	

	}
	//method for sending the data file across to the server
	public synchronized void sending(String hostname, String port_sending, String fileName) {

		try {

			String fileContent = getFileContent(file);
			//The below while loop goes through the file contents, split it
			//into chunks of 30 bytes and send it across the UDP port.
			while(fileContent.length()>0)
			{

				if(fileContent.length()>30)
				{
					payLoad = fileContent.substring(0, 30);
					fileContent = fileContent.substring(30);

				}

				// else loop : For the last payload.
				else
				{
					payLoad = fileContent.substring(0, fileContent.length());
					fileContent = "";
					eot_flag = true;

				}
				//check if the window is full
				if(Sn  <= Sf + windowSize)
				{


					send_packet = new packet(1,Sn,payLoad.length(),payLoad);

					byte sendBuf[] = seralize(send_packet);

					//Sending the payload. 
					datapacket = new DatagramPacket(sendBuf, sendBuf.length, ip, Integer.parseInt(port_sending));

					dataSocket.send(datapacket);
					//adding the sent packet to the cache
					cache.add(send_packet);

					send_packet.printContents();
					System.out.println("Sn"+Sn);
					System.out.println("Sf"+Sf);
						
					out = (Sn - Sf);
					
					System.out.println("Number of outstanding packets"+out);
	
					writeSeqLog(Sn);	
					
					//start the timer when the packet is sent

					if(Sf == Sn) {
						timer.schedule(new Timeout(), delay);

					}
					// do mod 8
					Sn = (Sn + 1) % 8;
					
				}
		
				
				new Thread(new client()).start();
				Thread.sleep(1000);


			}
			//for sending the EOT
			if(flag = true)
			{
				

				packet p_eot = new packet(3,Sn,0,"eot");
			
				byte[] sendBuf1 = seralize(p_eot);

				//Sending the  eot payload. 
				datapacket = new DatagramPacket(sendBuf1, sendBuf1.length, ip, Integer.parseInt(port_sending));
				dataSocket.send(datapacket);

				dataSocket = new DatagramSocket();
				writeSeqLog(Sn);
				p_eot.printContents();
				dataSocket.close();
				
			}
			new Thread(new client()).start();
			Thread.sleep(1000);

		}
		catch(Exception e)
		{
			System.out.println("Exception");
		}
		finally{

			try {

				ack_clientSocket.close();
				file.close();
				System.exit(1);

			} catch (Exception e) {
				System.out.println("Socket Closed");
				
			}
		}


	}
	//method to serialize the packet to byte stream
	public byte[] seralize(packet p)
	{
		byte[]	sendBuf = null;
		try
		{
		ByteArrayOutputStream	oSt	=	new	ByteArrayOutputStream();	
		ObjectOutputStream	ooSt	=	new	ObjectOutputStream(new BufferedOutputStream(oSt));																										
		ooSt.flush();
		ooSt.writeObject(p);	
		ooSt.flush();	

		sendBuf = oSt.toByteArray();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return sendBuf;
		
	}
	//method to write the sequence numbers to the log file
	public void writeSeqLog(int seqNum)
	{
		try
		{
		FileWriter fw = new FileWriter(file_seq,true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(String.valueOf(seqNum));
		 
		bw.newLine();
		bw.close();
		fw.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//method to write the acknowledgement numbers to log file
	public void writeAckLog(int ackNum)
	{
		try
		{
			FileWriter fw1 = new FileWriter(file_ack,true);
			BufferedWriter bw1 = new BufferedWriter(fw1);
			bw1.write(String.valueOf(ackNum));
			bw1.newLine();
			bw1.close();
			fw1.close();
			
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
//method to get the file content as a string
	public String getFileContent( FileInputStream fis ) {

		StringBuilder sb = new StringBuilder();
		try {
			Reader r = new InputStreamReader(fis, "UTF-8");  
			int ch = r.read();
			while(ch >= 0) {
				sb.append((char)ch);
				ch = r.read();
			}
		} catch (Exception e) {

			System.out.println(" Caught exception while trying to read the file content in getFileContent method : " + e.getMessage());
			e.printStackTrace();

		}
		return sb.toString();
	}

	class Timeout extends TimerTask {	
		public void run() {
			// Restart timer
			timer.schedule(new Timeout(), delay);
				if(Sf != Sn && flag != true)
				{
				
            //restransmit the packets when an acknowledgement is not
				//recieved
				for(int i = 0; i <= cache.size(); i++) {
				try {
		
			packet rep = new packet(1,cache.get(i).getSeqNum(),cache.get(i).getData().length(),cache.get(i).getData());
			System.out.println(Sf);
			ByteArrayOutputStream	oSt	=	new	ByteArrayOutputStream();	
			ObjectOutputStream	ooSt	=	new	ObjectOutputStream(new BufferedOutputStream(oSt));																										
			ooSt.flush();
			ooSt.writeObject(rep);	
			ooSt.flush();	

			byte[]	sendBuf = oSt.toByteArray();

			//Sending the payload. 
			datapacket = new DatagramPacket(sendBuf, sendBuf.length, ip, Integer.parseInt(port_sending));

			dataSocket.send(datapacket);


			rep.printContents();
			System.out.println("Sn"+Sn);
			System.out.println("Sf"+Sf);
			int out = (Sn - Sf);
			System.out.println("Number of outstanding packets"+out);
			Thread.sleep(1000);
			} catch (Exception e) {
			// TODO
			}
			}
			
			}
			
		}
	}
	//deserialize the byte stream to obtain packet
	public packet getPacket(byte[] recieveData)
	{
		try
		{
		ByteArrayInputStream abaos = new ByteArrayInputStream(recieveData);
		ObjectInputStream aoos = new ObjectInputStream (new BufferedInputStream(abaos));
		recievePacket = (packet)aoos.readObject();
		recievePacket.printContents();
		
		
	  }
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return recievePacket;
	}
	//method that recieves the acknowledgement
	@Override
		public synchronized void run() {
			// TODO Auto-generated method stub
			try
			{
				byte receiveAckData[] = new byte[1024];
				DatagramPacket ackPacket = new DatagramPacket(receiveAckData, receiveAckData.length) ;
				//recieving the packet
				ack_clientSocket.receive(ackPacket);
				recievePacket = getPacket(receiveAckData);
				int ackSeqNum = recievePacket.getSeqNum();
				writeAckLog(ackSeqNum);
				
					
					timer.cancel();
					Sf = (Sf +1)% 8  ;
					if(Sf == Sn)
					{
                  //remove the packet from the cache which has recieved the
						//acknowledgement
						cache.removeFirst();
					}

					
				
				if(eot_flag == true)
				{
				//set EOT flag when all the acks have been recieved	
					flag = true;

				}
			}
			catch (IOException e) {
				System.out.println(e);
			} 
		
		}
  }






