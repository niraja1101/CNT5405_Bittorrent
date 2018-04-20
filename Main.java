import com.sun.org.apache.xml.internal.serializer.utils.SystemIDResolver;

import java.util.*;

public class Main{

    public static void main( String args[])
    {
        CommonConfigParser commonparser = new CommonConfigParser();
        commonparser.saveCommonConfig();
        System.out.println("PreferredNeighbors= "+ commonparser.NumberOfPreferredNeighbors);
        System.out.println("Unchoking Interval= "+ commonparser.UnchokingInterval);
        System.out.println("OptimisticUnchokingInterval= "+ commonparser.OptimisticUnchokingInterval);
        System.out.println("Filename= "+ commonparser.Filename);
        System.out.println("filesize= "+ commonparser.filesize);
        System.out.println("piecesize= "+ commonparser.piecesize);

        PeerConfigParser peerparser = new PeerConfigParser();
        peerparser.savepeerinfo();
        LinkedList <RemPeer>newlist = peerparser.getneighborinfo();

       
        }


    }
}
