package threads;

import command.Command;
import command.CommandType;
import task.Protocol;
import task.Task;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import static consts.Const.DEBUG_MODE;

public class MulticastListener implements Runnable {
    private final String mGroup;
    private final int mPort;
    private final byte[] mReceivedData;
    private final BlockingQueue<Task> mTasks;
    private final Set<Protocol> mProtocolsSupervised;

    public MulticastListener(String group, int port, BlockingQueue<Task> tasks, Set<Protocol> protocolsSupervised) {
        mProtocolsSupervised = protocolsSupervised;
        mTasks = tasks;
        mGroup = group;
        mPort = port;
        mReceivedData = new byte[1024];
    }

    @Override
    public void run() {
        try {
            if(DEBUG_MODE) System.out.println("Listening probe packet...");
            MulticastSocket serverSocket = new MulticastSocket(mPort);
            serverSocket.joinGroup(InetAddress.getByName(mGroup));

            while(true)
            {
                DatagramPacket receivePacket = new DatagramPacket(mReceivedData, mReceivedData.length);
                serverSocket.receive(receivePacket);
                String received = new String(receivePacket.getData()).substring(0, receivePacket.getLength()-2);
                if(DEBUG_MODE) System.out.println("Received from " + receivePacket.getAddress() + " : " + received);

                Command command = new Command(received);
                if(command.isSyntaxicallyCorrect()) {
                    switch (command.getType()) {
                        case IAMHERE:
                            mTasks.put(new Task(receivePacket.getAddress(),
                                    Integer.parseInt(command.getMatcher().group("port")),
                                    CommandType.CURCONFIG, Protocol.valueOf(command.getMatcher().group("protocol").toUpperCase())));
                            mProtocolsSupervised.add(Protocol.valueOf(command.getMatcher().group("protocol").toUpperCase()));
                            break;
                        case NOTIFY:
                            mTasks.put(new Task(receivePacket.getAddress(),
                                    Integer.parseInt(command.getMatcher().group("port")),
                                    CommandType.STATEREQ, Protocol.valueOf(command.getMatcher().group("protocol").toUpperCase())));
                            break;
                    }
                }
                else if (DEBUG_MODE) System.out.println(command.getError());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
