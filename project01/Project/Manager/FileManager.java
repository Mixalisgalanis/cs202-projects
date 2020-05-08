package Manager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FileManager {

    //Class variables

    //constants
    private static final int page_size = 512;
    private static final int buffer_size = 128;
    public RandomAccessFile accessFile;
    //Buffer types
    private byte[] page; //with page_size: 512 bytes
    private int[] buffer; //with buffer_size: 128 (4-bytes)
    //File Properties
    private File filePath;
    private long numberOfPages;


    //Constructor
    public FileManager() {
        //Page_size = 512 bytes
        this.page = new byte[page_size];
        this.buffer = new int[buffer_size];
    }


    //Getters
    public static int getPage_size() {
        return page_size;
    }

    public int getBuffer_Size() {
        return buffer_size;
    }

    public byte[] getPage() {
        return page;
    }

    public int[] getBuffer() {
        return buffer;
    }

    //Setters
    public void setPage(byte[] page) {
        this.page = page;
    }

    public long getNumberOfPages() {
        return numberOfPages;
    }

    public File getFilePath() {
        return filePath;
    }

    public void setBuffer(int value, int index) {
        this.buffer[index] = value;
    }


    //Class Essential Methods
    public void fileHandle() throws IOException {

        //File must exist in order to extract information
        if (filePath == null) {
            System.out.println("Please set file path! Process failed (Exit Error Code 0).");
            return;
        } else if (filePath.exists() && !filePath.isDirectory()) {
            accessFile = new RandomAccessFile(filePath, "rw");
        }

        //Exporting information from file
        numberOfPages = (accessFile.length() == 0) ? 0 : accessFile.length() / page_size + 1;

        //Importing information to first block
        buffer[0] = (int) numberOfPages;
        buffer[1] = (int) accessFile.getFilePointer();

        convertBufferToPage();

        writeBlock(0);
    }

    public void createFile(String fileName) throws IOException {

        //Creates a filepath and check whether a file is already created.
        filePath = new File(fileName + ".txt");
        if (filePath.createNewFile()) System.out.println(filePath.getName() + " successfully created!");
        else {
            System.out.println(filePath.getName() + " already exists! Process failed (Exit Error Code 0).");
            return;
        }

        //Sets up & writes information in the first page of the file by calling fileHandle()
        fileHandle();
    }

    public long openFile(String fileName) throws IOException {

        //Performing checks
        if (filePath == null) filePath = new File(fileName + ".txt");
        else if (!filePath.exists()) {
            System.out.println(filePath.getName() + "not found! Process failed (Exit Error Code 0).");
            return 0;
        } else if (!filePath.isDirectory()) accessFile = new RandomAccessFile(filePath, "rw");

        //Assuming everything else is OK.
        return (accessFile.length() % page_size > 0) ? (accessFile.length() / page_size) + 1 : accessFile.length() / page_size;
    }

    public void readBlock(long filePointer) throws IOException {

        //Performing checks
        if (accessFile == null) {
            System.out.println("File is not opened. Process failed (Exit Error Code 0).");
            return;
        }

        if (filePointer < 0 || filePointer > accessFile.length() || filePointer % page_size != 0) {
            System.out.println("Invalid file pointer. Process failed (Exit Error Code 0).");
            return;
        }

        //Assuming everything else is OK.
        accessFile.seek(filePointer);
        accessFile.read(page);
        convertPageToBuffer();
    }

    public void readNextBlock() throws IOException {
        //Performing checks
        if (accessFile == null) {
            System.out.println("File is not opened. Process failed (Exit Error Code 0).");
            return;
        }

        //Assuming everything else is OK.
        accessFile.seek(accessFile.getFilePointer() + page_size);
        accessFile.read(page);
        convertPageToBuffer();
    }

    public int readPrevBlock() throws IOException {
        //Performing checks
        if (accessFile == null) {
            System.out.println("File is not opened. Process failed.");
            return 0;
        }

        //Assuming everything else is OK.
        accessFile.seek(accessFile.getFilePointer() - page_size);
        accessFile.read(page);
        convertPageToBuffer();
        return 1;
    }

    public void writeBlock(long filePointer) throws IOException {
        //Performing checks
        if (accessFile == null) {
            System.out.println("File is not opened. Process failed (Exit Error Code 0).");
            return;
        }

        if (filePointer < 0 || filePointer > accessFile.length()) {
            System.out.println("Invalid file pointer. Process failed (Exit Error Code 0).");
            return;
        }

        //Assuming everything else is OK.
        accessFile.seek(filePointer);
        convertBufferToPage();
        accessFile.write(page);

        if (filePointer / page_size > numberOfPages) {
            numberOfPages += (filePointer / page_size + 1 - numberOfPages);
        }
    }

    public int writeNextBlock() throws IOException {
        //Performing checks
        if (accessFile == null) {
            System.out.println("File is not opened. Process failed.");
            return 0;
        }

        //Assuming everything else is OK.
        accessFile.seek(accessFile.getFilePointer() + page_size);
        convertBufferToPage();
        accessFile.write(page);

        if (accessFile.getFilePointer() / page_size > numberOfPages) {
            numberOfPages += (accessFile.getFilePointer() / page_size + 1 - numberOfPages);
        }
        return 1;
    }

    public void appendBlock() throws IOException {
        //Performing checks
        if (accessFile == null) {
            System.out.println("File is not opened. Process failed (Exit Error Code 0).");
            return;
        }

        writeBlock(accessFile.getFilePointer());
        numberOfPages++;
    }

    public void deleteBlock() throws IOException {
        //Performing checks
        /*if (accessFile == null) {
            System.out.println("File is not opened. Process failed (Exit Error Code 0).");
            return;
        }*/

        //this function is not needed

    }

    public void closeFile() throws IOException{

        if (accessFile == null) {
            System.out.println("File is not opened. Process failed (Exit Error Code 0).");
            return;
        }

        accessFile.close();

    }


    //Other Methods

    //Buffer and page must occupate the same size
    private void convertBufferToPage() {
        for (int i = 0; i < buffer_size; i++) {
            page[4* i] = (byte) (buffer[i] >> 24);
            page[4* i + 1] = (byte) (buffer[i] >> 16);
            page[4* i + 2] = (byte) (buffer[i] >> 8);
            page[4* i + 3] = (byte) (buffer[i]);
        }
    }

    //Buffer and page must occupate the same size
    private void convertPageToBuffer() {
        for (int i = 0; i < buffer_size; i++) {
            byte[] tempByteArray = new byte[4];
            tempByteArray[0] = page[4 * i];
            tempByteArray[1] = page[4 * i + 1];
            tempByteArray[2] = page[4 * i + 2];
            tempByteArray[3] = page[4 * i + 3];
            ByteBuffer byteBuffer = ByteBuffer.wrap(tempByteArray);
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
            buffer[i] = byteBuffer.getInt();
        }
    }

    public boolean isInBuffer(int key) {
        for (int i = 0; i < buffer_size; i++) {
            if (key == buffer[i]) return true;
        }
        return false;
    }


}
