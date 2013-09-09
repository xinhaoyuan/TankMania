package tankmania.client;

import tankmania.common.*;

import java.net.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;

public class TMClient implements Runnable
{
	 private Reader mInput;
	 private BlockingQueue<String> mMessages;
	 private Thread mThread;
	 private TMMonitor mMonitor;
	 private SynchronousQueue<Integer> mLock;
	 
	 public TMClient(BlockingQueue<String> messages, TMMonitor monitor) {
		  mInput = null;
		  mMessages = messages;
		  mThread = null;
		  mMonitor = monitor;
		  mLock = new SynchronousQueue<Integer> ();
	 }

	 public void getReady() {
		  try
		  {
			   mLock.take();
		  }
		  catch (Exception e)
		  {
		  }
	 }

	 public void run() {
		  BufferedReader input = new BufferedReader(mInput);
		  while (true)
		  {
			   StringTokenizer tokens;
			   String line;
			   try
			   {
					line = input.readLine();
					tokens = new StringTokenizer(line);
					String timeStr = tokens.nextToken();
					String cmdHeaderStr = tokens.nextToken();
					
					if (cmdHeaderStr.equals("sync"))
					{
						 int oldTime = Integer.decode(tokens.nextToken());
						 mMonitor.mTimeStart += oldTime - Integer.decode(timeStr);
						 continue;
					}
					else if (cmdHeaderStr.equals("set-id"))
					{
						 int id = Integer.decode(tokens.nextToken());
						 mMonitor.setSelfId(id);
						 continue;
					}
					else if (cmdHeaderStr.equals("load"))
					{
						 String filename = tokens.nextToken();
						 mMonitor.mMap.loadFromFile(new File(filename));

						 mLock.put(new Integer(0));
						 continue;
					}


					mMessages.put(line);
			   }
			   catch (Exception e)
			   {
					break;
			   }
		  }
	 }
	 
	 public void start(Reader input) {
		  mInput = input;
		  mThread = new Thread(this);
		  mThread.start();
	 }
}
