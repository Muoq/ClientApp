package com.muoq.main.util;

import java.util.ArrayList;
import java.util.Scanner;

public class InputScanner implements Runnable {

    private Scanner scanner;
    private ArrayList<InputReceiver> inputReceivers;

    public InputScanner() {
        this.scanner = new Scanner(System.in);
        inputReceivers = new ArrayList<>();
    }

    public void addInputReceiver(InputReceiver inputReceiver) {
        inputReceivers.add(inputReceiver);
    }

    public void run() {
        String message;

        while ((message = scanner.nextLine()) != null) {
            if (!inputReceivers.isEmpty()) {
                System.out.println(message);
                for (InputReceiver inputReceiver : inputReceivers) {
                    inputReceiver.receive(message);
                }
            }
        }
    }

}
