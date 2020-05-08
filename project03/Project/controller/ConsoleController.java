package controller;

import java.io.IOException;

public class ConsoleController {

    //Main function should be as simple as possible
    //Just calls out a starting function
    public static void main(String[] args) throws IOException {
        startProcess();
    }

    //Class method
    //Calls starting function of controller
    private static void startProcess() throws IOException{
        Controller controller08 = new Controller (0.8);
        if (!controller08.createFiles()) {
            System.out.println("\nFiles already created from a previous run! Please delete them and start over.");
            return;
        }
        controller08.createInitialPages();
        controller08.insertKeys();
        controller08.displayStats();


        Controller controller05 = new Controller (0.5);
        if (!controller05.createFiles()) {
            System.out.println("\nFiles already created from a previous run! Please delete them and start over.");
            return;
        }
        controller05.createInitialPages();
        controller05.insertKeys();
        controller05.displayStats();

    }

}
