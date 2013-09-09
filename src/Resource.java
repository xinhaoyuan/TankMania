package tankmania.common;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class Resource
{
    private static Resource mSingleton = null;

    public static Resource Get() {
        try{
            if (mSingleton == null)
                mSingleton = new Resource();
        }
        catch (IOException x)
        {
            System.out.println("Exception occurs when initialize the resources.:" + x);
            mSingleton = null;
        }
        
        return mSingleton;
    }

    private BufferedImage[] mTankImages;
    private BufferedImage[] mGunImages;
    private BufferedImage[] mEffectImages;
    private BufferedImage[] mBulletImages;

    private Resource() throws IOException {
        // Initialize all needed resource
        mTankImages = new BufferedImage[Rule.NUM_OF_TANK_TYPES];
        for (int i = 0; i != Rule.NUM_OF_TANK_TYPES; ++i)
            mTankImages[i] = ImageIO.read(new File("res/tank_" + i + ".png"));
        mGunImages = new BufferedImage[Rule.NUM_OF_GUN_TYPES];
        for (int i = 0; i != Rule.NUM_OF_GUN_TYPES; ++ i)
            mGunImages[i] = ImageIO.read(new File("res/gun_" + i + ".png"));
        mEffectImages = new BufferedImage[Rule.NUM_OF_EFFECT_TYPES];
        for (int i = 0; i != Rule.NUM_OF_EFFECT_TYPES; ++ i)
            mEffectImages[i] = ImageIO.read(new File("res/effect_" + i + ".png"));
        mBulletImages = new BufferedImage[Rule.NUM_OF_BULLET_TYPES];
        for (int i = 0; i != Rule.NUM_OF_BULLET_TYPES; ++i)
            mBulletImages[i] = ImageIO.read(new File("res/bullet_" + i + ".png"));
    }

    public BufferedImage getTankImage(int type) {
        return mTankImages[type];
    }

    public BufferedImage getGunImage(int type) {
        return mGunImages[type];
    }

    public BufferedImage getEffectImage(int type) {
        return mEffectImages[type];
    }

    public BufferedImage getBulletImage(int type) {
        return mBulletImages[type];
    }
}
