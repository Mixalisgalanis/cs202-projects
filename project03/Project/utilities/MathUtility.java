package utilities;

import java.util.Random;

public class MathUtility {

    //Class variables
    private Random rand;
    private int lowestRandom;
    private int biggestRandom;

    //Constructor is default
    public MathUtility() {
    }

    //Class Method
    //Defines Randomness Space
    public void setUpRandomness(int lowestRandom, int biggestRandom){
        rand = new Random();
        this.lowestRandom = lowestRandom;
        this.biggestRandom = biggestRandom;
    }

    //Class Method
    //Generates a random key between lowestRandom and biggestRandom
    public int generateRandomKey() {
        return rand.nextInt(biggestRandom) + lowestRandom;
    }

}