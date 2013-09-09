package tankmania.common;

public class Rule
{
    public final static int NUM_OF_TANK_TYPES = 1;
    public final static int NUM_OF_GUN_TYPES = 1;
    public final static int NUM_OF_EFFECT_TYPES = 2;
    public final static int NUM_OF_BULLET_TYPES = 1;

    public final static int MAX_TANKS = 12;

    public static float getGunShootDistance(int type) {
        switch (type)
        {
        case 0:
            return 400;
        default:
            return 0;
        }
    }

    public static int getGunShootInterval(int type) {
        switch (type)
        {
        case 0:
            return 30;
        default:
            return Integer.MAX_VALUE;
        }
    }

    public static int getTankMaxLife(int type) {
        switch (type)
        {
        case 0:
            return 1000;
        default:
            return 1000;
        }
    }

    public static int getTankMaxMana(int type) {
        switch (type)
        {
        case 0:
            return 500;
        default:
            return 500;
        }
    }
}
