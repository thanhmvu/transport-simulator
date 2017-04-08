package transport;

import java.util.LinkedList;

/**
 * A class which represents the receiver transport layer
 */
public class SenderTransport {

    private NetworkLayer nl;
    private Timeline tl;
    private int n; // window size
    private boolean usingTCP;
    private int nextSeqNum; // seg num of the next packet
    private int base;
    private int timeout;
    private LinkedList<Message> queue;
    private LinkedList<Message> unackedMsgs;
    private int cntDupAcks;

    public SenderTransport(NetworkLayer nl) {
        this.nl = nl;
        initialize();
    }

    /**
     * This routine will be called once, before any of other sender routines are
     * called. It can be used to do any required initialization.
     */
    public void initialize() {
        base = 0;
        nextSeqNum = 0;
        timeout = 20; // avg RTT = 10 time units
        queue = new LinkedList<>();
        unackedMsgs = new LinkedList<>();
        cntDupAcks = 0;
    }

    /**
     * This routine will be called whenever the upper layer at the sending side
     * has a message to send. The data in such a message should be delivered
     * in-order and correctly to the receiving side upper layer.
     *
     * @param msg message contains data to be sent to the other side (B-side)
     */
    public void sendMessage(Message msg) {
        if (nextSeqNum < base + n) { // Send message if the window is not full
            // buffer unacked msg
            unackedMsgs.add(msg);
            
            // start timer if needed
            if (this.base == this.nextSeqNum) {
                tl.startTimer(timeout);
            }

            Packet p = new Packet(msg.clone(), nextSeqNum, -1);
            nl.sendPacket(p, Event.RECEIVER);
            nextSeqNum++;

        } else { // Buffer message if full
            queue.add(msg);

            debug_print("Buffered message");
            debug_print("Current buffered messages: " + queue.size());
            // message should be sent when base increases
        }
    }

    /**
     * This routine will be called whenever a packet sent from the receiver
     * arrives at the sender. Packet is sent from the receiver (B-side) and is
     * possibly corrupted.
     *
     * @param pkt the receiving packet
     */
    public void receiveMessage(Packet pkt) {
        debug_print("Receive packet at sender; seqnum: " + pkt.getSeqnum() + " acknum: " + pkt.getAcknum());
        if (usingTCP) {
            receiveMessageTCP(pkt);
        } else {
            receiveMessageGBN(pkt);
        }
    }
    
    /**
     * This routine will be called whenever 
     * a GBN packet sent from the receiver arrives at the sender. 
     * Packet is sent from the receiver (B-side) and is possibly corrupted.
     * 
     * pkt - the receiving packet
     */
    public void receiveMessageGBN(Packet pkt) {
        if (!pkt.isCorrupt() && pkt.getAcknum() >= base) {
            // update unacked messages
            for (int i = base; i <= pkt.getAcknum(); i++) {
                unackedMsgs.removeFirst();
            }

            // move base + stop/ restart timer
            base = pkt.getAcknum() + 1;
            tl.stopTimer();
            if (base != this.nextSeqNum) {
                // restart timer if there is still unacked message
                tl.startTimer(timeout);
            }

            // Send buffered messages if there is any
            flushUnsentMsg();
        }
    }

    /**
     * This routine will be called whenever a TCP packet sent from the receiver
     * arrives at the sender. Packet is sent from the receiver (B-side) and is
     * possibly corrupted.
     *
     * @param pkt the receiving packet
     */
    public void receiveMessageTCP(Packet pkt) {
        if (!pkt.isCorrupt()) {
            if (pkt.getAcknum() > base) { // valid ack
                // update unacked messages
                for (int i = base; i < pkt.getAcknum(); i++) {
                    unackedMsgs.removeFirst();
                }

                // update variables 
                base = pkt.getAcknum();
                cntDupAcks = 0;
                tl.stopTimer();

                // restart if there is unacked message
                if (base != this.nextSeqNum) {
                    tl.startTimer(timeout);
                }

                // send buffered messages if there is any
                flushUnsentMsg();

            } else { // duplicate ack
                cntDupAcks++;
                if (cntDupAcks == 3) { // fast retransmit
                    resendFirstMsg();
                }
            }
        }
    }
    
    public int openWins() {
        return (base + n - nextSeqNum);
    }

    public void flushUnsentMsg() {
        while (!queue.isEmpty() && openWins() > 0) {
            debug_print("Current queuing messages: " + queue.size() + ", open windows: " + openWins());
            debug_print("Sending the next message in the queue");
            this.sendMessage(queue.pop());
        }
    }

    /**
     * This routine will be called when the sender's timer expires, thus
     * generating a timer interrupt. This routine should be used to control the
     * retransmission of packets. See starttimer() and stoptimer() for how the
     * timer is started and stopped.
     */
    public void timerExpired() {
        if (usingTCP) {
            resendFirstMsg();
        } else { //GBN
            resendAllMsgs();
        }
    }
    
    private void resendAllMsgs() {
        tl.startTimer(timeout);
        // resend all unacked messages
        int seqnum = base;
        for (Message msg : unackedMsgs) {
            Packet p = new Packet(msg.clone(), seqnum, -1);
            nl.sendPacket(p, Event.RECEIVER);
            seqnum++;
        }
    }

    private void resendFirstMsg() {
        // reset variables
        cntDupAcks = 0;
        tl.startTimer(timeout);

        // resend unacked message with smallest seqnum
        Packet p = new Packet(unackedMsgs.getFirst().clone(), base, -1);
        nl.sendPacket(p, Event.RECEIVER);
    }

    public void setTimeLine(Timeline tl) {
        this.tl = tl;
    }

    public void setWindowSize(int n) {
        this.n = n;
    }

    public void setProtocol(int n) {
        usingTCP = n > 0;
    }
    
    private void debug_print(String s){
        if (NetworkSimulator.DEBUG > 1) System.out.println("[ST] "+s);
    }
}
