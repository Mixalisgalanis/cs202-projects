package utilities;

import java.util.Random;

public class KeyGenerator {

    //Class variables
    private Random rand;
    private int lowest;
    private int biggest;

    //Class Constructor
    public KeyGenerator(int lowest, int biggest) {
        this.lowest = lowest;
        this.biggest = biggest;
        rand = new Random();
    }

    //Class Method
    public int generateRandomKey() {
        return rand.nextInt(biggest) + lowest;
    }
}
