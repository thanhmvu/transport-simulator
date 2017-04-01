package transport;

import java.util.*;

/**
 * A class which represents the transoprt layer for both sender and receiver.
 */

public class NetworkLayer {

    float lossProbability; //probablity of losing a packet
    float corrProbability; //probability of corruping a packet
    Timeline tl;
    Random ran; //random number generator for losing packets.

    /**
     * Create a network layer
     *
     * @param lp The loss probability
     * @param cp The corrupting probability
     * @param tl The timeline to send events to
     */
    public NetworkLayer(float lp, float cp, Timeline tl) {
        lossProbability = lp;
        corrProbability = cp;
        this.tl = tl;
        ran = new Random();
    }

    /**
     * Sending packet if it is not lost, and corrupting it if necessary.
     *
     * @param pkt The packet to be sent
     * @param to Who the packet is being sent to (Event.SENDER or
     * Event.RECEIVER)
     */
    public void sendPacket(Packet pkt, int to) {
        if (ran.nextDouble() < lossProbability) {
            if (NetworkSimulator.DEBUG > 1) {
                System.out.println("[NL] Packet seq:" + pkt.getSeqnum() + " ack: " + pkt.getAcknum() + " lost");
            }
            return;
        }
        if (ran.nextDouble() < corrProbability) {
            if (NetworkSimulator.DEBUG > 1) {
                System.out.println("[NL] Packet seq:" + pkt.getSeqnum() + " ack: " + pkt.getAcknum() + " corruped");
            }
            pkt.corrupt();
        }
        if (NetworkSimulator.DEBUG > 1) {
            System.out.println("[NL] Packet seq:" + pkt.getSeqnum() + " ack: " + pkt.getAcknum() + " sent");
        }
        tl.createArriveEvent(pkt, to);
    }

}
