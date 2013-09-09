package tankmania.client;

import tankmania.common.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.html.*;
import javax.swing.text.*;
import java.util.*;
import java.awt.image.*;
import javax.imageio.*;
import java.awt.geom.*;
import java.net.*;
import java.util.concurrent.*;

public class TMClientUI
{
    static class TMClientDisplay extends JPanel
    {
        TMMonitor mMonitor;
        TMClient mClient;
		  
        TMClientDisplay(TMMonitor monitor, TMClient client) {
            mMonitor = monitor;
            mClient = client;
        }
		  
        public void paintComponent(Graphics _g) {
            super.paintComponent(_g);
            Graphics2D g = (Graphics2D)_g;
            mMonitor.drawOnGraphics2D(g);
        }

        protected void clear(Graphics g) {
            super.paintComponent(g);
        }

        public int getHeight() {
            return mMonitor.mMap.getHeight();			   
        }

        public int getWidth() {
            return mMonitor.mMap.getWidth();
        }
    }
	 
    public static void runClient(String addrStr, String name, int port, int tankType, int gunType) {
        TMClientUI ui = new TMClientUI();
        
        InetAddress address;
        Socket conn;
		
        try
        {
            address = InetAddress.getByName(addrStr);
            conn = new Socket();
            conn.connect(new InetSocketAddress(address, port));
        }
        catch (Exception ex)
        {
            address = null;
            port = 0;
            conn = null;
            name = null;
            tankType = 0;
            gunType = 0;
			   
            System.out.println("Cannot connect to the address:port");
            System.exit(-1);
        }
		  
        try
        {
            ui.start(conn, name, tankType, gunType);
        }
        catch (Exception ex)
        {
            System.out.println("Catch exception " + ex + ", exiting");
            System.exit(-1);
        }
    }

    JFrame mFrame;
    JTextPane mMessageBox;
    TMMonitor mMonitor;
    TMClient mClient;
    TMClientDisplay mDisplay;
    javax.swing.Timer mDrawTimer;
    java.util.Timer mSyncTimer;
    BufferedWriter mOutput;
    Socket mConn;
    boolean mBorned;
    String mName;
    int mTankType;
    int mGunType;
	 
    void start(Socket conn, String name, int tankType, int gunType) {
        mName = name;
        mConn = conn;
        mTankType = tankType;
        mGunType = gunType;
		  
        BlockingQueue<String> messages = new LinkedBlockingQueue<String> ();
        mMonitor = new TMMonitor(messages);
        mClient = new TMClient(messages, mMonitor);
        try
        {
            mOutput = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            mClient.start(new InputStreamReader(conn.getInputStream()));
        }
        catch (IOException ex)
        {
            mOutput = null;
        }
		  
        try
        {
            mOutput.write("sync " + mMonitor.getCurrentTime());
            mOutput.newLine();
            mOutput.write("get-status");
            mOutput.newLine();
            mOutput.flush();
        }
        catch (IOException ex)
        {
        }

        mClient.getReady();
		  
        mFrame = new JFrame("Tank Mania Demo -- Client");
        mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		  
        mMessageBox = new JTextPane();
        JTextField mMessageInput = new JTextField();
		  
        mDisplay = new TMClientDisplay(mMonitor, mClient);
        // JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        // JPanel panel = new JPanel(new BorderLayout());
        // panel.add(mMessageBox, BorderLayout.CENTER);
        // panel.add(mMessageInput, BorderLayout.SOUTH);
		  
        // pane.setTopComponent(mDisplay);
        // pane.setBottomComponent(panel);
        // System.out.println(mDisplay.getHeight());
        // pane.setDividerLocation(mDisplay.getHeight());
        mFrame.add(mDisplay);
        mFrame.setResizable(false);

        mBorned = false;
        mDisplay.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    try
                    {
                        if (e.getButton() == MouseEvent.BUTTON1)
                        {
                            if (mBorned)
                            {
                                mOutput.write("rotate-to " + e.getX() + " " + e.getY());
                                mOutput.newLine();
                            }
                            else {
                                mOutput.write("born " + mName + " " + e.getX() + " " + e.getY() + " " +
                                              mTankType + " " + mGunType);
                                mOutput.newLine();
                                mBorned = true;
                            }
                        }
                        else if (e.getButton() == MouseEvent.BUTTON3)
                        {
                            if (mBorned)
                            {
                                mOutput.write("move-to " + e.getX() + " " + e.getY());
                                mOutput.newLine();
                            }
                            else {
                                mOutput.write("born " + mName + " " + e.getX() + " " + e.getY() + " " +
                                              mTankType + " " + mGunType);
                                mOutput.newLine();
                                mBorned = true;
                            }
                        }

                        mOutput.flush();
                    }
                    catch (IOException ex)
                    {
                    }
                }
            });
        mFrame.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_CONTROL)
                    {
                        try
                        {
                            if (mBorned)
                            {
                                mOutput.write("shoot");
                                mOutput.newLine();
                                mOutput.flush();
                            }
                        }
                        catch (IOException ex)
                        {
                        }
                    }
                }
            });

        mDrawTimer =
            new javax.swing.Timer(
                17,
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        mDisplay.repaint(20);
                    }
                }
                );

        mDrawTimer.start();
        mFrame.setVisible(true);
        mFrame.setSize(mDisplay.getWidth() +
                       mFrame.getWidth() - mFrame.getContentPane().getWidth(),
                       mDisplay.getHeight() +
                       mFrame.getHeight() - mFrame.getContentPane().getHeight());
    }
}
