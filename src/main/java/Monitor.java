import command.Command;
import probe.StatusType;
import task.Protocol;
import task.Task;
import threads.MulticastListener;
import threads.Worker;
import utils.JsonUtility;

import javax.crypto.SecretKey;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static consts.Const.DEBUG_MODE;

public class Monitor {
    private final SSLServerSocketFactory ssf;
    private final Worker mWorker;
    private final MulticastListener mListener;
    //id, status
    private final Map<String, StatusType> mServices;
    //id, augmented url
    private final Map<String, String> mServicesToSupervise;
    private final Set<Protocol> mProtocolsSupervised;

    public static void main(String[] args) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        Monitor monitor = new Monitor(JsonUtility.getAesKey(), JsonUtility.getMulticastAddress(), JsonUtility.getMulticastPort());
        monitor.start();
    }

    public Monitor(SecretKey key, String group, int port) throws KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, CertificateException, UnrecoverableKeyException{
        mServicesToSupervise = Collections.synchronizedMap(new HashMap<>());
        mServices = Collections.synchronizedMap(new HashMap<>());
        mProtocolsSupervised = Collections.synchronizedSet(new HashSet<>());
        BlockingQueue<Task> mTasks = new LinkedBlockingQueue<>();
        mWorker = new Worker(key, mTasks, mServicesToSupervise, mServices, mProtocolsSupervised);
        mListener = new MulticastListener(group,port, mTasks, mProtocolsSupervised);

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("src/main/resources/group26.monitor.p12"), "group26".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, "group26".toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);
        SSLContext sc = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = tmf.getTrustManagers();
        sc.init(kmf.getKeyManagers(), trustManagers, null);
        this.ssf = sc.getServerSocketFactory();
    }

    public void start() {
        startWorker();
        startMulticastListener();
        handleUnicastConnection();
    }

    private void startWorker() {
        new Thread(mWorker).start();
    }

    private void startMulticastListener() {
        new Thread(mListener).start();
    }

    private void handleUnicastConnection()
    {
        try(ServerSocket listener = ssf.createServerSocket(64321))
        {
            while (true)
            {
                SSLSocket client = (SSLSocket) listener.accept();
                client.startHandshake();
                if (DEBUG_MODE) System.out.println("Client connected!");
                new Thread(new ClientConnection(client, this)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addNewService(String augmentedUrl) {
        //packaging the command into a curconfig to get the id of the augmented url
        Command command = new Command("CURCONFIG" + " " + augmentedUrl);
        mServicesToSupervise.put(command.getMatcher().group("id"), augmentedUrl);
    }

    public Set<String> getServicesIds() {
        return mServices.keySet();
    }

    public StatusType getStatus(String id) {
        return mServices.get(id);
    }

    public String getUrl(String id) {
        Command command = new Command("CURCONFIG " + mServicesToSupervise.get(id));
        return command.getMatcher().group("url");
    }

    public boolean isServiceSupervised(String id) {
        return mServices.containsKey(id);
    }

    public boolean isProtocolAccepted(Protocol protocol) {
        return mProtocolsSupervised.contains(protocol);
    }
}
