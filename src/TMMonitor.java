package tankmania.client;

import tankmania.common.*;

import java.awt.*;
import java.util.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import javax.imageio.*;
import java.awt.font.*;
import java.util.concurrent.*;

class TMMonitor
{
	 long mTimeStart;
	 int mSelfId;
	 TMTank[] mTanks;
	 TreeMap<Integer, TMBullet> mBullets;
	 TMMap mMap = null;
	 BufferedImage mMapBackground;
	 BlockingQueue<String> mMessages;

	 int getCurrentTime() {
		  return (int)(System.currentTimeMillis() / 10 - mTimeStart);
	 }
	 
	 public TMMonitor(BlockingQueue<String> message) {
		  mTanks = new TMTank[Rule.MAX_TANKS];
		  for (int i = 0; i != Rule.MAX_TANKS; ++ i)
		  {
			   mTanks[i] = new TMTank();
			   mTanks[i].mId = i;
		  }
		  mBullets = new TreeMap<Integer, TMBullet>();
		  mMap = new TMMap();
		  mMapBackground = null;
		  mSelfId = -1;
		  mTimeStart = System.currentTimeMillis() / 10;
		  mMessages = message;
	 }

	 public TMTank getTank(int id) {
		  return mTanks[id];
	 }

	 public TMBullet getBullet(int id) {
		  Integer key = new Integer(id);
		  TMBullet result = mBullets.get(key);
		  if (result == null)
		  {
			   result = new TMBullet();
			   mBullets.put(key, result);
		  }
		  return result;
	 }

	 public void setSelfId(int id) {
		  mSelfId = id;
	 }
	 
	 public void drawOnGraphics2D(Graphics2D g) {
		  int now = getCurrentTime();
		  long k = System.currentTimeMillis() / 10;
		  
		  ArrayList<String> msgs = new ArrayList<String>(10);
		  mMessages.drainTo(msgs);
		  Iterator<String> it = msgs.iterator();

		  while (it.hasNext())
		  {
			   String line = it.next();
			   StringTokenizer tokens = new StringTokenizer(line);
			   
			   int time;
			   time = Integer.decode(tokens.nextToken());
			   
			   String cmdHeaderStr = tokens.nextToken();
			   String objTypeStr = tokens.nextToken();
			   String objIdStr = tokens.nextToken();

			   if (cmdHeaderStr.equals("create"))
			   {
					if (objTypeStr.equals("tank"))
					{
						 int id = Integer.decode(objIdStr);
						 String name = tokens.nextToken();
						 int tankTypeId = Integer.decode(tokens.nextToken());
						 int gunTypeId = Integer.decode(tokens.nextToken());
						 int life = Integer.decode(tokens.nextToken());
						 int mana = Integer.decode(tokens.nextToken());
						 float x = Float.valueOf(tokens.nextToken());
						 float y = Float.valueOf(tokens.nextToken());
						 float tAngle = Float.valueOf(tokens.nextToken());
						 float gAngle = Float.valueOf(tokens.nextToken());
						 TMTank tank = getTank(id);
						 tank.mName = name;
						 tank.mTankType = tankTypeId;
						 tank.mGunType = gunTypeId;
						 tank.mLife = life;
						 tank.mMana = mana;
						 tank.mX.setAsStatic(x);
						 tank.mY.setAsStatic(y);
						 tank.mTankAngle.setAsStatic(tAngle);
						 tank.mGunAngle.setAsStatic(gAngle);
						 tank.mAliveStart = time;
						 tank.mAliveEnd = Integer.MAX_VALUE;
					}
					else if (objTypeStr.equals("bullet"))
					{
						 int id = Integer.decode(objIdStr);
						 int typeId = Integer.decode(tokens.nextToken());
						 int ownerId = Integer.decode(tokens.nextToken());
						 float x = Float.valueOf(tokens.nextToken());
						 float y = Float.valueOf(tokens.nextToken());
						 float angle = Float.valueOf(tokens.nextToken());
						 TMBullet bullet = getBullet(id);
						 bullet.mType = typeId;
						 bullet.mId = id;
						 bullet.mOwnerTankId = ownerId;
						 bullet.mX.setAsStatic(x);
						 bullet.mY.setAsStatic(y);
						 bullet.mAngle.setAsStatic(angle);
						 bullet.mAliveStart = time;
						 bullet.mAliveEnd = Integer.MAX_VALUE;
					}
			   }
			   else if (cmdHeaderStr.equals("destroy"))
			   {
					if (objTypeStr.equals("tank"))
					{
						 int id = Integer.decode(objIdStr);
						 TMTank tank = getTank(id);
						 tank.mAliveEnd = time;
					}
					else if (objTypeStr.equals("bullet"))
					{
						 int id = Integer.decode(objIdStr);
						 TMBullet bullet = getBullet(id);
						 bullet.mAliveEnd = time;
					}
			   }
			   else if (cmdHeaderStr.equals("move"))
			   {
					if (objTypeStr.equals("tank"))
					{
						 int id = Integer.decode(objIdStr);
						 float sx = Float.valueOf(tokens.nextToken());
						 float sy = Float.valueOf(tokens.nextToken());
						 float ex = Float.valueOf(tokens.nextToken());
						 float ey = Float.valueOf(tokens.nextToken());
						 int timeCost = Integer.decode(tokens.nextToken());
						 TMTank tank = getTank(id);
						 tank.mX.setAsSegment(time, sx, time + timeCost, ex);
						 tank.mY.setAsSegment(time, sy, time + timeCost, ey);
					}
					else if (objTypeStr.equals("bullet"))
					{
						 int id = Integer.decode(objIdStr);
						 float sx = Float.valueOf(tokens.nextToken());
						 float sy = Float.valueOf(tokens.nextToken());
						 float ex = Float.valueOf(tokens.nextToken());
						 float ey = Float.valueOf(tokens.nextToken());
						 int timeCost = Integer.decode(tokens.nextToken());
						 TMBullet bullet = getBullet(id);
						 bullet.mX.setAsSegment(time, sx, time + timeCost, ex);
						 bullet.mY.setAsSegment(time, sy, time + timeCost, ey);
					}
			   }
			   else if (cmdHeaderStr.equals("rotate-tank"))
			   {
					int id = Integer.decode(objIdStr);
					float sAngle = Float.valueOf(tokens.nextToken());
					float eAngle = Float.valueOf(tokens.nextToken());
					int timeCost = Integer.decode(tokens.nextToken());
					TMTank tank = getTank(id);
					tank.mTankAngle.setAsSegment(time, sAngle, time + timeCost, eAngle);
			   }
			   else if (cmdHeaderStr.equals("rotate-gun"))
			   {
					int id = Integer.decode(objIdStr);
					float sAngle = Float.valueOf(tokens.nextToken());
					float eAngle = Float.valueOf(tokens.nextToken());
					int timeCost = Integer.decode(tokens.nextToken());
					TMTank tank = getTank(id);
					tank.mGunAngle.setAsSegment(time, sAngle, time + timeCost, eAngle);
			   }
			   else if (cmdHeaderStr.equals("rotate-bullet"))
			   {
					int id = Integer.decode(objIdStr);
					float sAngle = Float.valueOf(tokens.nextToken());
					float eAngle = Float.valueOf(tokens.nextToken());
					int timeCost = Integer.decode(tokens.nextToken());
					TMBullet bullet = getBullet(id);
					bullet.mAngle.setAsSegment(time, sAngle, time + timeCost, eAngle);
			   }
			   else if (cmdHeaderStr.equals("effect-start"))
			   {
					int id = Integer.decode(objIdStr);
					int type = Integer.decode(tokens.nextToken());
					float angle = Float.valueOf(tokens.nextToken());
					TMTank tank = getTank(id);
					tank.mEffectAngle[type] = angle;
					tank.mEffectStart[type] = time;
			   }
			   else if (cmdHeaderStr.equals("effect-end"))
			   {
					int id = Integer.decode(objIdStr);
					int type = Integer.decode(tokens.nextToken());
					TMTank tank = getTank(id);
					tank.mEffectEnd[type] = time;
			   }
			   else if (cmdHeaderStr.equals("set-life"))
			   {
					int id = Integer.decode(objIdStr);
					int life = Integer.decode(tokens.nextToken());
					TMTank tank = getTank(id);
					tank.mLife = life;
			   }
			   else if (cmdHeaderStr.equals("set-mana"))
			   {
					int id = Integer.decode(objIdStr);
					int mana = Integer.decode(tokens.nextToken());
					TMTank tank = getTank(id);
					tank.mMana = mana;
			   }
		  }
		  
		  Collection<TMBullet> bullets = mBullets.values();
		  if (mMapBackground == null)
		  {
			   String filename = mMap.getImageFilename();
			   if (filename != null)
			   {
					try
					{
						 mMapBackground = ImageIO.read(new File(filename));
					}
					catch (IOException ex)
					{
						 mMapBackground = null;
					}
			   }
		  }
		  else
		  {
			   g.drawImage(mMapBackground, null, null);
		  }

		  Iterator<TMBullet> bulletIt = bullets.iterator();
		  while (bulletIt.hasNext())
		  {
			   TMBullet bullet = bulletIt.next();
			   if (bullet.mAliveStart <= now &&
				   bullet.mAliveEnd > now)
					drawBulletOnGraphics2D(g, bullet, now);
		  }

		  TMTank selfTank = getTank(mSelfId);
		  
		  for (int i = 0; i != Rule.MAX_TANKS; ++ i) {
			   TMTank tank = mTanks[i];
			   if (tank.mAliveStart <= now &&
				   tank.mAliveEnd > now)
				 {
					  if (tank.mId != mSelfId &&
						  selfTank.mAliveStart <= now &&
						  selfTank.mAliveEnd > now)
					  {
						   float dx = tank.mX.getCurrent(now) -
								selfTank.mX.getCurrent(now);
						   float dy = selfTank.mY.getCurrent(now) -
								tank.mY.getCurrent(now);
						   float angle =(float)
								Math.atan2(dx, dy);
						   if (Math.sin(selfTank.mGunAngle.getCurrent(now)) *
							   Math.sin(angle) +
							   Math.cos(selfTank.mGunAngle.getCurrent(now)) *
							   Math.cos(angle) < -0.5)
								continue;
						   float edis = (float)Math.sqrt(dx * dx + dy * dy);
						   if (edis > 800)
								continue;
						   float dis = mMap.getFarestReach(selfTank.mX.getCurrent(now),
														   selfTank.mY.getCurrent(now),
														   10,
														   (float)Math.PI - angle);
						   if (dis > 0 && dis < edis) continue;
					  }
					  
					  drawTankOnGraphics2D(g, tank, now);
				 }
		  }

		  g.drawString("" + now, 0, 10);

	 }

	 private void drawTankOnGraphics2D(Graphics2D g, TMTank tank, int time) {
		  BufferedImage tankImage = Resource.Get().getTankImage(tank.mTankType);
		  BufferedImage gunImage = Resource.Get().getGunImage(tank.mGunType);
		  AffineTransform tankTf = new AffineTransform();
		  AffineTransform gunTf = new AffineTransform();
		  float x = tank.mX.getCurrent(time);
		  float y = tank.mY.getCurrent(time);
		  // 20 20 for the center point of image
		  tankTf.setToTranslation(x - 20,
								  y - 20);
		  gunTf.setToTranslation(x - 20,
								 y - 20);
		  tankTf.rotate(tank.mTankAngle.getCurrent(time), 20, 20);
		  gunTf.rotate(tank.mGunAngle.getCurrent(time), 20, 20);
		  g.drawImage(tankImage, tankTf, null);
		  g.drawImage(gunImage, gunTf, null);

		  AffineTransform tf = new AffineTransform();
		  for (int i = 0; i != Rule.NUM_OF_EFFECT_TYPES; ++i)
		  {
			   if (tank.mEffectStart[i] > time ||
				   tank.mEffectEnd[i] <= time) continue;
			   
			   tf.setToTranslation(x - 20,
								   y - 20);
			   tf.rotate(tank.mEffectAngle[i], 20, 20);
			   g.drawImage(Resource.Get().getEffectImage(i), tf, null);
		  }

		  GlyphVector textVector = g.getFont().createGlyphVector(g.getFontRenderContext(), tank.mName);
		  Stroke oldStroke = g.getStroke();
		  float fx = (float)(x - textVector.getLogicalBounds().getWidth() / 2);
		  float fy = y - 34;
		  
		  g.setColor(Color.WHITE);
		  g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		  g.draw(textVector.getOutline(fx, fy));
		  g.setStroke(oldStroke);
		  g.setColor(Color.BLACK);
		  g.drawGlyphVector(textVector, fx, fy);
				  
		  
		  g.setColor(Color.RED);
		  g.fill(new Rectangle((int)x - 25, (int)y - 30,
							   (int)((float)tank.mLife / Rule.getTankMaxLife(tank.mTankType) * 50.0),
							   4));
		  g.setColor(Color.BLACK);
		  g.draw(new Rectangle((int)x - 25, (int)y - 30, 50, 4));
	 }
	 
	 private void drawBulletOnGraphics2D(Graphics2D g, TMBullet bullet, int time) {
		  BufferedImage bulletImage = Resource.Get().getBulletImage(bullet.mType);
		  AffineTransform tf = new AffineTransform();
		  // 20 20 for the center point of image
		  tf.setToTranslation(bullet.mX.getCurrent(time) - 20,
							  bullet.mY.getCurrent(time) - 20);
		  tf.rotate(bullet.mAngle.getCurrent(time), 20, 20);
		  g.drawImage(bulletImage, tf, null);
	 }
}
