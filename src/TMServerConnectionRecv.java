package tankmania.server;

import tankmania.common.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class TMServerConnectionRecv implements Runnable
{
	 BlockingQueue<String> mMessages;
	 BufferedReader mInput;
	 Socket mConn;
	 TMServer mServer;
	 
	 public TMServerConnectionRecv(TMServer server, BlockingQueue<String> messages, Reader input, Socket conn) {
		  mServer = server;
		  mMessages = messages;
		  mInput = new BufferedReader(input);
		  mConn = conn;
	 }

	 public void run() {
		  while (true)
		  {
			   String line;
			   try
			   {
					line = mInput.readLine();
					if (line == null) throw new IOException();
			   }
			   catch (IOException ex)
			   {
					try
					{
						 mMessages.put("end-connection");
						 mConn.close();
					}
					catch (Exception exx)
					{
					}
					break;
			   }

			   try
			   {
					mMessages.put(mServer.getTime() + " " + line);
			   }
			   catch (InterruptedException ex)
			   {
			   }
		  }
	 }
}
