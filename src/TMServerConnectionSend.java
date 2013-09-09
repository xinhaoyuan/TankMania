package tankmania.server;

import tankmania.common.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class TMServerConnectionSend implements Runnable
{
	 BlockingQueue<String> mMessages;
	 BufferedWriter mOutput;
	 Socket mConn;
	 
	 public TMServerConnectionSend(BlockingQueue<String> messages, Writer output, Socket conn) {
		  mMessages = messages;
		  mOutput = new BufferedWriter(output);
		  mConn = conn;
	 }
	 
	 public void run() {
		  int th = 0;
		  while (true)
		  {
			   String message;
			   try
			   {
					message = mMessages.take();
			   }
			   catch (InterruptedException ex)
			   {
					break;
			   }

			   if (message.equals("end-connection"))
			   {
					try
					{
						 mConn.close();
					}
					catch(Exception ex)
					{
					}
					break;
			   }

			   try
			   {
					mOutput.write(message);
					mOutput.newLine();
					++ th;
					if (mMessages.peek() == null || th > 50)
					{
						 mOutput.flush();
						 th = 0;
					}
			   }
			   catch (IOException ex)
			   {
					try
					{
						 mConn.close();
					}
					catch(Exception exx)
					{
					}
					break;
			   }
		  }
	 }
}
