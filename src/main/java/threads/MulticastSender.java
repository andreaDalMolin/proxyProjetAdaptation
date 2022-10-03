package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import static consts.Const.DEBUG_MODE;

public class MulticastSender implements Runnable {
    private final String mMulticastAddress;
    private final int mMulticastPort;
    private final String mMessage;
    private final long mInterval;

    public MulticastSender(String multicastAddress, int multicastPort, String message, long interval) {
        mMulticastAddress = multicastAddress;
        mMulticastPort = multicastPort;
        mMessage = message;
        mInterval = interval;
    }

    @Override
    public void run() {
        try {
            MulticastSocket socket = new MulticastSocket();
            InetAddress address = InetAddress.getByName(mMulticastAddress);
            socket.joinGroup(address);
            DatagramPacket packet = new DatagramPacket(mMessage.getBytes(), mMessage.length(), address, mMulticastPort);
            while (true) {
                if(DEBUG_MODE) System.out.println("Sending UDP packet...");
                socket.send(packet);
                Thread.sleep(mInterval);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
