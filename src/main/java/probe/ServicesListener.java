package probe;

import exceptions.ServiceNotFoundException;
import task.Protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

import static consts.Const.DEBUG_MODE;

public class ServicesListener implements Runnable {
    private int mFrequency;
    private final int mUnicastPort;
    private final int mMulticastPort;
    private final String mMulticastAddress;
    private final Protocol mProtocol;
    private final StatusGetter mStatusGetter;
    private final Map<String, StatusType> mServicesValues;
    private final Set<String> mServices;

    public ServicesListener(int unicastPort, int multicastPort, String multicastAddress, Protocol protocol, StatusGetter statusGetter) {
        mServicesValues = Collections.synchronizedMap(new HashMap<>());
        mServices = new HashSet<>();
        mStatusGetter = statusGetter;
        mProtocol = protocol;
        mUnicastPort = unicastPort;
        mMulticastPort = multicastPort;
        mMulticastAddress = multicastAddress;
    }

    /**
     * Listen for a new service. If there is already a listener for this service, it is simply ignored
     * @param id of the new service to listen to
     * @param augUrl of the new service
     * @param minVal minimum value expected from the service
     * @param maxVal maximum value expected from the service
     * @param frequency refresh rate expected for the service, in seconds
     */
    public void listenForNewService(String id, String augUrl, int minVal, int maxVal, int frequency) {
        if(mServices.add(id)) {
            mFrequency = Math.max(mFrequency, frequency);
            new Thread(() -> {
                while (true){
                    try {
                        StatusType currentStatus = mStatusGetter.getStatus(augUrl, minVal, maxVal);
                        mServicesValues.put(id, currentStatus);
                        serviceStatusUpdated(id);
                        Thread.sleep(frequency * 1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private synchronized void serviceStatusUpdated(String id) {
        notify();
    }

    @Override
    public void run() {
        byte[] message = ("NOTIFY" + " " + mProtocol.name().toLowerCase() + " " + mUnicastPort + "\r\n").getBytes();
        try {
            MulticastSocket socket = new MulticastSocket();
            InetAddress address = InetAddress.getByName(mMulticastAddress);
            socket.joinGroup(address);
            DatagramPacket packet = new DatagramPacket(message, message.length, address, mMulticastPort);
            while (true) {
                synchronized(this) {while (mServices.isEmpty() || !mServices.containsAll(mServicesValues.keySet())) wait();}
                if(DEBUG_MODE) System.out.println("Sending NOTIFY packet...");
                socket.send(packet);
                Thread.sleep(mFrequency * 1000L);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized StatusType getStatus(String id) throws ServiceNotFoundException, InterruptedException {
        if(!mServices.contains(id)) throw new ServiceNotFoundException();
        while (!mServicesValues.containsKey(id)) wait();
        return mServicesValues.get(id);
    }
}
