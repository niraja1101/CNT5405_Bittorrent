/*import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import Message.MESSAGE;

public class MessageHandler {
    
	
	public static void messageHandling(Handler handler){

		
		List<Message> msg_to_remove = new ArrayList<Message>();
	
		synchronized (handler.sc.messages_rcvd) {
		    Iterator<Message> it = handler.sc.messages_rcvd.iterator();
		    while (it.hasNext()) {
		        Message msg_incom = it.next();
		        Integer peerId = handler.peerIdList.get(msg_incom.clID);
		        if(checkHandshake(msg_incom, msg_index))
		        	continue;
		        
		       // msg_index = findIndex(pd, msg_incom, msg_index);
		        if (peerId != null) {
		            if (((int)msg_incom.msg_type != Message.bitfield) && 
		            		pd.peer_neighbours[msg_index].has_rcvd_bit_field == false &&
		            		pd.peer_neighbours[msg_index].has_rcvd_handshake == true) {
		                continue;
		            }
		        }
		        
		       	messageProcessing(handler, msg_incom, peerId);
		        msg_to_remove.add(msg_incom);
		    }

		    for (Message m : msg_to_remove) {
		        handler.sc.messages_rcvd.remove(m);
		    }
		}
	
    }
	
	private static void messageProcessing(Handler handler, Message msg_incom, int msg_index) {

		 if(MESSAGE.BITFIELD==)
	}
    
    
    
    
}
*/