package tankmania.common;

public class TMBullet
{
	 public int mAliveStart;
	 public int mAliveEnd;
	 public int mType;
	 public int mId;
	 public int mOwnerTankId;
		  
	 public TMScalar mX;
	 public TMScalar mY;
	 public TMScalar mAngle;

	 public TMBullet() {
		  mAliveStart = 1;
		  mAliveEnd = 0;
		  mAngle = new TMScalar();
		  mX = new TMScalar();
		  mY = new TMScalar();
	 }

}
