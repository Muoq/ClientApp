package com.muoq.main;

import com.muoq.main.util.InputReceiver;
import com.muoq.main.util.InputScanner;

import javax.net.ssl.*;
import java.io.*;
import java.net.ConnectException;
import java.net.SocketException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class ClientApp implements InputReceiver {

    static final char NUL = (char) 0;
    static final int SERVER_PORT = 8080;
    static final String SERVER_IP = "192.168.1.46";
    static final String CERT_PATH = "/cert/servercert.crt";

    InputScanner inputScanner;

    SSLContext sslContext;
    SSLSocket sslSocket;

    PrintWriter writer;

    public ClientApp() {
        inputScanner = new InputScanner();
        inputScanner.addInputReceiver(this);
        Thread scannerThread = new Thread(inputScanner);
        scannerThread.start();
    }

    private void setupNetworking() {
        try {
            InputStream certInputStream = getClass().getResourceAsStream(CERT_PATH);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(certInputStream);

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null);
            ks.setCertificateEntry("selfsign", cert);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
        } catch (ConnectException e) {
            System.out.println("Could not connect to server: Server unavailable.");
            System.out.println("Exiting...");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        setupNetworking();
        System.out.println("SSL Setup completed. Connecting to server...");
        connectToServer();
    }

    private void handleMessage(String message) {
        System.out.println("Message from server: " + message);
    }

    public void receive(String message) {
        String[] messageComponents = message.split(" ", 2);
        try {
            message = "VIPC" + NUL + messageComponents[0] + NUL + messageComponents[1] + NUL + NUL;
        } catch (ArrayIndexOutOfBoundsException e) {
            message = "VIPC" + NUL + messageComponents[0] + NUL + NUL + NUL;
        }
        writer.println(message);
        writer.flush();

        System.out.println("Wrote to server: " + message);
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
            } catch(SocketException e) {
                System.out.println("Server disconnected.");
                System.out.println("Exiting...");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
