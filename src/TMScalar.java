package tankmania.common;

public class TMScalar
{
	 public int mType;
		  
	 public int mStartTime;
	 public int mEndTime;

	 public float mStartValue;
	 public float mEndValue;

	 public TMScalar() {
		  mType = 0;
		  mStartValue = 0;
	 }

	 public boolean isChanging(int now) {
		  switch (mType)
		  {
		  case 0:
			   return false;

		  case 1:
			   return (now >= mStartTime && now < mEndTime);

		  case 2:

			   return (now >= mStartTime);
			   
		  default:
			   return false;
		  }
	 }

	 public float getCurrent(int now) {
		  switch (mType)
		  {
		  case 0:
			   return mStartValue;

		  case 1:
			   if (now <= mStartTime)
					return mStartValue;
			   else if (now >= mEndTime)
					return mEndValue;
			   else return
						 (mEndValue * (now - mStartTime) +
						  mStartValue * (mEndTime - now)) /
						 (mEndTime - mStartTime);
					
		  case 2:
			   if (now <= mStartTime)
					return mStartValue;
			   else return mStartValue +
						 mEndValue *
						 (now - mStartTime) / mEndTime;
		  default:
			   return 0;
		  }
	 }

	 public void setAsStatic(float value) {
		  mStartValue = value;
		  mType = 0;
	 }

	 public void setAsSegment(int startTime, float startValue,
					   int endTime, float endValue) {
		  mStartTime = startTime;
		  mStartValue = startValue;
		  mEndTime = endTime;
		  mEndValue = endValue;
		  mType = 1;
	 }

	 public void setAsRadial(int startTime, float startValue,
					  int stepTime, float stepValue) {
		  mStartTime = startTime;
		  mStartValue = startValue;
		  mEndTime = stepTime;
		  mEndValue = stepValue;
		  mType = 2;
	 }
		  
}
