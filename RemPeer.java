import java.util.*;
import java.util.concurrent.atomic.*;

public class RemPeer {

    public final int peer_id;
    public final String peer_hostname;
    public final boolean peer_has_file;
    public final int peer_port;
    public AtomicBoolean interested  = new AtomicBoolean (false);
    public AtomicInteger download_Bytes = new AtomicInteger (0);
    public BitSet received_Parts;

    public RemPeer(int peerid, String hostname, int port,boolean hasfile)
    {
        this.peer_has_file=hasfile;
        this.peer_id=peerid;
        this.peer_port=port;
        this.peer_hostname=hostname;
    }

    //Used to print the Adjacent peer object
    public String displayPeerobj() {
        return new StringBuilder (peer_id)
                .append (" peer address:").append (peer_hostname)
                .append(" peer port: ").append(peer_port).toString();
    }

    //Used to compare to objects
    public boolean isRemPeer (Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof RemPeer) {

            return (((RemPeer) obj).peer_id == peer_id);

        }
        return false;
    }

    //This functions needs to be overridden when over-riding the equals file
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.peer_id);
        return hash;
    }


}
