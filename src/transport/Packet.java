package transport;

import java.util.*;

/**
 * A class which represents a packet
 */
public class Packet {

    private Message msg; //the enclosed message
    private int seqnum; //packets seq. number
    private int acknum; //packet ack. number
    private int checksum; //packet checksum

    Random ran; //random number generator

    /*========= 
    Thanh: may be we should use setChecksum to get the checksum 
    instead of letting the transport layer to pass it in
    =========*/
    public Packet(Message msg, int seqnum, int acknum) {
        this.msg = msg;
        this.seqnum = seqnum;
        this.acknum = acknum;
        this.setChecksum();  // Thanh: Maybe this.setChecksum() instead?
        this.ran = new Random();
    }

    public int getAcknum() {
        return acknum;
    }

    public int getSeqnum() {
        return seqnum;
    }

    public Message getMessage() {
        return msg;
    }

    /**
     * Sets the checksum field to have a valid value
     */
    public void setChecksum() {
    }

    /**
     * Uses the checksum field to check if the packet is corrupt or not.
     * @return true if the packet is corrupted, false if not
     */
    public boolean isCorrupt() {
        return false;
    }

    /**
     * This method corrupts the packet the following way: corrupt the message
     * with a 75% chance corrupt the seqnum with 12.5% chance corrupt the acknum
     * with 12.5% chance
     */
    public void corrupt() {
        if (ran.nextDouble() < 0.75) {
            this.msg.corruptMessage();
        } else if (ran.nextDouble() < 0.875) {
            this.seqnum = this.seqnum + 1;
        } else {
            this.acknum = this.acknum + 1;
        }

    }

}
