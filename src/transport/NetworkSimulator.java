package transport;

import java.io.FileNotFoundException;
import java.util.*;
import java.io.FileReader;

public class NetworkSimulator {

    public static int DEBUG;

    public NetworkSimulator() {

    }

    /**
     * Main method
     *
     * @param args fileName timeBetweenSends lossProb corrProb winSize
     * protocolType tracing
     */
    public static void main(String[] args) {
        //checking to see if enough arguements have been sent    
        if (args.length < 6) {
            System.out.println("need at least 6 arguments");
            System.exit(1);
        }
        NetworkSimulator ns = new NetworkSimulator();
        if (args.length > 7) {
            ns.run(args[0],
                    Integer.parseInt(args[1]),
                    Float.parseFloat(args[2]),
                    Float.parseFloat(args[3]),
                    Integer.parseInt(args[4]),
                    Integer.parseInt(args[5]),
                    Integer.parseInt(args[6]));
        } else {
            ns.run(args[0],
                    Integer.parseInt(args[1]),
                    Float.parseFloat(args[2]),
                    Float.parseFloat(args[3]),
                    Integer.parseInt(args[4]),
                    Integer.parseInt(args[5]),
                    0);
        }
    }

    /**
     *
     * @param fileName file with messages
     * @param timeBetweenMsg time between messages
     * @param lossProb loss probability
     * @param corrProb corruption probability
     * @param windowsSize windows size
     * @param protocolType Go-back-N vs TCP 0 means go­back­n,1 means TCP
     * @param tracing Tracing: 0 will turn this off. 1 prints out times for
     * sending, receiving and timers expiring events. 2 prints out when a
     * message is corrupted and lost. Greater than 2 will display messages that
     * are related to the event timeline.
     * @return The total time taken to run the simulation
     */
    public int run(String fileName, int timeBetweenMsg,
            float lossProb, float corrProb,
            int windowsSize, int protocolType,
            int tracing) {
        //current event to process
        Event currentEvent;

        //reading in file line by line. Each line will be one message
        ArrayList<String> messageArray = readFile(fileName);
        //creating a new timeline with an average time between packets.
        Timeline tl = new Timeline(timeBetweenMsg, messageArray.size());
        //creating a new network layer with specific loss and curroption probability.
        NetworkLayer nl = new NetworkLayer(lossProb, corrProb, tl);
        SenderApplication sa = new SenderApplication(messageArray, nl);
        SenderTransport st = sa.getSenderTransport();
        //sender and receiver transport needs access to timeline to set timer.
        st.setTimeLine(tl);
        ReceiverTransport rt = new ReceiverTransport(nl);
        //setting window size
        st.setWindowSize(windowsSize);
        //setting protocol type
        st.setProtocol(protocolType);
        rt.setProtocol(protocolType);

        DEBUG = tracing;

        //this loop will run while there are events in the priority queue
        int totalTime = 0;
        while (true) {
            //get next event
            currentEvent = tl.returnNextEvent();

            //if no event present, break out
            if (currentEvent == null) {
                return totalTime;
            }
            totalTime = currentEvent.getTime();
            

            //if event is time to send a message, call the send message function of the sender application.   
            if (currentEvent.getType() == Event.MESSAGESEND) {
                if (DEBUG > 0) {
                    System.out.println("\n[NS] Message sending from sender to receiver at time " + currentEvent.getTime());
                }
                sa.sendMessage();
            } //if event is a message arrival
            else if (currentEvent.getType() == Event.MESSAGEARRIVE) {
                //if it arrives at the sender, call the get packet from the sender
                if (currentEvent.getHost() == Event.SENDER) {
                    if (DEBUG > 0) {
                        System.out.println("\n[NS] Message arriving from receiver to sender at time " + currentEvent.getTime());
                    }
                    st.receiveMessage(currentEvent.getPacket());
                } //if it arrives at the receiver, call the get packet from the receiver
                else {
                    if (DEBUG > 0) {
                        System.out.println("\n[NS] Message arriving from sender to receiver at time " + currentEvent.getTime());
                    }
                    rt.receiveMessage(currentEvent.getPacket());
                }
            } //If event is an expired timer, call the timerExpired method in the sender transport.
            else if (currentEvent.getType() == Event.TIMER) {
                if (DEBUG > 0) {
                    System.out.println("\n[NS] Timer expired at time " + currentEvent.getTime());
                }

                tl.stopTimer();
                st.timerExpired();
            } else if (currentEvent.getType() == Event.KILLEDTIMER) {//do nothing if it is just a turned off timer.
            } //this should not happen.
            else {
                System.out.println("Unidentified event type!");
                System.exit(1);
            }

        }
    }

    /**
     * Reading from file line by line
     *
     * @param fileName The name of the file
     * @return A list of string, each is a line from the file
     */
    public ArrayList<String> readFile(String fileName) {
        ArrayList<String> messageArray = new ArrayList<>();
        try {
            Scanner sc = new Scanner(new FileReader(fileName));
            while (sc.hasNextLine()) {
                messageArray.add(sc.nextLine());
            }
            return messageArray;
        } catch (FileNotFoundException e) {
            System.out.println("Could not open file " + e);
        }
        return new ArrayList<>();
    }

}
