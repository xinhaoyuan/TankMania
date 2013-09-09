package tankmania.server;

import tankmania.common.*;
import java.util.*;

public class TMServerEventDetect extends TimerTask
{
	 boolean mBusy;
	 TMServer mServer;
	 TreeMap<Integer, TreeMap<Integer, TMTank> > mTempMap;
	 
	 public TMServerEventDetect(TMServer server) {
		  mServer = server;
		  mBusy = false;
	 }

	 public void run() {

		  if (mBusy)
		  {
			   System.err.println("Busy!");
			   return;
		  }
		  mBusy = true;
		  
		  int time = mServer.getTime();
		  LinkedList<String> out = new LinkedList<String>();
		  LinkedList<String> statusOut = null;

		  for (int i = 0; i != Rule.MAX_TANKS; ++ i)
		  {
			   if (!mServer.mPlayers[i].mAlive) continue;

			   ArrayList<String> msgs = new ArrayList<String>(10);

			   mServer.mPlayers[i].mInQueue.drainTo(msgs);
			   Iterator<String> it = msgs.iterator();
			   
			   while (it.hasNext())
			   {
					String line = it.next();
					try
					{
					
						 if (line.equals("end-connection") || out == null)
						 {
							  mServer.mPlayers[i].mInQueue = null;
							  if (mServer.mPlayers[i].mOutQueue != null)
							  {
								   try
								   {
										mServer.mPlayers[i].mOutQueue.put("end-connection");
								   }
								   catch (Exception ex)
								   {
								   }
								   mServer.mPlayers[i].mOutQueue = null;
							  }

							  TMTank tank = mServer.getTank(i);
							  if (tank.mAliveStart <= time && tank.mAliveEnd > time)
							  {
								   mServer.getTank(i).mLife = 0;
								   mServer.getTank(i).mAliveEnd = time;
								   out.add(time + " destroy tank " + i);
							  }
							  mServer.mPlayers[i].mAlive = false;
							  break;
						 }

						 StringTokenizer tokens = new StringTokenizer(line);
						 int recvTime = Integer.decode(tokens.nextToken());
						 String headerStr = tokens.nextToken();

						 if (headerStr.equals("sync"))
						 {
							  int ctime = Integer.decode(tokens.nextToken());
							  mServer.mPlayers[i].mOutQueue.add("" + recvTime + " sync " + ctime);
						 }
						 else if (headerStr.equals("get-status"))
						 {
							  if (statusOut == null)
							  {
								   statusOut = new LinkedList<String> ();
								   for (int j = 0; j != Rule.MAX_TANKS; ++ j)
								   {
										TMTank tank = mServer.getTank(j);
										if (tank.mAliveStart <= time && tank.mAliveEnd > time)
										{
											 statusOut.add("" + time + " create tank " + tank.mId + " " +
														   tank.mName + " 0 0 " +
														   tank.mLife + " " +
														   tank.mMana + " " +
														   tank.mX.getCurrent(time) + " " +
														   tank.mY.getCurrent(time) + " " +
														   tank.mTankAngle.getCurrent(time) + " " +
														   tank.mGunAngle.getCurrent(time));

											 if (tank.mTankAngle.mType != 0)
											 {
												  int startTime = tank.mTankAngle.mStartTime;
												  int endTime = tank.mTankAngle.mEndTime;

												  statusOut.add("" + startTime + " rotate-tank tank " + tank.mId + " " +
																tank.mTankAngle.getCurrent(startTime) + " " +
																tank.mTankAngle.getCurrent(endTime) + " " + 
																(endTime - startTime));
											 }

											 if (tank.mGunAngle.mType != 0)
											 {
												  int startTime = tank.mGunAngle.mStartTime;
												  int endTime = tank.mGunAngle.mEndTime;

												  statusOut.add("" + startTime + " rotate-gun tank " + tank.mId + " " +
																tank.mGunAngle.getCurrent(startTime) + " " +
																tank.mGunAngle.getCurrent(endTime) + " " + 
																(endTime - startTime));
											 }


											 if (tank.mX.mType != 0 || tank.mY.mType != 0)
											 {
												  int startTime = 0;
												  int endTime = 0;
										
												  if (tank.mX.mType != 0)
												  {
													   startTime = tank.mX.mStartTime;
													   endTime = tank.mX.mEndTime;
												  }
												  else
												  {
													   startTime = tank.mY.mStartTime;
													   endTime = tank.mY.mEndTime;
												  }

												  statusOut.add("" + startTime + " move tank " + tank.mId + " " +
																tank.mX.getCurrent(startTime) + " " + tank.mY.getCurrent(startTime) + " " +
																tank.mX.getCurrent(endTime) + " " + tank.mY.getCurrent(endTime) + " " +
																(endTime - startTime));
											 }
										}

										Iterator<TMBullet> bIt = mServer.getBullets().iterator();
										while (bIt.hasNext())
										{
											 TMBullet bullet = bIt.next();

											 if (bullet.mX.mType != 0 || bullet.mY.mType != 0)
											 {
												  int startTime = 0;
												  int endTime = 0;
										
												  if (bullet.mX.mType != 0)
												  {
													   startTime = bullet.mX.mStartTime;
													   endTime = bullet.mX.mEndTime;
												  }
												  else
												  {
													   startTime = bullet.mY.mStartTime;
													   endTime = bullet.mY.mEndTime;
												  }

												  float sx = bullet.mX.getCurrent(startTime);
												  float sy = bullet.mY.getCurrent(startTime);
												  float ex = bullet.mX.getCurrent(endTime);
												  float ey = bullet.mY.getCurrent(endTime);
											 

												  statusOut.add("" + time + " create bullet " + bullet.mId + " 0 " +
																tank.mId + " " +
																bullet.mX.getCurrent(time) + " " + bullet.mY.getCurrent(time) + " " +
																(float)Math.atan2(ex - sx, sy - ey));
												  statusOut.add("" + startTime + " move bullet " + bullet.mId + " " +
																sx + " " + sy + " " +
																ex + " " + ey + " " +
																(endTime - startTime));
											 }
										}
								   }
							  }

							  for (String msg : statusOut)
							  {
								   try
								   {
										mServer.mPlayers[i].mOutQueue.put(msg);
								   }
								   catch (Exception e)
								   {
								   }
							  }
						 }
						 else if (headerStr.equals("born"))
						 {
							  if (mServer.mPlayers[i].mBorned) continue;
							  mServer.mPlayers[i].mBorned = true;

							  TMTank tank = mServer.getTank(i);
							  String name = tokens.nextToken();
							  float x = Float.valueOf(tokens.nextToken());
							  float y = Float.valueOf(tokens.nextToken());
							  int tankType = Integer.decode(tokens.nextToken());
							  int gunType = Integer.decode(tokens.nextToken());
					
							  if (tank.mAliveStart <= time &&
								  tank.mAliveEnd > time)
								   continue;

							  tank.mTankType = tankType;
							  tank.mGunType = gunType;
							  tank.mAliveStart = Integer.MAX_VALUE;
							  tank.mAliveEnd = Integer.MIN_VALUE;
							  tank.mName = name;
							  tank.mLife = Rule.getTankMaxLife(tankType);
							  tank.mMana = Rule.getTankMaxMana(tankType);
							  tank.mX.setAsStatic(x);
							  tank.mY.setAsStatic(y);
							  tank.mTankAngle.setAsStatic(0);
							  tank.mGunAngle.setAsStatic(0);
						 }
						 else if (headerStr.equals("move-to"))
						 {
							  if (mServer.mPlayers[i].mLastMove == time) continue;
							  mServer.mPlayers[i].mLastMove = time;
						 
							  TMTank tank = mServer.getTank(i);
							  if (tank.mAliveStart > time ||
								  tank.mAliveEnd <= time)
								   continue;

							  float sx = tank.mX.getCurrent(time);
							  float sy = tank.mY.getCurrent(time);
							  float ex = Float.valueOf(tokens.nextToken());
							  float ey = Float.valueOf(tokens.nextToken());
							  float dAngle = (float)Math.atan2(ex - sx, sy - ey) % (2 * (float)Math.PI);
							  float sAngle = tank.mTankAngle.getCurrent(time) % (2 * (float)Math.PI);
							  float aAngle = (float)Math.PI -
								   (float)Math.atan2(ex - sx, sy - ey);
							  float fdis = mServer.getFarestReach(
								   sx + 20 * (float)Math.sin(aAngle),
								   sy + 20 * (float)Math.cos(aAngle),
								   40, aAngle);
							  float edis = (float)Math.sqrt((sx - ex) * (sx - ex) +
															(sy - ey) * (sy - ey));
							  if (fdis >= 0 && fdis < edis)
							  {
								   ex = sx + (ex - sx) * (fdis / edis);
								   ey = sy + (ey - sy) * (fdis / edis);
								   edis = fdis;
							  }
					
							  if (dAngle > sAngle + (float)Math.PI)
								   dAngle -= 2 * (float)Math.PI;
							  else if (dAngle < sAngle - (float)Math.PI)
								   dAngle += 2 * (float)Math.PI;

							  int rotateCost =
								   (int)(Math.abs(dAngle - sAngle) / (float)Math.PI * 100);
					
							  int moveCost = (int)(edis / 0.8);

							  tank.mTankAngle.setAsSegment(time, sAngle,
														   time + rotateCost, dAngle);
							  tank.mX.setAsSegment(time  + rotateCost, sx,
												   time  + rotateCost + moveCost, ex);
							  tank.mY.setAsSegment(time  + rotateCost, sy,
												   time  + rotateCost + moveCost, ey);
						 
							  out.add("" + (time  + rotateCost) + " move tank " + i + " " +
									  sx + " " + sy + " " +
									  ex + " " + ey + " " +
									  moveCost);
							  out.add("" + time + " rotate-tank tank " + i + " " +
									  sAngle + " " + dAngle + " " + rotateCost);
						 }
						 else if (headerStr.equals("rotate-to"))
						 {
							  if (mServer.mPlayers[i].mLastRotate == time) continue;
							  mServer.mPlayers[i].mLastRotate = time;

							  TMTank tank = mServer.getTank(i);
							  if (tank.mAliveStart > time ||
								  tank.mAliveEnd <= time)
								   continue;

							  float sx = tank.mX.getCurrent(time);
							  float sy = tank.mY.getCurrent(time);
							  float ex = Float.valueOf(tokens.nextToken());
							  float ey = Float.valueOf(tokens.nextToken());

							  float dAngle = (float)Math.atan2(ex - sx, sy - ey) % (2 * (float)Math.PI);
							  float sAngle = tank.mGunAngle.getCurrent(time) % (2 * (float)Math.PI);
							  if (dAngle > sAngle + (float)Math.PI)
								   dAngle -= 2 * (float)Math.PI;
							  else if (dAngle < sAngle - (float)Math.PI)
								   dAngle += 2 * (float)Math.PI;

							  int rotateCost =
								   (int)(Math.abs(dAngle - sAngle) / (float)Math.PI * 100);
					
							  tank.mGunAngle.setAsSegment(time, sAngle,
														  time + rotateCost, dAngle);
							  out.add("" + time + " rotate-gun tank " + i + " " +
									  sAngle + " " + dAngle + " " + rotateCost);
						 }
						 else if (headerStr.equals("shoot"))
						 {
							  TMTank tank = mServer.getTank(i);
							  if (time - mServer.mPlayers[i].mLastShoot < Rule.getGunShootInterval(tank.mGunType))
								   continue;
							  mServer.mPlayers[i].mLastShoot = time;
					
							  if (tank.mAliveStart > time ||
								  tank.mAliveEnd <= time)
								   continue;
					
							  float angle = tank.mGunAngle.getCurrent(time) % (2 * (float)Math.PI);
							  float sx = tank.mX.getCurrent(time) + 15 * (float)Math.sin(angle);
							  float sy = tank.mY.getCurrent(time) + 15 * -(float)Math.cos(angle);
							  float edis = Rule.getGunShootDistance(tank.mGunType);
							  float fdis = mServer.getFarestReach(
								   sx, sy,
								   5, (float)Math.PI - angle);
							  if (fdis > 0 && fdis < edis)
								   edis = fdis;
							  float ex = sx + edis * (float)Math.sin(angle);
							  float ey = sy + edis * -(float)Math.cos(angle);

							  int moveCost = (int)(Math.sqrt((sx - ex) * (sx - ex) +
															 (sy - ey) * (sy - ey)) / 1.5);

							  TMBullet bullet = mServer.newBullet();

							  bullet.mOwnerTankId = i;
							  bullet.mAngle.setAsStatic(angle);
							  bullet.mX.setAsSegment(time, sx, time + moveCost, ex);
							  bullet.mY.setAsSegment(time, sy, time + moveCost, ey);
							  bullet.mAliveStart = time;
							  bullet.mAliveEnd = Integer.MAX_VALUE;


							  out.add("" + time + " create bullet " + bullet.mId + " 0 " +
									  tank.mId + " " +
									  sx + " " + sy + " " + angle);
							  out.add("" + time + " move bullet " + bullet.mId + " " +
									  sx + " " + sy + " " +
									  ex + " " + ey + " " +
									  moveCost);
						 }
					}
					catch (Exception x)
					{
						 System.err.println("Got exception " + x + " while processing input");
					}
			   }
		  }
		  
		  mTempMap = new TreeMap<Integer, TreeMap<Integer, TMTank> > ();
		  Collection<TMTank> tanks = mServer.getTanks();
		  Iterator<TMTank> it = tanks.iterator();

		  while (it.hasNext())
		  {
			   TMTank tank = it.next();
			   if (tank.mAliveStart > time ||
				   tank.mAliveEnd <= time) continue;

			   Integer x = new Integer((int)(tank.mX.getCurrent(time) / 20));
			   Integer y = new Integer((int)(tank.mY.getCurrent(time) / 20));
			   if (!mTempMap.containsKey(x))
					mTempMap.put(x, new TreeMap<Integer, TMTank>());
			   mTempMap.get(x).put(y, tank);
			   // System.err.println("tank " + tank.mId + " in region " + x + " " + y);
		  }
		  
		  // Conflict detection
		  it = tanks.iterator();
		  while (it.hasNext())
		  {
			   TMTank tank = it.next();
			   if (tank.mAliveStart > time ||
				   tank.mAliveEnd <= time) continue;

			   float ax = tank.mX.getCurrent(time);
			   float ay = tank.mY.getCurrent(time);
			   Integer x = new Integer((int)(ax / 20));
			   Integer y = new Integer((int)(ay / 20));

			   for (int dx = -2; dx <= 2; ++ dx)
			   {
					Integer nx = new Integer(x + dx);
					if (!mTempMap.containsKey(nx)) continue;
					for (int dy = -2; dy <= 2; ++dy)
					{
						 if (dx == 0 && dy == 0) continue;
						 
						 Integer ny = new Integer(y + dy);
						 TMTank cur = mTempMap.get(nx).get(ny);
						 if (cur == null) continue;

						 float bx = cur.mX.getCurrent(time);
						 float by = cur.mY.getCurrent(time);
						 float ddx = tank.mX.mType != 0 ?
							  (tank.mX.mEndValue - tank.mX.mStartValue) / (tank.mX.mEndTime - tank.mX.mStartTime)
							  : 0;

						 float ddy = tank.mY.mType != 0 ?
							  (tank.mY.mEndValue - tank.mY.mStartValue) / (tank.mY.mEndTime - tank.mY.mStartTime)
							  : 0;

						 float angleJudge = ddx * (bx - ax) -
							  ddy * (by - ay);
						 if (Math.sqrt((ax - bx) * (ax - bx) +
									   (ay - by) * (ay - by)) < 40 &&
							 angleJudge > 0)
						 {
							  tank.mX.setAsStatic(ax);
							  tank.mY.setAsStatic(ay);
							  out.add(time + " move tank " + tank.mId + " " +
									  ax + " " + ay + " " + ax + " " + ay + " 0");
						 }
					}
			   }
		  }
		  
		  it = tanks.iterator();
		  while (it.hasNext())
		  {
			   TMTank tank = it.next();
		
			   if ((tank.mAliveStart <= time &&
					tank.mAliveEnd > time) ||
				   tank.mLife <= 0 ) continue;
		
			   float ax = tank.mX.getCurrent(time);
			   float ay = tank.mY.getCurrent(time);
			   Integer x = new Integer((int)(ax / 20));
			   Integer y = new Integer((int)(ay / 20));
			   boolean noConflict;
			   noConflict = true;
			   for (int dx = -2; dx <= 2 && noConflict; ++ dx)
			   {
					Integer nx = new Integer(x + dx);
					if (!mTempMap.containsKey(nx)) continue;
					for (int dy = -2; dy <= 2 && noConflict; ++dy)
					{
						 Integer ny = new Integer(y + dy);
						 TMTank cur = mTempMap.get(nx).get(ny);
						 if (cur == null) continue;

						 float bx = cur.mX.getCurrent(time);
						 float by = cur.mY.getCurrent(time);

						 if (Math.sqrt((ax - bx) * (ax - bx) +
									   (ay - by) * (ay - by)) < 40)
						 {
							  noConflict = false;
						 }
					}
			   }

			   if (noConflict)
			   {
					tank.mAliveStart = time;
					tank.mAliveEnd = Integer.MAX_VALUE;

					out.add("" + time + " create tank " + tank.mId + " " +
							tank.mName + " 0 0 " +
							tank.mLife + " " +
							tank.mMana + " " +
							tank.mX.getCurrent(time) + " " +
							tank.mY.getCurrent(time) + " " +
							tank.mTankAngle.getCurrent(time) + " " +
							tank.mGunAngle.getCurrent(time));
			   }
		  }


		  Iterator<TMBullet> bIt = mServer.getBullets().iterator();
		  while (bIt.hasNext())
		  {
			   TMBullet bullet = bIt.next();

			   if (bullet.mAliveStart > time ||
				   bullet.mAliveEnd <= time)
					continue;
			   
			   if (!bullet.mX.isChanging(time) &&
				   !bullet.mY.isChanging(time))
			   {
					bullet.mAliveEnd = time;
					out.add(time + " destroy bullet " + bullet.mId);
					bIt.remove();
					continue;
			   }

			   float ax = bullet.mX.getCurrent(time);
			   float ay = bullet.mY.getCurrent(time);
			   Integer x = new Integer((int)(ax / 20));
			   Integer y = new Integer((int)(ay / 20));

			   // System.err.println("detecting bullet " + bullet.mId + " owner " + bullet.mOwnerTankId + " region " + x + " " + y);
			   

			   for (int dx = -2; dx <= 2; ++ dx)
			   {
					Integer nx = new Integer(x + dx);
					if (!mTempMap.containsKey(nx)) continue;
					for (int dy = -2; dy <= 2; ++dy)
					{
						 Integer ny = new Integer(y + dy);
						 // System.err.println("scan region " + nx + " " + ny);
						 
						 TMTank cur = mTempMap.get(nx).get(ny);
						 if (cur == null) continue;

						 // System.err.println("detecting with tank " + cur.mId);
						 
						 if (bullet.mOwnerTankId == cur.mId) continue;

						 float bx = cur.mX.getCurrent(time);
						 float by = cur.mY.getCurrent(time);

						 float angle = bullet.mAngle.getCurrent(time);
						 float angleJudge = (float)Math.sin(angle) * (bx - ax) -
							  (float)Math.cos(angle) * (by - ay);
						 if (Math.sqrt((ax - bx) * (ax - bx) +
									   (ay - by) * (ay - by)) < 20 &&
							  angleJudge > 0)
						 {
							  bullet.mX.setAsStatic(ax);
							  bullet.mY.setAsStatic(ay);

							  cur.mLife = cur.mLife - 100;
							  if (cur.mLife <= 0)
							  {
								   cur.mAliveEnd = time;
								   out.add(time + " destroy tank " + cur.mId);
								   cur.mUsed = false;
							  }
							  else
							  {
								   out.add(time + " set-life tank " + cur.mId + " " + cur.mLife);
							  }
						 }
					}
			   }
		  }

		  for (int i = 0; i != Rule.MAX_TANKS; ++i)
		  {
			   if (!mServer.mPlayers[i].mAlive || mServer.mPlayers[i].mOutQueue == null) continue;
			   for (String msg : out)
			   {
					try
					{
						 mServer.mPlayers[i].mOutQueue.put(msg);
					}
					catch (Exception ex)
					{
					}
			   }
		  }

		  mBusy = false;
	 }
}
