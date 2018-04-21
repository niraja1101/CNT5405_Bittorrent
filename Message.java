import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class Message {
	
	public enum MESSAGE{
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
	
	public static MESSAGE getMsgType(byte b) {
		
		if(b==0) return MESSAGE.CHOKE;
		else if(b==1) return MESSAGE.UNCHOKE;	
        
		return null;
    }

	private int length;
	private byte[] lengthB;
	private MESSAGE type;
	private byte[] payload;
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
		 byte[] messageType=ByteBuffer.allocate(1).putInt(type.messageValue).array();
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
	
	public synchronized static void sendHandShakeMessage(Handler handler,int peerId){
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
        byte[] handShakeArray = handShakeBuffer.array();
        sendMessage(handler,handShakeArray,peerId);
    	}
	
	  public static void sendNotInterested(Handler handler, int index ) {
	        sendMessage(handler,getMessage(MESSAGE.INTERESTED), handler.peerIdList.get(index));
	  }
	  
	  public static void sendBitfield(Handler handler, int peerId) {
	        byte[] bitfieldMessage = getMessage(handler.fh.bitfield.length, MESSAGE.BITFIELD, handler.fh.bitfield);
	        sendMessage(handler,bitfieldMessage, peerId);
	    }

}
