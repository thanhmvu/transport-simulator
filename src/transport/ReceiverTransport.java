package transport;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A class which represents the receiver transport layer
 */
public class ReceiverTransport {

    private ReceiverApplication ra;
    private NetworkLayer nl;
    private boolean usingTCP;
    private int cumulativeAckNum;
    private Set<Packet> buffer;

    public ReceiverTransport(NetworkLayer nl) {
        ra = new ReceiverApplication();
        this.nl = nl;
    }

    /**
     * This routine will be called once, before any of your other receiver
     * routines are called. It can be used to do any required initialization.
     */
    public final void initialize() {

    }

    /**
     * Initialize Go-Back-N routine
     */
    private void initializeGBN() {
        cumulativeAckNum = -1;
    }

    /**
     * Initialize TCP routine
     */
    private void initializeTCP() {
        cumulativeAckNum = 0;
        buffer = new TreeSet<>(new Comparator<Packet>() {
            @Override
            public int compare(Packet p1, Packet p2) {
                return p2.getSeqnum() - p1.getSeqnum();
            }

        });
    }

    /**
     * This routine will be called whenever a packet sent from the sender
     * arrives at the receiver
     *
     * @param pkt the (possibly corrupted) packet sent from the sender.
     */
    public void receiveMessage(Packet pkt) {
        if (!usingTCP) {
            receiveMessageGBN(pkt);
        } else {
            receiveMessageTCP(pkt);
        }
    }

    /**
     * Implement receive message for Go-Back-N routine
     *
     * @param pkt the (possibly corrupted) packet sent from the sender
     */
    private void receiveMessageGBN(Packet pkt) {
        if (!pkt.isCorrupt()) {
            if (pkt.getSeqnum() == cumulativeAckNum + 1) {
                this.sendPacketToApp(pkt);
            }
            sendAck();
        }
    }

    /**
     * Implement receive message for TCP routine
     *
     * @param pkt the (possibly corrupted) packet sent from the sender
     */
    private void receiveMessageTCP(Packet pkt) {
        if (!pkt.isCorrupt()) {
            if (pkt.getSeqnum() == cumulativeAckNum) {
                this.sendPacketToApp(pkt);
                //fill in the gap
                List<Packet> packetsToUnbuffer = new ArrayList<>();
                for (Packet p : buffer) {
                    if (p.getSeqnum() == cumulativeAckNum) {

                        packetsToUnbuffer.add(p);
                    } else {
                        break;
                    }
                }
                for (Packet p : packetsToUnbuffer) {
                    buffer.remove(p);
                    debugPrint("Remove packet seqnum " + p.getSeqnum() + "from buffer");
                    this.sendPacketToApp(pkt);
                }
            } else if (pkt.getSeqnum() > cumulativeAckNum) {
                buffer.add(pkt);
                debugPrint("Buffer packet seqnum " + pkt.getSeqnum());
                debugPrint("Number of receiver's buffered pkts: " + buffer.size());
            }
            sendAck();
        }
    }

    /**
     * Receive packet and send to app (also increase cumulative ACK num)
     *
     * @param pkt The packet that arrives in order
     */
    private void sendPacketToApp(Packet pkt) {
        cumulativeAckNum++;
        ra.receiveMessage(pkt.getMessage());
    }

    /**
     * Send the highest cumulative ack
     */
    private void sendAck() {
        Packet ackPkt = new Packet(new Message("ACK"), -1, cumulativeAckNum);
        nl.sendPacket(ackPkt, Event.SENDER);
    }

    /**
     * Set whether protocol is Go-back-N or TCP
     *
     * @param n if n > 0, use TCP, else use Go-back-N
     */
    public void setProtocol(int n) {
        usingTCP = n > 0;

        if (!usingTCP) {
            initializeGBN();
        } else {
            initializeTCP();
        }
    }

    /**
     * Print out debug code
     *
     * @param s
     */
    private void debugPrint(String s) {
        if (NetworkSimulator.DEBUG > 1) {
            System.out.println("[RT] " + s);
        }
    }

}
