package tankmania.common;

import java.util.BitSet;

public class TMTank
{
	 public boolean mUsed;
	 public int mAliveStart;
	 public int mAliveEnd;
	 public String mName;
	 public int mTankType;
	 public int mGunType;
	 public int mId;
	 public int[] mEffectStart;
	 public int[] mEffectEnd;
	 public float[] mEffectAngle;
		  
	 public TMScalar mTankAngle;
	 public TMScalar mGunAngle;
	 public TMScalar mX;
	 public TMScalar mY;

	 public int mLife;
	 public int mMana;
	 public int mActionPoint;

	 public TMTank() {
		  mUsed = false;
		  mAliveStart = 1;
		  mAliveEnd = 0;
		  mName = null;
		  mEffectStart = new int[Rule.NUM_OF_EFFECT_TYPES];
		  mEffectEnd = new int[Rule.NUM_OF_EFFECT_TYPES];
		  for (int i = 0; i != Rule.NUM_OF_EFFECT_TYPES; ++i)
		  {
			   mEffectStart[i] = 1;
			   mEffectEnd[i] = 0;
		  }
		  mEffectAngle = new float[Rule.NUM_OF_EFFECT_TYPES];
		  mTankAngle = new TMScalar();
		  mGunAngle = new TMScalar();
		  mX = new TMScalar();
		  mY = new TMScalar();
	 }
}
