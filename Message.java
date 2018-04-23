import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;


enum MESSAGE{
	CHOKE((byte)0),
	UNCHOKE((byte)1),
	INTERESTED((byte)2),
	NOT_INTERESTED((byte)3),
	HAVE((byte)4),
	BITFIELD((byte)5),
	REQUEST((byte)6),
	PIECE((byte)7),
	HANDSHAKE((byte)8);

	byte messageValue = -1;

	MESSAGE(byte b){
		this.messageValue = b;
	}
}


public class Message {

	public static MESSAGE getMsgType(byte b) {

		if(b==0) return MESSAGE.CHOKE;
		else if(b==1) return MESSAGE.UNCHOKE;
		else if(b==2) return MESSAGE.INTERESTED;
		else if(b==3) return MESSAGE.NOT_INTERESTED;
		else if(b==4) return MESSAGE.HAVE;
		else if(b==5) return MESSAGE.BITFIELD;
		else if(b==6) return MESSAGE.REQUEST;
		else if(b==7) return MESSAGE.PIECE;
		else if(b==8) return MESSAGE.HANDSHAKE;
		else return null;
	}

	public static  void messageProcessing(Handler handler, Message msg_incom, Integer peerId) {

		if(msg_incom.type==MESSAGE.CHOKE){
            chokeMessage(handler, peerId);
		}
		else if(msg_incom.type == MESSAGE.UNCHOKE){
            unchokeMessage(handler, peerId);
        }
        else if(msg_incom.type == MESSAGE.INTERESTED){
            interestedNeighborMessage(handler, peerId);
        }
        else if(msg_incom.type == MESSAGE.NOT_INTERESTED){
            uninterestedMessage(handler, peerId);
        }
        else if(msg_incom.type == MESSAGE.HAVE){
            haveMessageHandler(handler, msg_incom, peerId);
        }
        else if(msg_incom.type == MESSAGE.REQUEST){
            bitFieldHandler(handler, msg_incom, peerId);
        }
        else if(msg_incom.type == MESSAGE.REQUEST){
            requestMessage(handler, msg_incom, peerId);
        }
        else if(msg_incom.type == MESSAGE.PIECE){
            pieceMessageHandler(handler, msg_incom, peerId);
        }
		else if(msg_incom.type==MESSAGE.HANDSHAKE){
			handShakeHandler(handler, msg_incom, peerId);
		}

	}

	public int length;
	public byte[] lengthB;
	public MESSAGE type;
	public byte[] payload;
	public int clID = -1;

	public Message(int length, byte type, byte[] payload, int clientId) {
		this.lengthB = ByteBuffer.allocate(4).putInt(length).array();
		this.type = getMsgType(type);
		this.length = length;
		this.clID=clientId;
		if (this.type==MESSAGE.CHOKE||this.type==MESSAGE.UNCHOKE || this.type==MESSAGE.INTERESTED || this.type==MESSAGE.NOT_INTERESTED) {
			this.payload=null;
		}
		else {
			this.payload = new byte[length];
			this.payload = payload;
		}

	}


	public Message(byte[] data, int clientId){

		byte[] message_payload = Arrays.copyOfRange(data, 5, data.length);
		int message_length = ByteBuffer.allocate(4).put(Arrays.copyOfRange(data, 0, 4)).getInt(0);
		this.type = type;
		this.payload = payload;
		this.clID=clientId;
	}

	public static void sendMessage(Handler handler,byte[] message, int peerId) {
		try {
			handler.outputStream.get(peerId).writeObject(message);
			handler.outputStream.get(peerId).flush();
		}
		catch(IOException e){
			System.err.println("Message not sent error.");
		}
	}

	//public static void createMessage(int length,)

	public static byte[] getMessage(MESSAGE  type){
		byte[] length=ByteBuffer.allocate(4).putInt(1).array();
		ByteBuffer bf=ByteBuffer.allocate(5);
		byte[] messageType=ByteBuffer.allocate(1).putInt(type.messageValue).array();
		bf.put(length);
		bf.put(messageType);
		return bf.array();
	}

	/* public static byte[] getMessage(byte[] data, MESSAGE type){
	        int dataLength=ByteBuffer.wrap(data).getInt();
	        byte[] messageLength=ByteBuffer.allocate(4).putInt(dataLength+1).array();
	        ByteBuffer bf=ByteBuffer.allocate(data.length+5);
	        byte[] messageType=ByteBuffer.allocate(1).putInt(type.messageValue).array();
	        bf.put(messageLength);
	        bf.put(messageType);
	        bf.put(data);
	        return bf.array();
	    }
	 */
	public static byte[] getMessage(int length, MESSAGE type,byte[] data ){
		byte[] dataLength=ByteBuffer.allocate(4).putInt(length).array();
		byte[] messageType=ByteBuffer.allocate(1).put(type.messageValue).array();
		ByteBuffer bf=ByteBuffer.allocate(data.length+5);
		bf.put(dataLength);
		bf.put(messageType);
		bf.put(data);
		return bf.array();
	}

	public static byte[] getMessage(byte[] data, MESSAGE type){
		int dataLength=ByteBuffer.wrap(data).getInt();
		byte[] messageLength=ByteBuffer.allocate(4).putInt(dataLength+1).array();
		ByteBuffer bf=ByteBuffer.allocate(data.length+5);
		byte[] messageType=ByteBuffer.allocate(1).putInt(type.messageValue).array();
		bf.put(messageLength);
		bf.put(messageType);
		bf.put(data);
		return bf.array();
	}

	public static void sendRequestMessage(Handler handler, int piece, int peerId){
		byte[] pieceIndex = ByteBuffer.allocate(4).putInt(piece).array();
		byte[] message=getMessage(pieceIndex,MESSAGE.REQUEST);
		sendMessage(handler, message, peerId);
	}

	public static void sendHandShakeMessage(Handler handler,int peerId){
		byte[] hnd_shake_hdr = new byte[18];
		try {
			hnd_shake_hdr = "P2PFILESHARINGPROJ".getBytes("UTF-8");
		} catch (Exception e) {

		}
		byte[] zero_bits = new byte[10];
		byte[] peer_id_Arr = ByteBuffer.allocate(4).putInt(handler.peerId).array();

		ByteBuffer handShakeBuffer = ByteBuffer.allocate(32);

		handShakeBuffer.put(hnd_shake_hdr);
		handShakeBuffer.put(zero_bits);
		handShakeBuffer.put(peer_id_Arr);
		handShakeBuffer.clear();
		byte[] handShakeArray = handShakeBuffer.array();
		sendMessage(handler,handShakeArray,peerId);
	}

    private static void bitFieldHandler(Handler handler, Message incomingMessage, int peerId) {
        handler.remotePeers.get(peerId).bit_field_map = incomingMessage.payload;
        handler.remotePeers.get(peerId).has_rcvd_bit_field = true;
        if (checkNeededPieces(handler.remotePeers.get(peerId),handler)) {
            Message.sendInterested(peerId,handler);
        } else {
            Message.sendNotInterested(handler, peerId);
        }
    }

	public static void sendNotInterested(Handler handler, int index ) {
		sendMessage(handler,getMessage(MESSAGE.NOT_INTERESTED), handler.peerIdList.get(index));
	}

	public static void sendBitfield(Handler handler, int peerId) {
		byte[] bitfieldMessage = getMessage(handler.fh.bitfield.length, MESSAGE.BITFIELD, handler.fh.bitfield);
		sendMessage(handler,bitfieldMessage, peerId);
	}

	public static void sendInterested(int index, Handler handler) {

        sendMessage(handler,getMessage(MESSAGE.INTERESTED), handler.peerIdList.get(index));
	}

    private static void pieceMessageHandler(Handler handler, Message incomingMessage, int msg_index) {
        handler.fh.file_pieces[handler.remotePeers.get(msg_index).piece_num] = incomingMessage.payload;

        handler.remotePeers.get(msg_index).is_waiting_for_piece = false;

        BigInteger tempField = new BigInteger(handler.fh.bitfield);

        tempField = tempField.setBit(handler.remotePeers.get(msg_index).piece_num);

        handler.fh.bitfield = tempField.toByteArray();

        //handler.recieved_data[msg_index] += handler.size_of_piece;
        //handler.writelogs.pieceDownloaded(handler.other_peer_Ids[handler.my_clID], handler.peer_neighbours[msg_index].peerId, handler.peer_neighbours[msg_index].piece_num, ++handler.tot_pieces);

        boolean haveFile = true;
        for (int i = 0; i < handler.number_of_bits; i++) {
            if (!tempField.testBit(i)) {
                haveFile = false;
                break;
            }
        }

        handler.hasFile.set((handler.clientId),haveFile);

        if (haveFile){
            //handler.writelogs.fileDownloaded(handler.my_peer_Id);
        }
        int i2 = 0;
        for(int peerId:handler.peerIdList){
            if( i2 == handler.clientId)
                continue;
            Message.sendHave(i2, handler.remotePeers.get(msg_index).piece_num, handler);
            i2++;
        }

        handler.remotePeers.get(msg_index).piece_num = -1;



        int j = 0;
        for(AdjacentPeers peerId:handler.remotePeers){
            if (j == handler.clientId)
                continue;

            boolean interested = false;

            if (handler.remotePeers.get(j).bit_field_map != null)
                interested = checkNeededPieces(handler.remotePeers.get(j), handler);

            if (!interested)
                Message.sendNotInterested(handler, j);
            j++;
        }

        int j2=0;
        boolean all_have_file = true;
        for(int peerId:handler.peerIdList){
            if(!handler.hasFile.get(j2)){
                all_have_file = false;
            }
            j2++;
        }

        handler.every_peer_has_file = all_have_file;
    }

    public static boolean checkNeededPieces(AdjacentPeers neighbor,Handler handler) {
        BigInteger self_field = new BigInteger(handler.fh.bitfield);
        BigInteger neighbour_field = new BigInteger(neighbor.bit_field_map);

        if (neighbour_field.and(self_field.and(neighbour_field).not()).doubleValue() > 0) {
            return true;
        }
        return false;
    }




	public static void handShakeHandler(Handler handler, Message incomingMessage, Integer msg_index) {
		System.out.println("only fucking here!");
		handler.clientIdtoPeerId.put(incomingMessage.clID, incomingMessage.length);
		System.out.println("here!");
		for (int i = 0; i < handler.peerIdList.size(); i++) {
			if (handler.peerIdList.get(i) == incomingMessage.length) {
				msg_index = i;
				System.out.println("shit here");
				break;
			}
		}
		handler.remotePeers.get(msg_index).has_rcvd_handshake = true;
	}

	public static boolean checkHandshake(Message incomingMessage, Integer peerId) {
		return (peerId == null && !(incomingMessage.type == MESSAGE.HANDSHAKE));
	}



	public static void messageHandling(Handler handler){
		List<Message> msg_to_remove = new ArrayList<Message>();

		synchronized (handler.sc.messages_rcvd) {

			Iterator<Message> it = handler.sc.messages_rcvd.iterator();
			while (it.hasNext()) {
				Message msg_incom = it.next();
				Integer peerId = handler.clientIdtoPeerId.get(msg_incom.clID);
				if(Message.checkHandshake(msg_incom, peerId))
					continue;

				System.out.println(msg_incom.type);
				messageProcessing(handler, msg_incom, peerId);
				msg_to_remove.add(msg_incom);
			}

			for (Message m : msg_to_remove) {
				handler.sc.messages_rcvd.remove(m);
			}
		}
	}


	private static void chokeMessage(Handler handler, Integer peerId) {
        int index = findIndex(handler, peerId);
		handler.remotePeers.get(index).is_choked = true;
		//	  pd.writelogs.chokedMsgType(pd.other_peer_Ids[pd.my_clID], pd.peer_neighbours[msg_index].peerId);
	}

	private static void unchokeMessage(Handler handler, Integer peerId) {
	    int index = findIndex(handler, peerId);
		handler.remotePeers.get(index).is_choked = true;
		//	  pd.writelogs.unchokedMsgType(pd.other_peer_Ids[pd.my_clID], pd.peer_neighbours[msg_index].peerId);
	}

	static public int findIndex(Handler handler, Integer peerId){

	    int temp=-1;
	    for(int i=0;i<handler.peerIdList.size();i++)
        {
            if(handler.peerIdList.get(i)==peerId){
                temp=i;
                break;
            }
        }
        return temp;

    }

	private static void interestedNeighborMessage(Handler handler, Integer peerId) {
        int index = findIndex(handler, peerId);
		handler.remotePeers.get(index).is_interested = true;
		//	pd.writelogs.interestedMsgType(pd.other_peer_Ids[pd.my_clID], pd.peer_neighbours[msg_index].peerId);
	}

	private static void uninterestedMessage(Handler handler, Integer peerId) {
        int index = findIndex(handler, peerId);
		handler.remotePeers.get(index).is_interested = false;
		//	pd.writelogs.notInterestedMsgType(pd.other_peer_Ids[pd.my_clID], pd.peer_neighbours[msg_index].peerId);
	}

	private static void requestMessage(Handler handler, Message incomingMessage, Integer peerId) {
		ByteBuffer buffer = ByteBuffer.wrap(incomingMessage.payload);
		int pieceNumber = buffer.getInt();
		Message.sendFilePiece(handler,pieceNumber ,peerId);
	}

	public static void sendFilePiece(Handler handler, int pieceNumber, Integer peerId) {
		sendMessage(handler, getMessage(handler.fh.pieceSize, MESSAGE.PIECE, handler.fh.file_pieces[pieceNumber]),peerId);
	}

	public static void sendHave(int index, int pieceNumber , Handler handler) {
		byte[] pieceIndex = ByteBuffer.allocate(4).putInt(pieceNumber).array();
		sendMessage(handler,getMessage(4,MESSAGE.HAVE,pieceIndex), index );
	}


	private static void haveMessageHandler(Handler pd, Message incomingMessage, int msg_index) {
		BigInteger tempField = new BigInteger(pd.remotePeers.get(msg_index).bit_field_map);
		ByteBuffer buffer = ByteBuffer.wrap(incomingMessage.payload);

		int this_indx = buffer.getInt();
		tempField = tempField.setBit(this_indx);

		pd.remotePeers.get(msg_index).bit_field_map = tempField.toByteArray();

		boolean neighborHasFile = true;
		for (int i = 0; i < pd.number_of_bits; i++) {
			if (!tempField.testBit(i)) {
				neighborHasFile = false;
				break;
			}
		}

		pd.hasFile.set(msg_index, neighborHasFile);

		boolean temp = true;

        for(Integer peerId:pd.peerIdList){
		    if(!pd.hasFile.get(peerId)){
		        temp = false;
		        break;
            }
        }

		pd.every_peer_has_file = temp;
		//pd.writelogs.haveMsgType(pd.other_peer_Ids[pd.my_clID], pd.peer_neighbours[msg_index].peerId, this_indx);

		BigInteger myField = new BigInteger(pd.fh.bitfield);
		if (!myField.testBit(this_indx))
			Message.sendInterested(msg_index, pd);
	}

}