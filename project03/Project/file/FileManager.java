package file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FileManager {

    //Class variables
    //constants
    public static final int PAGE_SIZE = 512;
    public static final int BUFFER_SIZE = 128;
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
        this.page = new byte[PAGE_SIZE];
        this.buffer = new int[BUFFER_SIZE];
    }


    //Getters
    public int getPAGE_SIZE() {
        return PAGE_SIZE;
    }
    public int getBUFFER_SIZE() {
        return BUFFER_SIZE;
    }
    public byte[] getPage() {
        return page;
    }
    public int[] getBuffer() {
        return buffer;
    }
    public int getBuffer(int index){
        return buffer[index];
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
    public boolean createFile(String fileName) throws IOException {

        //Creates a filepath and check whether a file is already created.
        filePath = new File(fileName + ".txt");
        if (filePath.createNewFile()) System.out.println(filePath.getName() + " successfully created!");
        else {
            System.out.println(filePath.getName() + " already exists! Process failed (Exit Error Code 0).");
            return false;
        }
        return true;
    }

    public long openFile(String fileName) throws IOException {

        //Performing checks
        if (filePath == null) filePath = new File(fileName + ".txt");
        else if (!filePath.exists()) {
            System.out.println(filePath.getName() + "not found! Process failed (Exit Error Code 0).");
            return 0;
        } else if (!filePath.isDirectory()) accessFile = new RandomAccessFile(filePath, "rw");

        //Assuming everything else is OK.
        return (accessFile.length() % PAGE_SIZE > 0) ? (accessFile.length() / PAGE_SIZE) + 1 : accessFile.length() / PAGE_SIZE;
    }

    public void readBlock(long filePointer) throws IOException {

        //Performing checks
        if (accessFile == null) {
            System.out.println("File is not opened. Process failed (Exit Error Code 0).");
            return;
        }

        if (filePointer < 0 || filePointer > accessFile.length() || filePointer % PAGE_SIZE != 0) {
            //System.out.println("Invalid file pointer. Process failed (Exit Error Code 0).");
            return;
        }

        //Assuming everything else is OK.
        accessFile.seek(filePointer);
        accessFile.read(page);
        convertPageToBuffer();
    }

    public void writeBlock(long filePointer) throws IOException {
        //Performing checks
        if (accessFile == null) {
            System.out.println("File is not opened. Process failed (Exit Error Code 0).");
            return;
        }

        if (filePointer < 0 || filePointer > (accessFile.length() + getPAGE_SIZE())) {
            //System.out.println("Invalid file pointer. Process failed (Exit Error Code 0).");
            return;
        }

        //Assuming everything else is OK.
        accessFile.seek(filePointer);
        convertBufferToPage();
        accessFile.write(page);

        if (filePointer > (numberOfPages * getPAGE_SIZE() - 1)){
            numberOfPages++;
        }
    }

    public void appendBlock() throws IOException{
        //Performing checks
        if (accessFile == null) {
            System.out.println("File is not opened. Process failed (Exit Error Code 0).");
            return;
        }
        accessFile.seek(numberOfPages * getPAGE_SIZE());
        writeBlock(accessFile.getFilePointer());
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
        for (int i = 0; i < BUFFER_SIZE; i++) {
            page[4* i] = (byte) (buffer[i] >> 24);
            page[4* i + 1] = (byte) (buffer[i] >> 16);
            page[4* i + 2] = (byte) (buffer[i] >> 8);
            page[4* i + 3] = (byte) (buffer[i]);
        }
    }

    //Buffer and page must occupate the same size
    private void convertPageToBuffer() {
        for (int i = 0; i < BUFFER_SIZE; i++) {
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

    //Returns true if it finds a key within the buffer.
    public boolean isInBuffer(int key) {
        for (int i = 0; i < BUFFER_SIZE; i++) {
            if (key == buffer[i]) return true;
        }
        return false;
    }

    //Returns the index of a key found within the buffer. Returns -1 if not found.
    public int whereInBuffer(int key) {
        for (int i = 0; i< BUFFER_SIZE; i++){
            if (key == buffer[i]) return i;
        }
        return -1;
    }

}
