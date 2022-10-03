import socket.SocketSSLCommunicator;
import utils.JsonUtility;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Scanner;

public class Client extends SocketSSLCommunicator {

    public static void main(String[] args) {
        try {
            Client client = new Client("127.0.0.1", JsonUtility.getClientPort()); //localhost and port 64321
            client.start();
        } catch (KeyStoreException | IOException | CertificateException | UnrecoverableKeyException | KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public Client(String host, int port) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("src/main/resources/group26.monitor.p12"), "group26".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, "group26".toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);
        SSLContext sc = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = tmf.getTrustManagers();
        sc.init(kmf.getKeyManagers(), trustManagers, null);
        SSLSocketFactory ssf = sc.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket) ssf.createSocket(host, port);
        sslSocket.startHandshake();
        setSocket(sslSocket);
    }

    public void start() throws IOException {
        boolean quit = false;

        while(!quit)
        {
            Scanner scan = new Scanner(System.in);
            int choice;
            do{
                System.out.println("[1] - Display service's supervised list");
                System.out.println("[2] - Display service's supervised state");
                System.out.println("[3] - Supervise a new service");
                System.out.println("[4] - Quit");
                choice = scan.nextInt();
            } while(choice != 1 && choice != 2 && choice != 3 && choice != 4);

            scan = new Scanner(System.in);

            switch (choice)
            {
                case 1:
                    sendMessage("LISTSRV");
                    receiveMessage();
                    break;
                case 2:
                    System.out.print("Service's id : ");
                    sendMessage("STATESRV" + " " + scan.nextLine());
                    receiveMessage();
                    break;
                case 3:
                    System.out.print("Service's augmented url to supervise : ");
                    sendMessage("ADDSRV" + " " + scan.nextLine());
                    receiveMessage();
                    break;
                case 4:
                    quit = true;
                    break;
            }
        }
    }

}
