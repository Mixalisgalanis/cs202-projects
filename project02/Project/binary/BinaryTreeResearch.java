package binary;

public class BinaryTreeResearch {

    //Class variables
    private Node root;

    private int[] keyArray;

    private int counter = 0;
    private int keyRangeComparisons = 0;
    private int keyRangeMatches = 0;

    //Constructor
    public BinaryTreeResearch(int N) {
        this.keyArray = new int[N];
    }

    //Getters
    public int getKeyRangeComparisons() {
        return keyRangeComparisons;
    }

    public int getKeyRangeMatches() {
        return keyRangeMatches;
    }

    public Node getRoot() {
        return root;
    }

    public int[] getKeyArray() {
        return keyArray;
    }

    //Setters


    public void setCounter(int counter) {
        this.counter = counter;
    }

    public void setKeyRangeMatches(int keyRangeMatches) {
        this.keyRangeMatches = keyRangeMatches;
    }

    public void setKeyRangeComparisons(int keyRangeComparisons) {
        this.keyRangeComparisons = keyRangeComparisons;
    }

    //Other methods
    public int addKey(int key) {
        int comparisons = 0;
        Node newNode = new Node(key);

        if (root == null) {
            root = newNode;
        } else {
            Node currentNode = root;
            Node parentNode;

            while (true) {
                parentNode = currentNode;
                if (key < currentNode.getKey()) {
                    currentNode = parentNode.getLeftChild();
                    comparisons++;

                    if (currentNode == null) {
                        parentNode.setLeftChild(newNode);
                        return comparisons;
                    }
                } else {
                    currentNode = parentNode.getRightChild();
                    comparisons++;

                    if (currentNode == null) {
                        parentNode.setRightChild(newNode);
                        return comparisons;
                    }
                }
            }
        }
        return comparisons;
    }

    public int searchKey(int key) {
        int comparisons = 0;
        Node currentNode = root;

        while (currentNode.getKey() != key) {

            if (key < currentNode.getKey()) {
                currentNode = currentNode.getLeftChild();
                comparisons++;
            } else {
                currentNode = currentNode.getRightChild();
                comparisons++;
            }

            if (currentNode == null) {
                return comparisons;
            }

        }
        return comparisons;
    }


    public void searchKeyRange(Node currentNode, int key, int offset) {
        if (currentNode == null) return;
        if (key + offset < currentNode.getKey()) searchKeyRange(currentNode.getLeftChild(),key,offset);
        else if (key > currentNode.getKey()) searchKeyRange(currentNode.getRightChild(), key, offset);
        else {
            searchKeyRange(currentNode.getLeftChild(), key, offset);
            keyRangeComparisons++;
            searchKeyRange(currentNode.getRightChild(), key, offset);
        }
    }

    public void inOrderTraversal(Node currentNode) {
        if (currentNode != null) {
            inOrderTraversal(currentNode.getLeftChild());
            keyArray[counter++] = currentNode.getKey();
            inOrderTraversal(currentNode.getRightChild());
        }
    }


}
