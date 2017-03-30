package transport;

import java.io.FileNotFoundException;
import java.util.*;
import java.io.FileReader;

public class NetworkSimulator {

    public static int DEBUG;

    /**
     * Main method with follwing variables
     *
     *
     * @param args[0] file with messages
     * @param args[1] time between messages
     * @param args[2] loss probability
     * @param args[3] corruption probability
     * @param args[4] windows size
     * @param args[5] Go-back-N vs TCP 0 means go­back­n,1 means TCP
     * @param args[6] Tracing: 0 will turn this off. 1 prints out times for
     * sending, receiving and timers expiring events. 2 prints out when a
     * message is corrupted and lost. Greater than 2 will display messages that
     * are related to the event timeline.
     */
    public static void main(String[] args) {
        //current event to process
        Event currentEvent;
        //checking to see if enough arguements have been sent    
        if (args.length < 5) {
            System.out.println("need at least 5 arguements");
            System.exit(1);
        }
        //reading in file line by line. Each line will be one message
        ArrayList<String> messageArray = readFile(args[0]);
        //creating a new timeline with an average time between packets.
        Timeline tl = new Timeline(Integer.parseInt(args[1]), messageArray.size());
        //creating a new network layer with specific loss and curroption probability.
        NetworkLayer nl = new NetworkLayer(Float.parseFloat(args[2]), Float.parseFloat(args[3]), tl);
        SenderApplication sa = new SenderApplication(messageArray, nl);
        SenderTransport st = sa.getSenderTransport();
        //sender and receiver transport needs access to timeline to set timer.
        st.setTimeLine(tl);
        ReceiverTransport rt = new ReceiverTransport(nl);
        //setting window size
        st.setWindowSize(Integer.parseInt(args[4]));
        //setting protocol type
        st.setProtocol(Integer.parseInt(args[5]));
        rt.setProtocol(Integer.parseInt(args[5]));
        DEBUG = Integer.parseInt(args[6]);
        //this loop will run while there are events in the priority queue
        while (true) {
            //get next event
            currentEvent = tl.returnNextEvent();
            //if no event present, break out
            if (currentEvent == null) {
                break;
            }
            //if event is time to send a message, call the send message function of the sender application.   
            if (currentEvent.getType() == Event.MESSAGESEND) {
                sa.sendMessage();
                if (DEBUG > 0) {
                    System.out.println("Message sent from sender to receiver at time " + currentEvent.getTime());
                }
            } //if event is a message arrival
            else if (currentEvent.getType() == Event.MESSAGEARRIVE) {
                //if it arrives at the sender, call the get packet from the sender
                if (currentEvent.getHost() == Event.SENDER) {
                    if (DEBUG > 0) {
                        System.out.println("Message arriving from receiver to sender at time " + currentEvent.getTime());
                    }
                    st.receiveMessage(currentEvent.getPacket());
                } //if it arrives at the receiver, call the get packet from the receiver
                else {
                    if (DEBUG > 0) {
                        System.out.println("Message arriving from sender to receiver at time " + currentEvent.getTime());
                    }
                    rt.receiveMessage(currentEvent.getPacket());
                }
            } //If event is an expired timer, call the timerExpired method in the sender transport.
            else if (currentEvent.getType() == Event.TIMER) {
                if (DEBUG > 0) {
                    System.out.println("Timer expired at time " + currentEvent.getTime());
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

    //reading in file line by line.
    public static ArrayList<String> readFile(String fileName) {
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
