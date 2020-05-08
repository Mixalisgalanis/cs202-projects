package Controller;

import java.io.IOException;

public class ConsoleInterface {

    private static Controller controller;

    public static void main(String[] args) throws IOException {
        controller = new Controller();
        controller.generateRandomKeyFile();
        controller.searchWithoutPool(); //Method B
        controller.searchWithPool(controller.getPm1()); //Method D - (K=500)
        controller.searchWithPool(controller.getPm2()); //Method D - (K=1000)

    }

}
