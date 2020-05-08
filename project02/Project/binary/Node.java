package binary;

public class Node {

    //Class variables
    private int key;

    private Node leftChild;
    private Node rightChild;


    //Constructors
    public Node(int value) {
        this.key = value;
    }


    //Getters

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public Node getLeftChild() {
        return leftChild;
    }


    //Setters

    public void setLeftChild(Node leftChild) {
        this.leftChild = leftChild;
    }

    public Node getRightChild() {
        return rightChild;
    }

    public void setRightChild(Node rightChild) {
        this.rightChild = rightChild;
    }


    //Other Methods
}


