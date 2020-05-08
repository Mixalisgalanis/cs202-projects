package Manager;

import java.util.ArrayList;

public class PoolManager {

    //Class variables
    private  int maxBufferPoolPages;
    private static final int bufferSize = 128;

    private ArrayList<int[]> bufferPool;


    //Constructor
    public PoolManager(int maxBufferPoolPages) {
        this.maxBufferPoolPages = maxBufferPoolPages;
    }


    //Getters
    public int getMaxBufferPoolPages() {
        return maxBufferPoolPages;
    }

    public static int getBufferSize() {
        return bufferSize;
    }

    public ArrayList<int[]> getBufferPool() {
        return bufferPool;
    }


    //Setters
    public void setMaxBufferPoolPages(int maxBufferPoolPages) {
        this.maxBufferPoolPages = maxBufferPoolPages;
    }

    public void setBufferPool(ArrayList<int[]> bufferPool) {
        this.bufferPool = bufferPool;
    }


    //Essential methods
    public void createPool() {
        bufferPool = new ArrayList<>();
    }

    //Returns true if key is found, returns false if key is not found.
    public boolean searchPool(int key) {
        for (int[] bufferPoolPage : bufferPool) {
            for (int j = 0; j < bufferSize; j++){
                if (bufferPoolPage[j] == key) return true;
            }
        }
        return false;
    }

    public void insertPool(int[] bufferPoolPage) {
        bufferPool.add(0,bufferPoolPage);
        if (bufferPool.size() > maxBufferPoolPages){
            for (int i=0 ; i<bufferPool.size() - maxBufferPoolPages; i++){
                bufferPool.remove(maxBufferPoolPages);
            }
        }
    }


    public void deletePool(int index) {
        if (bufferPool.size() >= index){
            bufferPool.remove(index);
        }
    }

    public void freePool() {
        bufferPool.clear();
    }


}
