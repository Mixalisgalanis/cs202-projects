package Controller;

import Manager.FileManager;
import Manager.PoolManager;
import Utilities.KeyGenerator;

import java.io.IOException;

public class Controller {

    //Class variables - Constants forced by exercise's requirements
    private static final int lowestNumber = 1;
    private static final int biggestNumber = (int) Math.pow(10, 6);
    private static final int numberOfNumbers = (int) Math.pow(10, 6);

    private static final String randomGenFileName = "RandomGenFile";

    private PoolManager pm1;
    private PoolManager pm2;

    private FileManager fm;
    private KeyGenerator keygen;

    //Constructor
    Controller() {
        fm = new FileManager();
        keygen = new KeyGenerator(lowestNumber, biggestNumber);
        pm1 = new PoolManager(500);
        pm2 = new PoolManager(1000);
    }

    //Getters
    public PoolManager getPm1() {
        return pm1;
    }

    public PoolManager getPm2() {
        return pm2;
    }


    //Class Methods
    //Generates random keys, creates a file and imports them through the buffer variable
    public void generateRandomKeyFile() throws IOException {
        System.out.println("Attempting to generate a random key file.");
        fm.createFile(randomGenFileName);
        fm.openFile(randomGenFileName);

        for (int i = 0; i < (numberOfNumbers / fm.getBuffer_Size()) + 1; i++) {
            //Filling up the buffer with 128 numbers
            for (int j = 0; j < fm.getBuffer_Size(); j++) {
                int tempNumber = keygen.generateRandomKey();
                fm.setBuffer(tempNumber, j);
            }
            //Write 128 numbers to file
            fm.appendBlock();
        }

        fm.fileHandle();

        System.out.println("------------------------------------------");
        System.out.println("Number of pages: " + fm.getNumberOfPages());
        System.out.println("File size: " + fm.getFilePath().length() + " bytes (" + fm.getFilePath().length() / 1000000 + " MB).");
    }


    public void searchWithoutPool() throws IOException {
        final int numberOfSearches = 10000;
        int numbersFound = 0;
        int sumOfDiskAccesses = 0;
        int key;


        fm.openFile(randomGenFileName);
        System.out.println("This might take a while, please wait. . .");

        //Searching each number
        for (int i = 0; i < numberOfSearches; i++) {
            int diskAccesses = 0;
            key = keygen.generateRandomKey(); //This is the question.


            //System.out.println((i + 1) + ": Searching for value " + key);

            fm.readBlock(0);
            //Searching this number in each buffer page
            while (true) {
                fm.readNextBlock();
                if (fm.isInBuffer(key)) {
                    numbersFound++;
                    break;
                } else if ((int) fm.getNumberOfPages() - diskAccesses == 0) break;
                diskAccesses++;
            }
            sumOfDiskAccesses += diskAccesses;
        }
        System.out.println("\n\n-------------- METHOD B (1-page buffer) --------------");
        System.out.println("Numbers found: " + numbersFound + "/" + numberOfSearches + " (" + ((float) (numbersFound * 100)/numberOfSearches) + "%)");
        System.out.println("Average number of disk accesses for all searches: " + (sumOfDiskAccesses / numberOfSearches) + "\n\n");

    }


    public void searchWithPool(PoolManager pm) throws IOException {
        final int numberOfSearches = 10000;
        int numbersFound = 0;
        int sumOfDiskAccesses = 0;
        int key;

        fm.openFile(randomGenFileName);
        pm.createPool();

        //Searching each number
        for (int i = 0; i < numberOfSearches; i++) {
            int diskAccesses = 0;
            key = keygen.generateRandomKey(); //This is the question.

            //System.out.println((i + 1) + ": Searching for value " + key);

            fm.readBlock(0);

            for (int j = 0; j < ((fm.getNumberOfPages() / pm.getMaxBufferPoolPages()) + 1); j++) {
                int pages = ((int) fm.getNumberOfPages() / (pm.getMaxBufferPoolPages()) > 0 ? pm.getMaxBufferPoolPages() : ((int) fm.getNumberOfPages() % pm.getMaxBufferPoolPages()));

                //Filling up the bufferPool
                for (int k = 0; k < pages; k++) {
                    fm.readNextBlock();
                    pm.insertPool(fm.getBuffer().clone());
                }
                diskAccesses++;

                //Searching the bufferPool
                if (pm.searchPool(key)) {
                    numbersFound++;
                    break;
                }
            }

            sumOfDiskAccesses += diskAccesses;
        }
        System.out.println("\n\n-------------------- METHOD D (K= " + pm.getMaxBufferPoolPages() + ") --------------------");
        System.out.println("Numbers found: " + numbersFound + "/" + numberOfSearches + " (" + ((float) (numbersFound * 100)/numberOfSearches) + "%)");
        System.out.println("Average number of disk accesses for all searches: " + (sumOfDiskAccesses / numberOfSearches) + "\n\n");

    }

}
