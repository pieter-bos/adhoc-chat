package transport_v2;

public class Util {
    public static long differenceWithWrapAround(int x, int y) {
        long xx = x;
        long yy = y;

        if(xx == yy) {
            return 0;
        }

        if(yy < xx) {
            yy += 1L << 32;
        }

        long incrementSteps = yy - xx;

        yy = y;

        if(yy > xx) {
            yy -= 1L << 32;
        }

        long decrementSteps = xx - yy;

        if(incrementSteps > decrementSteps) {
            return decrementSteps;
        } else {
            return -incrementSteps;
        }
    }
}
