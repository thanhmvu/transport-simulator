package transport;


import java.util.ArrayList;
/**
 * A class which represents the receiver transport layer
 */
public class SenderTransport
{
    private NetworkLayer nl;
    private Timeline tl;
    private int n;
    private boolean usingTCP;

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
    }
    
    /**
     * This routine will be called when the sender's timer expires,
     * thus generating a timer interrupt. 
     * This routine should be used to control the retransmission of packets. 
     * See starttimer() and stoptimer() for how the timer is started and stopped.
     */
    public void timerExpired()
    { 
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

}
