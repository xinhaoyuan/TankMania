package tankmania.server;

import tankmania.common.*;

import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class TMServer
{
    static class Player
    {
        boolean mAlive;
        BlockingQueue<String> mInQueue;
        BlockingQueue<String> mOutQueue;
        int mLastMove;
        int mLastRotate;
        int mLastShoot;
        boolean mBorned;
    }

    long mStartTime;
	 
    TMTank[] mTanks;
    Player[] mPlayers;
	 
    TMMap mMap;
    int mBulletCount;
    TreeMap<Integer, TMBullet> mBullets;
	 	 
    public TMServer(File map) {
        mStartTime = System.currentTimeMillis() / 10;
        mTanks = new TMTank[Rule.MAX_TANKS];
        mPlayers = new Player[Rule.MAX_TANKS];
        for (int i = 0; i != Rule.MAX_TANKS; ++i)
        {
            mTanks[i] = new TMTank();
            mTanks[i].mId = i;
            mPlayers[i] = new Player();
            mPlayers[i].mAlive = false;
            mPlayers[i].mInQueue = null;
            mPlayers[i].mOutQueue = null;
        }

        mBulletCount = 0;
        mBullets = new TreeMap<Integer, TMBullet>();
        mMap = new TMMap();
        mMap.loadFromFile(map);
    }
	 
    public int getTime() {
        return (int)(System.currentTimeMillis() / 10 - mStartTime);
    }
	 
    public TMTank getTank(int id) {
        return mTanks[id];
    }

    public Collection<TMTank> getTanks() {
        int time = getTime();
        Vector<TMTank> result = new Vector<TMTank>();
        for (int i = 0; i != Rule.MAX_TANKS; ++ i)
        {
            if ((mTanks[i].mAliveStart <= time &&
                 mTanks[i].mAliveEnd > time) ||
                mTanks[i].mLife > 0)
                result.add(mTanks[i]);
        }
        return result;
    }
	 
    public float getFarestReach(float x, float y, float d, float angle) {
        return mMap.getFarestReach(x, y, d, angle);
    }
	 
    public TMBullet newBullet() {
        TMBullet result = new TMBullet();
        result.mId = mBulletCount ++;
        mBullets.put(new Integer(result.mId), result);
        return result;
    }

    public void removeBullet(int id) {
        mBullets.remove(new Integer(id));
    }

    public Collection<TMBullet> getBullets() {
        return mBullets.values();
    }

    public static void runServer(String mapFilename, int port) {
        TMServer server = new TMServer(new File(mapFilename));
        ServerSocket serverConn;
        try
        {
            serverConn = new ServerSocket(port);
            serverConn.setReuseAddress(true);
        }
        catch (IOException ex)
        {
            System.out.println("" + ex);
            serverConn = null;
        }

        if (serverConn == null)
        {
            System.err.println("Cannot listen on specified port");
        }
        else
        {
            new Timer(false).schedule(new TMServerEventDetect(server), 0, 30);
            while (true)
            {
                Socket conn;
                int id = -1;
                try
                {
                    conn = serverConn.accept();
                    for (int i = 0; i != Rule.MAX_TANKS; ++ i)
                    {
                        if (!server.mPlayers[i].mAlive)
                        {
                            id = i;
                            break;
                        }
                    }
                    if (id == -1)
                    {
                        conn.close();
                        continue;
                    }

                    System.err.println("New connection for id " + id);
                    LinkedBlockingQueue<String> inQueue = new LinkedBlockingQueue<String>();
                    LinkedBlockingQueue<String> outQueue = new LinkedBlockingQueue<String>();
						 
                    BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(conn.getOutputStream()));
                    outQueue.put(server.getTime() + " set-id " + id);
                    outQueue.put(server.getTime() + " load " + mapFilename);

                    server.mPlayers[id].mLastMove = -1;
                    server.mPlayers[id].mLastRotate = -1;
                    server.mPlayers[id].mLastShoot = -1;
                    server.mPlayers[id].mBorned = false;
                    server.mPlayers[id].mInQueue = inQueue;
                    server.mPlayers[id].mOutQueue = outQueue;
                    server.mPlayers[id].mAlive = true;

                    new Thread(
                        new TMServerConnectionRecv(
                            server,
                            inQueue,
                            new InputStreamReader(conn.getInputStream()),
                            conn
                            )).start();
                    new Thread(
                        new TMServerConnectionSend(
                            outQueue,
                            new OutputStreamWriter(conn.getOutputStream()),
                            conn
                            )).start();

                }
                catch (Exception ex)
                {
                    System.err.println("connection failed ");
                    ex.printStackTrace();
                    conn = null;
                    continue;
                }
            }
        }
    }
}
