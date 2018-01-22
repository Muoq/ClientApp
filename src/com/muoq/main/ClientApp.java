package com.muoq.main;

import com.sun.security.ntlm.Server;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Scanner;

public class ClientApp {

    static final int SERVER_PORT = 8080;
    static final String SERVER_IP = "127.0.0.1";

    SSLContext sslContext;
    SSLSocket sslSocket;

    PrintWriter writer;

    private void setupNetworking() {
        try {
//            CertificateFactory cf = CertificateFactory.getInstance("X.509");
//            X509Certificate cert = (X509Certificate) cf.generateCertificate(new FileInputStream("C:\\Users\\victo\\.keystore\\selfsigned.crt"));

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore ks = KeyStore.getInstance("PKCS12");
//            ks.load(null);

            ks.load(new FileInputStream("C:\\Users\\victo\\.keystore\\selfsigned.pks"), "victor1406".toCharArray());

//            ks.setCertificateEntry("selfsigned", cert);

            tmf.init(ks);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, "victor1406".toCharArray());

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
    }

    private void connectToServer() {
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        try {
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(SERVER_IP, SERVER_PORT);
            sslSocket.startHandshake();

            System.out.printf("Connected to server on port: %d.\n", sslSocket.getLocalPort());
            System.out.println("Server ip: " + sslSocket.getInetAddress());

            writer = new PrintWriter(sslSocket.getOutputStream());

            Thread serverListenerThread = new Thread(new ServerListener());
            serverListenerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupInput() {
        Thread inputThread = new Thread(new InputScanner());
        inputThread.start();
    }

    private void start() {
        setupInput();
        setupNetworking();
        System.out.println("SSL Setup completed. Connecting to server...");
        connectToServer();
    }

    private void handleMessage(String message) {

    }

    private void handleInput(String input) {
        if (writer != null) {
            writer.println(input);

            System.out.println("Wrote to server: " + input);
        } else {
            try {
                writer = new PrintWriter(sslSocket.getOutputStream());

                writer.println(input);
            } catch (IOException e) {
                System.out.println("Could not connect to server.");
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new ClientApp().start();
    }

    class ServerListener implements Runnable {

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));

                String message;

                while ((message = reader.readLine()) != null) {
                    handleMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    class InputScanner implements Runnable {

        public void run() {
            Scanner scanner = new Scanner(System.in);

            String message;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.printf("Input: ");
            while ((message = scanner.nextLine()) != null) {
                handleInput(message);
                System.out.printf("Input: ");
            }

        }

    }

}
