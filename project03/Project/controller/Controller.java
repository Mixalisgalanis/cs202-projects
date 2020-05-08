package controller;

import file.FileManager;
import utilities.MathUtility;

import java.io.IOException;

class Controller {

    //Constants
    private static final int LOWEST_NUMBER = 1;
    private static final int BIGGEST_NUMBER = (int) Math.pow(10, 6);
    private static final int INITIAL_PAGES = 10;
    private static final int MAX_PAGES = 40;
    //Name Files
    private final String PRIMARY_FILE_NAME;
    private final String OVERFLOW_FILE_NAME;
    private final double MAX_OCCUPANCY; //0.8 and 0.5 respectively
    //Class variables
    private double occupancyFactor; //aka "u"
    private double storedValues;

    //Diagnostic statistic variables
    private double storedValuesSinceSplit;
    private double[][] diskAccessesAlteration; //Row 0 used for averageDiskAccesses, Row 1 used for page expansion

    /*private int[][] primaryPageNumbers;
    private int[][] overflowPageNumbers;*/

    //Needed Classes
    private FileManager primaryFile;
    private FileManager overflowFile;
    private MathUtility keyGen;


    //Constructor
    Controller(double MAX_OCCUPANCY) {
        instantiateVariables();

        this.MAX_OCCUPANCY = MAX_OCCUPANCY;
        this.PRIMARY_FILE_NAME = "PrimaryFile" + MAX_OCCUPANCY;
        this.OVERFLOW_FILE_NAME = "OverflowFile" + MAX_OCCUPANCY;

        keyGen = new MathUtility();
        keyGen.setUpRandomness(LOWEST_NUMBER, BIGGEST_NUMBER);
    }

    private void instantiateVariables() {
        occupancyFactor = 0;
        storedValues = 0;

        storedValuesSinceSplit = 0;
        diskAccessesAlteration = new double[2][MAX_PAGES - INITIAL_PAGES];
    }

    public boolean createFiles() throws IOException {
        primaryFile = new FileManager();
        overflowFile = new FileManager();

        return !(!primaryFile.createFile(PRIMARY_FILE_NAME) || !overflowFile.createFile(OVERFLOW_FILE_NAME));

    }

    //Essential Methods
    public void insertKeys() throws IOException {
        double diskAccesses = 0;
        while (primaryFile.getNumberOfPages() < MAX_PAGES) {
            int key = keyGen.generateRandomKey();
            //If there isn't already a key in that block.
            if (!searchKey(key, hashFunction(key))) {
                diskAccesses++;
                //Need to locate the first zero for key to be inserted in primary file.
                //Check if there is a zero at all.
                if ((firstZeroInArray(primaryFile.getBuffer()) != -1) && primaryFile.getBuffer(127) != 0) {
                    primaryFile.setBuffer(key, primaryFile.whereInBuffer(0));
                    primaryFile.writeBlock(hashFunction(key) * primaryFile.getPAGE_SIZE());
                    storedValues++;
                    storedValuesSinceSplit++;
                } else {
                    //Need to locate the first zero for key to be inserted in overflow file.
                    //Check if there is a zero at all.
                    if (primaryFile.getBuffer(127) == -1) {
                        diskAccesses++;
                        //Create overflow page
                        addOverflowPage();

                        //Fill Pointer in primary page
                        long page = overflowFile.getNumberOfPages() - 1;
                        primaryFile.setBuffer((int) page, 127);
                        primaryFile.writeBlock(hashFunction(key) * overflowFile.getPAGE_SIZE());

                        //Fill first element of overflow page
                        overflowFile.readBlock(page * overflowFile.getPAGE_SIZE());
                        overflowFile.setBuffer(key, 0);
                        overflowFile.writeBlock(page * overflowFile.getPAGE_SIZE());

                        storedValues++;
                        storedValuesSinceSplit++;
                    } else {
                        overflowFile.readBlock(primaryFile.getBuffer(127) * overflowFile.getPAGE_SIZE());
                        if (firstZeroInArray(overflowFile.getBuffer()) != -1) {
                            diskAccesses++;
                            overflowFile.setBuffer(key, firstZeroInArray(overflowFile.getBuffer()));
                            overflowFile.writeBlock(primaryFile.getBuffer(127) * overflowFile.getPAGE_SIZE());

                            storedValues++;
                            storedValuesSinceSplit++;
                        } else {
                            //Overflow overflowed
                            if (primaryFile.getNumberOfPages() != MAX_PAGES) initiateSplit();
                            diskAccessesAlteration[0][(int) primaryFile.getNumberOfPages() - 1 - INITIAL_PAGES] = (storedValuesSinceSplit != 0) ? ((diskAccesses / storedValuesSinceSplit)) : 0;
                            diskAccessesAlteration[1][(int) primaryFile.getNumberOfPages() - 1 - INITIAL_PAGES] = primaryFile.getNumberOfPages();
                            storedValuesSinceSplit = 0;
                            diskAccesses = 0;
                            continue;
                        }
                    }
                }

                occupancyFactor = calculateOccupancyFactor();
                if (occupancyFactor > MAX_OCCUPANCY) {
                    if (primaryFile.getNumberOfPages() != MAX_PAGES) initiateSplit();
                    diskAccessesAlteration[0][(int) primaryFile.getNumberOfPages() - 1 - INITIAL_PAGES] = (diskAccesses / storedValuesSinceSplit);
                    diskAccessesAlteration[1][(int) primaryFile.getNumberOfPages() - 1 - INITIAL_PAGES] = primaryFile.getNumberOfPages();
                    storedValuesSinceSplit = 0;
                    diskAccesses = 0;

                }
            }
        }
    }

    private double calculateOccupancyFactor() {
        double totalPrimaryNumbers = primaryFile.getNumberOfPages() * (primaryFile.getBUFFER_SIZE() - 1);
        double totalOverflowNumbers = overflowFile.getNumberOfPages() * overflowFile.getBUFFER_SIZE();
        return storedValues / (totalPrimaryNumbers + totalOverflowNumbers);
    }

    private void initiateSplit() throws IOException {
        //Create a new page filled with zeros
        addPrimaryPage();

        int multiplier = calculateMultiplier();

        //Primary page Redistribution process
        for (int k = 0; k < multiplier - 1; k++) {
            for (int i = 0; i < primaryFile.getBUFFER_SIZE() - 1; i++) {
                long oldIndexPage = (((primaryFile.getNumberOfPages() - 1) % 10) + (k * 10)); //Old Index
                primaryFile.readBlock(oldIndexPage * primaryFile.getPAGE_SIZE()); //Read old index Page

                int key = primaryFile.getBuffer(i);
                long newIndexPage = hashFunction(key);

                if (newIndexPage != oldIndexPage) {
                    //Remove old entry
                    primaryFile.setBuffer(0, i);
                    primaryFile.writeBlock(oldIndexPage * primaryFile.getPAGE_SIZE());

                    //Set new entry
                    primaryFile.readBlock(newIndexPage * primaryFile.getPAGE_SIZE()); //Read new index page

                    //Checking for space in primary file
                    if (firstZeroInArray(primaryFile.getBuffer()) != -1) {
                        primaryFile.setBuffer(key, firstZeroInArray(primaryFile.getBuffer()));
                        primaryFile.writeBlock(newIndexPage * primaryFile.getPAGE_SIZE());
                    } else {
                        if (primaryFile.getBuffer(127) == -1) {
                            //Key goes to overflow page
                            addOverflowPage();
                            //Fill address page on last element of primary page
                            long page = overflowFile.getNumberOfPages() - 1;
                            primaryFile.setBuffer((int) page, 127);
                            primaryFile.writeBlock(newIndexPage * primaryFile.getPAGE_SIZE());

                            //Fill first element of overflow page
                            overflowFile.readBlock(page * overflowFile.getPAGE_SIZE());
                            overflowFile.setBuffer(key, 0);
                            overflowFile.writeBlock(page * overflowFile.getPAGE_SIZE());
                        } else {
                            long page = primaryFile.getBuffer(127);
                            overflowFile.readBlock(page * overflowFile.getPAGE_SIZE());
                            if (firstZeroInArray(overflowFile.getBuffer()) != -1) {
                                overflowFile.setBuffer(key, firstZeroInArray(overflowFile.getBuffer()));
                                overflowFile.writeBlock(page * overflowFile.getPAGE_SIZE());
                            }
                        }
                    }
                }

            }
            int lastIndex = primaryFile.getBuffer(127);
            //Primary page Redistribution process
            if (lastIndex != -1) {
                for (int i = 0; i < overflowFile.getBUFFER_SIZE(); i++) {
                    long oldIndexPage = (lastIndex * overflowFile.getPAGE_SIZE()); //Old Index
                    overflowFile.readBlock(oldIndexPage); //Read old index Page

                    int key = overflowFile.getBuffer(i);
                    long newIndexPrimaryPage = hashFunction(key);

                    if (newIndexPrimaryPage != oldIndexPage) {
                        //Remove old entry
                        overflowFile.setBuffer(0, i);
                        overflowFile.writeBlock(oldIndexPage);

                        //Set new entry
                        primaryFile.readBlock(newIndexPrimaryPage * primaryFile.getPAGE_SIZE()); //Read new index page

                        //Checking for space in primary
                        if (firstZeroInArray(primaryFile.getBuffer()) != -1) {
                            //Key goes to primary page
                            primaryFile.setBuffer(key, firstZeroInArray(primaryFile.getBuffer()));
                            primaryFile.writeBlock(newIndexPrimaryPage * primaryFile.getPAGE_SIZE());
                        } else {

                            //First overflow entry
                            if (primaryFile.getBuffer(127) == -1) {
                                //Key goes to overflow page
                                addOverflowPage();
                                //Fill address page on last element of primary page
                                long page = overflowFile.getNumberOfPages() - 1;
                                primaryFile.setBuffer((int) page, 127);
                                primaryFile.writeBlock(newIndexPrimaryPage * primaryFile.getPAGE_SIZE());

                                //Fill first element of overflow page
                                overflowFile.readBlock(page * overflowFile.getPAGE_SIZE());
                                overflowFile.setBuffer(key, 0);
                                overflowFile.writeBlock(page * overflowFile.getPAGE_SIZE());

                            } else {
                                long page = primaryFile.getBuffer(127);
                                overflowFile.readBlock(page * overflowFile.getPAGE_SIZE());
                                if (firstZeroInArray(overflowFile.getBuffer()) != -1) {
                                    overflowFile.setBuffer(key, firstZeroInArray(overflowFile.getBuffer()));
                                    overflowFile.writeBlock(page * overflowFile.getPAGE_SIZE());
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    public int firstZeroInArray(int[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 0) return i;
        }
        return -1;
    }

    public boolean searchKey(int key, int page) throws IOException {
        primaryFile.readBlock(page * primaryFile.getPAGE_SIZE());
        return primaryFile.isInBuffer(key);

    }

    //Returns index of page the key has to be stored in.
    private int hashFunction(int key) {
        int multiplier = calculateMultiplier();
        return key % (((primaryFile.getNumberOfPages() > key % (multiplier * INITIAL_PAGES)) ? multiplier : multiplier / 2) * INITIAL_PAGES);
    }

    //Calculates multiplier needed for hash function.
    private int calculateMultiplier() {
        int multiplier = 1;
        while (primaryFile.getNumberOfPages() > multiplier * INITIAL_PAGES) {
            multiplier *= 2;
        }
        return multiplier;
    }

    public void displayStats() {

        System.out.println("\n\nLinear Hashing Stats ( u > " + MAX_OCCUPANCY * 100 + "% ):");
        System.out.println("First Column represents the number of current expansion.");
        System.out.println("Second Column represents average number of disk accesses per page expansion.");

        System.out.println("- - - - - - -");
        //Column 0
        for (int i = 0; i < diskAccessesAlteration[0].length; i++) {
            System.out.println(String.format("%.0f", diskAccessesAlteration[1][i]) + ", " + String.format("%.6f", diskAccessesAlteration[0][i]));
        }

        System.out.println("- - - - - - -");


    }

    //Help Methods

    //Creates 10 empty pages (filled with zeros)
    public void createInitialPages() throws IOException {
        //Adding INITIAL_PAGES
        primaryFile.openFile(PRIMARY_FILE_NAME);
        primaryFile.accessFile.seek(0);
        for (int i = 0; i < INITIAL_PAGES; i++) {
            addPrimaryPage();
        }

        overflowFile.openFile(OVERFLOW_FILE_NAME);
        overflowFile.accessFile.seek(0);

        System.out.println("\n" + PRIMARY_FILE_NAME + " ------------------------------");
        System.out.println("Number of pages: " + primaryFile.getNumberOfPages());
        System.out.println("File size: " + primaryFile.getFilePath().length() + " bytes (" + primaryFile.getFilePath().length() / 1000 + " KB).");

    }

    //Created additional pages for primary file (Splitting pages)
    private void addPrimaryPage() throws IOException {
        //Filling up the 128-key buffer with zeros
        for (int j = 0; j < primaryFile.getBUFFER_SIZE() - 1; j++) {
            primaryFile.setBuffer(0, j);
        }
        primaryFile.setBuffer(-1, 127); //Buffer(127) == -1 when no overflow page exists.
        primaryFile.appendBlock();
    }

    private void addOverflowPage() throws IOException {
        //Filling up the 128-key buffer with zeros
        for (int j = 0; j < overflowFile.getBUFFER_SIZE(); j++) {
            overflowFile.setBuffer(0, j);
        }
        overflowFile.appendBlock();
    }

    /*public void extractBuffers() throws IOException{
        primaryPageNumbers = new int[(int)primaryFile.getNumberOfPages()][primaryFile.getBUFFER_SIZE()];
        overflowPageNumbers = new int[(int)overflowFile.getNumberOfPages()][overflowFile.getBUFFER_SIZE()];


        //Extracting primary pages
        for (int i =0 ; i< primaryFile.getNumberOfPages(); i++){
            primaryFile.readBlock(i * primaryFile.getPAGE_SIZE());
            for (int j = 0; j < primaryFile.getBUFFER_SIZE() ; j++){
                primaryPageNumbers[i][j] =  primaryFile.getBuffer(j);
            }
        }
        //Extracting overflow pages
        for (int i =0 ; i< overflowFile.getNumberOfPages(); i++){
            overflowFile.readBlock(i * overflowFile.getPAGE_SIZE());
            for (int j = 0; j < overflowFile.getBUFFER_SIZE() ; j++){
                overflowPageNumbers[i][j] =  overflowFile.getBuffer(j);
            }
        }
    }*/
}
