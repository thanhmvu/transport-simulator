package transport;

/**
 * A class which represents the receiver transport layer
 */
public class ReceiverTransport {

    private ReceiverApplication ra;
    private NetworkLayer nl;
    private boolean usingTCP;

    public ReceiverTransport(NetworkLayer nl) {
        ra = new ReceiverApplication();
        this.nl = nl;
        initialize();
    }

    /**
     * This routine will be called once, before any of your other receiver
     * routines are called. It can be used to do any required initialization.
     */
    public final void initialize() {
        if (!usingTCP) {
            initializeGBN();
        } else {
            initializeTCP();
        }
    }
    
    /**
     * Initialize Go-Back-N routine
     */
    private void initializeGBN() {
        
    }
    
    /**
     * Initialize TCP routine
     */
    private void initializeTCP() {
        
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
     * @param pkt the (possibly corrupted) packet sent from the sender
     */
    private void receiveMessageGBN(Packet pkt) {
        
    }
    
    /**
     * Implement receive message for TCP routine
     * @param pkt the (possibly corrupted) packet sent from the sender
     */
    private void receiveMessageTCP(Packet pkt) {
        
    }

    public void setProtocol(int n) {
        if (n > 0) {
            usingTCP = true;
        } else {
            usingTCP = false;
        }
    }

}
