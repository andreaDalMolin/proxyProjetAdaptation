package probe;

import task.Protocol;
import threads.MulticastSender;
import utils.JsonUtility;

import javax.crypto.SecretKey;
import java.net.ServerSocket;
import java.net.Socket;

import static consts.Const.DEBUG_MODE;

public class HTTPSProbe {
    private final int mUnicastPort;
    private final MulticastSender mMulticastSender;
    private final ServicesListener mServicesListener;
    private final SecretKey mAesKey;

    public static void main(String[] args) {
        HTTPSProbe httpsProbe = new HTTPSProbe(JsonUtility.getProtocolUnicastPort(Protocol.HTTPS), JsonUtility.getMulticastPort(), JsonUtility.getMulticastAddress(), new HTTPSStatusGetter(), JsonUtility.getAesKey());
        httpsProbe.start();
    }

    public HTTPSProbe(int unicastPort, int multicastPort, String multicastAddress, StatusGetter statusGetter, SecretKey aesKey) {
        mUnicastPort = unicastPort;

        mMulticastSender = new MulticastSender(multicastAddress, multicastPort, "IAMHERE" + " " + "https" + " " + mUnicastPort + "\r\n", 90*1000L); //IAMHERE every 90 seconds
        mServicesListener = new ServicesListener(mUnicastPort, multicastPort, multicastAddress, Protocol.HTTPS, statusGetter);
        mAesKey = aesKey;
    }

    public void start() {
        startServicesListener();
        startMulticastSender();
        handleUnicastConnection();
    }

    private void startMulticastSender() {
        new Thread(mMulticastSender).start();
    }

    private void startServicesListener() {
        new Thread(mServicesListener).start();
    }

    private void handleUnicastConnection()
    {
        try(ServerSocket listener = new ServerSocket(mUnicastPort))
        {
            while (true)
            {
                Socket worker = listener.accept();
                if (DEBUG_MODE) System.out.println("Client connected!");
                new Thread(new ClientConnection(worker, mServicesListener, mAesKey)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
