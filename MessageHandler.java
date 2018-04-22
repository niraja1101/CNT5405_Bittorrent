import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MessageHandler {
    
	
	public static void messageHandling(Handler handler){

		
		List<Message> msg_to_remove = new ArrayList<Message>();
	
		synchronized (handler.sc.messages_rcvd) {
		    Iterator<Message> it = handler.sc.messages_rcvd.iterator();
		    while (it.hasNext()) {
		        Message msg_incom = it.next();
		        Integer peerId = handler.clientIdtoPeerId.get(msg_incom.clID);
		        if(Message.checkHandshake(msg_incom, peerId))
		        	continue;
		        
		        
		        		if (peerId != null) {
		            if ((msg_incom.type != MESSAGE.BITFIELD) && 
		            		handler.remotePeers.get(peerId).has_rcvd_bit_field == false &&
		            				handler.remotePeers.get(peerId).has_rcvd_bit_field == true) {
		                continue;
		            }
		        }
		        
		       	Message.messageProcessing(handler, msg_incom, peerId);
		        msg_to_remove.add(msg_incom);
		    }

		    for (Message m : msg_to_remove) {
		        handler.sc.messages_rcvd.remove(m);
		    }
		}
	
    }
	
	
    
    
    
    
}
