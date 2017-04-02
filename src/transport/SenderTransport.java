package transport;

import java.util.ArrayList;
import java.util.LinkedList;
/**
 * A class which represents the receiver transport layer
 */
public class SenderTransport
{
    private NetworkLayer nl;
    private Timeline tl;
    private int n; // window size
    private boolean usingTCP;
    private int nextSeqNum; // seg num of the next packet
    private int base;
    private int timeout; 
    private LinkedList<Message> buffer;
    private LinkedList<Message> unackedBuffer;
    
    public SenderTransport(NetworkLayer nl){
        this.nl=nl;
        initialize();
    }

    /**
     * This routine will be called once, 
     * before any of other sender routines are called. 
     * It can be used to do any required initialization. 
     */
    public void initialize()
    {
        base = 0;
        nextSeqNum = 0;
        timeout = 20; // avg RTT = 10 time units
        buffer = new LinkedList<Message>();
        unackedBuffer = new LinkedList<Message>();
    }
    
    /**
     * This routine will be called whenever 
     * the upper layer at the sending side has a message to send. 
     * The data in such a message should be delivered 
     * in-order and correctly to the receiving side upper layer.
     * 
     * msg - message contains data to be sent to the other side (B-side)
     */
    public void sendMessage(Message msg)
    {
//        debug_print("Requested to send: "+ msg.getMessage());
        
        if(usingTCP){ //TPC
            
        } else { //GBN
            if(nextSeqNum < base + n){ // Send message if the window is not full
                // start timer if needed
                if(this.base == this.nextSeqNum){
                    tl.startTimer(timeout);
                }
                
                int ackNum = -1;
                Packet p = new Packet(msg, nextSeqNum, ackNum);
                nl.sendPacket(p, Event.RECEIVER);
                nextSeqNum ++;
                
                // buffer unacked msg
                unackedBuffer.add(msg);
            } 
            else { // Buffer message if full
                buffer.add(msg); 
                
                debug_print("Buffered message");
                // message should be sent when base increases
            }
        }
    }
    
    /**
     * This routine will be called whenever 
     * a packet sent from the receiver arrives at the sender. 
     * Packet is sent from the receiver (B-side) and is possibly corrupted.
     * 
     * pkt - the receiving packet
     */
    public void receiveMessage(Packet pkt)
    {
        //GBN
        if(!usingTCP && !pkt.isCorrupt() && pkt.getAcknum() >= base){
            // update unacked messages
            for(int i = base; i <= pkt.getAcknum(); i++){
                unackedBuffer.removeFirst();
            }
            
            // move base + stop timer
            base = pkt.getAcknum() + 1;
            tl.stopTimer();
            if(base != this.nextSeqNum){ 
                // restart timer if there is still unacked message
                tl.startTimer(timeout);
            }
            
            // Send buffered messages if there is any
            int opening = base + n - nextSeqNum;
            for(int i = 0; i < Math.min(opening, buffer.size()); i++){
                debug_print("Sending buffered message in the queue");
                this.sendMessage(buffer.pop());
            }
        }
        //TCP
        if(usingTCP){
            // check for 2 duplicate acks
        }
    }
    
    /**
     * This routine will be called when the sender's timer expires,
     * thus generating a timer interrupt. 
     * This routine should be used to control the retransmission of packets. 
     * See starttimer() and stoptimer() for how the timer is started and stopped.
     */
    public void timerExpired()
    { 
        if(usingTCP){
        
        } else { //GBN
            tl.startTimer(timeout);
            // resend all unacked messages
            int seqnum = base;
            for(Message msg: unackedBuffer){
                int ackNum = -1;
                Packet p = new Packet(msg, seqnum, ackNum);
                nl.sendPacket(p, Event.RECEIVER);
                seqnum ++;
            }
        }
    }

    public void setTimeLine(Timeline tl)
    {
        this.tl=tl;
    }

    public void setWindowSize(int n)
    {
        this.n=n;
    }

    public void setProtocol(int n)
    {
        if(n>0)
            usingTCP=true;
        else
            usingTCP=false;
    }
    
    public void debug_print(String s){
        if (NetworkSimulator.DEBUG == 3) System.out.println("[ST] "+s);
    }
}
