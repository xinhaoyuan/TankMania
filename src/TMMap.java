package tankmania.common;

import java.io.*;
import java.util.*;

public class TMMap
{

	 static class Segment
	 {
		  public float mSX, mSY, mEX, mEY;
		  public Segment(float sx, float sy, float ex, float ey) {
			   mSX = sx;
			   mSY = sy;
			   mEX = ex;
			   mEY = ey;
		  }
	 }

	 int mWidth, mHeight;
	 Segment[] mSegments;
	 String mImageFilename;
	 
	 public TMMap() {
		  mWidth = mHeight = -1;
		  mSegments = null;
		  mImageFilename = null;
	 }

	 public int getHeight() {
		  return mHeight;
	 }

	 public int getWidth() {
		  return mWidth;
	 }

	 public String getImageFilename() {
		  return mImageFilename;
	 }

	 public void loadFromFile(File file) {
		  BufferedReader reader;
		  int count;
		  try
		  {
			   reader = new BufferedReader(new FileReader(file));
			   StringTokenizer tokens = new StringTokenizer(reader.readLine());
			   mWidth = Integer.decode(tokens.nextToken());
			   mHeight = Integer.decode(tokens.nextToken());
			   mImageFilename = reader.readLine();
			   count = Integer.decode(reader.readLine());
		  }
		  catch (IOException ex)
		  {
			   mWidth = mHeight = 0;
			   mImageFilename = null;
			   mSegments = new Segment[0];
			   return;
		  }
		  
		  mSegments = new Segment[count];
		  int idx = 0;
		  while (true) {
			   String line;
			   try
			   {
					line = reader.readLine();
			   }
			   catch (IOException ex)
			   {
					break;
			   }

			   if (line == null) break;

			   StringTokenizer tokens = new StringTokenizer(line);
			   float sX = Float.valueOf(tokens.nextToken());
			   float sY = Float.valueOf(tokens.nextToken());
			   float eX = Float.valueOf(tokens.nextToken());
			   float eY = Float.valueOf(tokens.nextToken());

			   mSegments[idx ++] = new Segment(sX, sY, eX, eY);
		  }
	 }

	 public float getFarestReach(float x, float y, float d, float angle) {
		  float result = -1;
		  
		  float a = (float)Math.cos(angle);
		  float b = -(float)Math.sin(angle);
		  float c = (a * x + b * y);
		  float cd = (float)Math.abs(a * d * (float)Math.cos(angle) - b * d * (float)Math.sin(angle)) / 2;

		  for (int i = 0; i != mSegments.length; ++ i)
		  {
			   Segment seg = mSegments[i];
			   float sc = seg.mSX * a + seg.mSY * b;
			   float ec = seg.mEX * a + seg.mEY * b;
						   
			   if ((sc < c - cd &&
					ec < c - cd) ||
				   (sc > c + cd &&
					ec > c + cd))
					continue;

			   float sx, sy, ex, ey;
			   if (sc < c - cd)
			   {
					sx = (seg.mSX * (ec - (c - cd)) + seg.mEX * ((c - cd) - sc)) / (ec - sc);
					sy = (seg.mSY * (ec - (c - cd)) + seg.mEY * ((c - cd) - sc)) / (ec - sc);
			   }
			   else if (sc > c + cd)
			   {
					sx = (seg.mSX * ((c + cd) - ec) + seg.mEX * (sc - (c + cd))) / (sc - ec);
					sy = (seg.mSY * ((c + cd) - ec) + seg.mEY * (sc - (c + cd))) / (sc - ec);
			   }
			   else
			   {
					sx = seg.mSX;
					sy = seg.mSY;
			   }

			   if (ec < c - cd)
			   {
					ex = (seg.mSX * ((c - cd) - ec) + seg.mEX * (sc - (c - cd))) / (sc - ec);
					ey = (seg.mSY * ((c - cd) - ec) + seg.mEY * (sc - (c - cd))) / (sc - ec);
			   }
			   else if (ec > c + cd)
			   {
					ex = (seg.mSX * (ec - (c + cd)) + seg.mEX * ((c + cd) - sc)) / (ec - sc);
					ey = (seg.mSY * (ec - (c + cd)) + seg.mEY * ((c + cd) - sc)) / (ec - sc);
			   }
			   else
			   {
					ex = seg.mEX;
					ey = seg.mEY;
			   }

			   float sr = (sx - x) * (float)Math.sin(angle) + (sy - y) * (float)Math.cos(angle);
			   float er = (ex - x) * (float)Math.sin(angle) + (ey - y) * (float)Math.cos(angle);

			   if (sr > 0 && er > 0)
			   {
					if (result < -0.5 || result > sr) result = sr;
					if (result < -0.5 || result > er) result = er;
			   }
			   else if (sr > 0 || er > 0)
					result = 0;
		  }

		  return result;
	 }
}
