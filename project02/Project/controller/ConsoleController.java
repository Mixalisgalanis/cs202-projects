package controller;

import java.io.IOException;

public class ConsoleController {

    private static Controller controller = new Controller();

    public static void main(String[] args) throws IOException {
        startProcess();
    }

    private static void startProcess() throws IOException{
        controller.startBTRProcess();
    }
}
