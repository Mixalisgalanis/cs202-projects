package controller;

import binary.BinaryTreeResearch;
import file.FileManager;
import utilities.KeyGenerator;
import java.io.IOException;

public class Controller {
    //Class variables
    private static final int arraySize = (int) Math.pow(10,7);
    private static final int insertKeys = (int) Math.pow(10, 4);         //Number of keys to be inserted
    private static final int searchKeys = (int) Math.pow(10, 2);         //Number of keys to be searched
    private static final int searchKeyOffset = (int) Math.pow(10, 2);    //Number of key ranges to be searched

    private BinaryTreeResearch btr = new BinaryTreeResearch(arraySize);
    private KeyGenerator kg = new KeyGenerator(0, (int) Math.pow(10, 4));
    private FileManager fm = new FileManager();
    private static final String randomGenFileName = "RandomGenFile";

    //Class methods - PART 1
    private void addNRandomKeys(int N) {
        int averageComparisons;
        int totalComparisons = 0;
        for (int i = 0; i <= N; i++) {
            totalComparisons += btr.addKey(kg.generateRandomKey());
        }
        averageComparisons = totalComparisons / N;

        System.out.println(N + " keys have been inserted into the binary tree.");
        System.out.println("Total comparisons: " + totalComparisons);
        System.out.println("Average comparisons per insertion: " + averageComparisons + "\n");
    }

    private void searchNRandomKeys(int N) {
        int averageComparisons;
        int totalComparisons = 0;
        for (int i = 0; i <= N; i++) {
            totalComparisons += btr.searchKey(kg.generateRandomKey());
        }
        averageComparisons = totalComparisons / N;
        System.out.println(N + " keys have been searched in the binary tree.");
        System.out.println("Total comparisons: " + totalComparisons);
        System.out.println("Average comparisons per search: " + averageComparisons + "\n");
    }

    private void searchNRandomKeyOffset(int N, int offset) {
        int averageComparisons;
        int totalComparisons = 0;
        for (int i = 0; i <= N; i++) {
            btr.searchKeyRange(btr.getRoot(), kg.generateRandomKey(), offset);
            totalComparisons += btr.getKeyRangeComparisons();
        }
        averageComparisons = totalComparisons / N;
        System.out.println(N + " keys have been searched in the binary tree with offset (+ " + offset + ").");
        System.out.println("Total comparisons: " + totalComparisons);
        System.out.println("Average comparisons per search: " + averageComparisons + "\n");
        btr.setKeyRangeComparisons(0);
        btr.setKeyRangeMatches(0);
    }

    private void inorderTraversalFile() throws IOException{
        btr.inOrderTraversal(btr.getRoot());
        createBTRFile(btr.getKeyArray());
        btr.setCounter(0);
    }

    public void createBTRFile(int[] keyArray) throws IOException {
        System.out.println("Attempting to generate a random key file.");
        fm.createFile(randomGenFileName);
        fm.openFile(randomGenFileName);

        for (int i = 0; i < (keyArray.length / fm.getBuffer_Size()); i++) {
            //Filling up the buffer with 128 numbers
            for (int j = 0; j < fm.getBuffer_Size(); j++) fm.setBuffer(keyArray[(i)*fm.getBuffer_Size() + j], j);

            //Write 128 numbers to file
            fm.appendBlock();
        }

        fm.fileHandle();

        System.out.println("------------------------------------------");
        System.out.println("Number of pages: " + fm.getNumberOfPages());
        System.out.println("File size: " + fm.getFilePath().length() + " bytes (" + fm.getFilePath().length() / 1000000 + " MB).");
    }


    //Class methods - PART 2

    private void serialSearchFile(int N) throws IOException {
        int sumOfDiskAccesses = 0;
        int key;

        fm.openFile(randomGenFileName);
        System.out.println("This might take a while, please wait. . .");

        //Searching each number
        for (int i = 0; i < N; i++) {
            int diskAccesses = 0;
            key = kg.generateRandomKey(); //This is the question.

            //System.out.println((i + 1) + ": Searching for value " + key);

            fm.readBlock(0);
            //Searching this number in each buffer page
            while (true) {
                fm.readNextBlock();
                if (fm.isInBufferSerial(key)) {
                    break;
                } else if ((int) fm.getNumberOfPages() - diskAccesses == 0) break;
                diskAccesses++;
            }
            sumOfDiskAccesses += diskAccesses;
        }
        System.out.println("Average number of disk accesses for "+ N + " searches: " + (sumOfDiskAccesses / N) + "\n\n");

    }

    private void binarySearchFile(int N) throws IOException{
        int sumOfDiskAccesses = 0;
        int key;

        fm.openFile(randomGenFileName);


        for (int i = 0; i < N; i++) { //N searches

            int diskAccesses = 0;
            key = kg.generateRandomKey(); //This is the question.
            //System.out.println((i + 1) + ": Searching for value " + key);

            long min_page = 0;
            long max_page = fm.getNumberOfPages() - 1;

            while (max_page >= min_page){

                long mid_page = (min_page + max_page)/2;
                fm.readBlock(mid_page*4);
                diskAccesses++;

                if (fm.isInBufferBinary(key)){
                    break;
                }
                if (fm.getBuffer(0) < key) min_page = mid_page + 1;
                if (fm.getBuffer(0) > key) max_page = mid_page - 1;

            }
            sumOfDiskAccesses += diskAccesses;
        }
        System.out.println("Average number of disk accesses for "+ N + " searches: " + (sumOfDiskAccesses / N) + "\n\n");
    }

    private void binarySearchRangeFile(int N, int offset) throws IOException{
        int sumOfDiskAccesses = 0;
        int key;

        for (int i = 0; i < N; i++) { //N searches
            int diskAccesses = 0;
            key = kg.generateRandomKey(); //This is the question.
            //System.out.println((i + 1) + ": Searching for value " + key);

            long min_page = 0;
            long max_page = fm.getNumberOfPages() - 1;

            while (max_page >= min_page){

                long mid_page = (min_page + max_page)/2;
                fm.readBlock(mid_page*4);
                diskAccesses++;

                if (fm.isInBufferBinary(key + offset))break;

                if (fm.getBuffer(0) < key + offset) min_page = mid_page + 1;
                if (fm.getBuffer(0) > key + offset) max_page = mid_page - 1;

            }

            if (max_page < min_page) continue;

            min_page = 0;
            max_page = fm.getNumberOfPages() - 1;

            while (max_page >= min_page){

                long mid_page = (min_page + max_page)/2;
                fm.readBlock(mid_page*4);
                diskAccesses++;

                if (fm.isInBufferBinary(key))break;

                if (fm.getBuffer(0) < key) min_page = mid_page + 1;
                if (fm.getBuffer(0) > key) max_page = mid_page - 1;

            }

            if (max_page < min_page) continue;

            while (!fm.isInBufferSerial(key + offset)) {
                fm.readNextBlock();
                if (fm.isInBufferSerial(key + offset)) {
                    break;
                } else if ((int) fm.getNumberOfPages() - diskAccesses == 0) break;
                diskAccesses++;
            }

            sumOfDiskAccesses += diskAccesses;
        }


        System.out.println("Average number of disk accesses for "+ N + " searches: " + (sumOfDiskAccesses / N) + "\n\n");


    }


    //CALCULATIONS
    public void startBTRProcess() throws IOException {
        //PART 1
        //Calculation 1 - Inserting 10^4 keys into the binary tree.
        System.out.println("CALCULATION 1 - Insertion of 10^4 keys into the binary tree.");
        System.out.println("---------------------------------------------------------");
        addNRandomKeys(insertKeys);

        //Calculation 2 - Searching 100 keys in the binary tree.
        System.out.println("CALCULATION 2 - Search of 100 keys in the binary tree.");
        System.out.println("---------------------------------------------------------");
        searchNRandomKeys(searchKeys);

        //Calculation 3A - Searching 100 keys with offset (+100) in the binary tree.
        System.out.println("CALCULATION 3A - Search of 100 keys with offset (+100) in the binary tree.");
        System.out.println("---------------------------------------------------------");
        searchNRandomKeyOffset(searchKeyOffset, 100);

        //Calculation 3B - Searching 100 keys with offset (+1000) in the binary tree.
        System.out.println("CALCULATION 3B - Search of 100 keys with offset (+1000) in the binary tree.");
        System.out.println("---------------------------------------------------------");
        searchNRandomKeyOffset(searchKeyOffset, 1000);

        //Calculation 4 - Inorder traversal and copy into sorted file.
        System.out.println("Executing Inorder traversal.");
        inorderTraversalFile();



        //PART 2
        System.out.println("\nPART 2");
        System.out.println("=========================================================\n");

        System.out.println("CALCULATION 1 - Serial Search of 100 keys from File.");
        System.out.println("---------------------------------------------------------");
        serialSearchFile(100);

        System.out.println("CALCULATION 2 - Binary Search of 100 keys from File.");
        System.out.println("---------------------------------------------------------");
        binarySearchFile(100);

        System.out.println("CALCULATION 3 - Binary Search of 100 keys from File.");
        System.out.println("K = 100");
        System.out.println("---------------------------------------------------------");
        binarySearchRangeFile(100,100);
        System.out.println("K = 1000");
        System.out.println("---------------------------------------------------------");
        binarySearchRangeFile(100,1000);

    }

}