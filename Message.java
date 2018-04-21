
import java.util.*;
import java.math.BigInteger;
import java.nio.*;
import java.io.IOException;



public class Message {


    public enum MESSAGE{
        CHOKE((byte)0),
        UNCHOKE((byte)1),
        INTERESTED((byte)2),
        NOT_INTERESTED((byte)3),
        HAVE((byte)4),
        BITFIELD((byte)5),
        REQUEST((byte)6),
        PIECE((byte)7);

        byte messageValue = -1;

        MESSAGE(byte b){
            this.messageValue = b;
        }
    }


    public MESSAGE msgTyp;

    public int myClientID = -1;
    public int len;
    public byte[] lenInB;
    public byte[] msgPayLd;

    public Message(){}

    public Message(int len, MESSAGE type, byte[] payload, int clientID) {
        this(len, type, payload);
        this.myClientID = clientID;
    }

    public Message(int len, MESSAGE type, byte[] payload) {
        this.lenInB = ByteBuffer.allocate(4).putInt(len).array();
        this.msgTyp = type;
        this.len = len;

        if (hasPayload(type)) {
            this.msgPayLd = new byte[len];
            this.msgPayLd = payload;
        }
        else {
            this.msgPayLd = null;
        }
    }

    public Message(int len, byte[] data) {
        this.len = len;
        //this.msgTyp = data[0];
        this.msgTyp = getMessagetype(data[0]);

        if(hasPayload(msgTyp)) {
            msgPayLd = new byte[len];
            System.arraycopy(data, 1, msgPayLd, 0, len);
        }
        else {
            msgPayLd = null;
        }
    }

    public MESSAGE getMessagetype(Byte data){

        MESSAGE msgTyp = null;
        if (data == (byte) 0){
            this.msgTyp = MESSAGE.CHOKE;
        }
        else if (data == (byte) 1){
            this.msgTyp = MESSAGE.UNCHOKE;
        }
        else if (data == (byte) 2){
            this.msgTyp = MESSAGE.INTERESTED;
        }
        else if (data == (byte) 3){
            this.msgTyp = MESSAGE.NOT_INTERESTED;
        }
        else if (data == (byte) 4){
            this.msgTyp = MESSAGE.HAVE;
        }
        else if (data == (byte) 5){
            this.msgTyp = MESSAGE.BITFIELD;
        }
        else if (data == (byte) 6){
            this.msgTyp = MESSAGE.REQUEST;
        }
        else if (data == (byte) 7){
            this.msgTyp = MESSAGE.PIECE;
        }
        return msgTyp;
    }

    public byte getByte(MESSAGE msg){
        byte bat = -1;

        if (msg == MESSAGE.CHOKE){
            bat = 0;
        }
        else if (msg == MESSAGE.UNCHOKE){
            bat = 1;
        }
        else if (msg == MESSAGE.BITFIELD){
            bat = 2;
        }
        else if (msg == MESSAGE.NOT_INTERESTED){
            bat = 3;
        }
        else if (msg == MESSAGE.HAVE){
            bat = 4;
        }
        else if (msg == MESSAGE.BITFIELD){
            bat = 5;
        }
        else if (msg == MESSAGE.REQUEST){
            bat = 6;
        }
        else if (msg == MESSAGE.PIECE){
            bat = 7;
        }

        return bat;

    }

    public boolean hasPayload(MESSAGE type) {

        return (type == MESSAGE.HAVE || type == MESSAGE.BITFIELD
                || type == MESSAGE.REQUEST || type == MESSAGE.PIECE );
    }

    public String toString() {
        String str = "Length: " + len +
                ", Type = " + msgTyp + ", Payload:";
        if (msgTyp == MESSAGE.PIECE)
            str += "[File Bytes]";
        else
            str += Arrays.toString(msgPayLd);
        return str;
    }

    public byte[] getMessageBytes() {
        ByteBuffer msgBuffer = ByteBuffer.allocate(5 + len);

        msgBuffer.put(lenInB);
        msgBuffer.put(getByte(msgTyp));
        //msgBuffer.put(msgTyp);

        if(hasPayload(msgTyp)) {
            msgBuffer.put(msgPayLd);
        }

        return msgBuffer.array();
    }

    public static void sendHandshake(int ind, ProcessData pd) {

        byte[] handShakeHeader = new byte[18];
        try {
            handShakeHeader = "P2PFILESHARINGPROJ".getBytes("UTF-8");
        } catch (Exception e) {
            //e.printStackTrace();
        }
        byte[] zBits = new byte[10];
        byte[] peers = ByteBuffer.allocate(4).putInt(pd.myPeerId).array();

        ByteBuffer handShakeBuff = ByteBuffer.allocate(32);

        handShakeBuff.put(handShakeHeader);
        handShakeBuff.put(zBits);
        handShakeBuff.put(peers);
        byte[] handShakes = handShakeBuff.array();

        sendMessage(handShakes, ind,pd);
        handShakeBuff.clear();
        pd.writelogs.TcpMakeConnection(pd.myPeerId, pd.peerNeighbours[ind].peerId);

    }

    public static void sendMessage(byte[] msg, int socketind, ProcessData pd)
    {
        try {
            pd.rqout[socketind].writeObject(msg);
            pd.rqout[socketind].flush();
        }
        catch(IOException IOException){
            System.err.println("Message not sent error.");
            //IOException.printStackTrace();
        }
    }

    public static void messageHandling(ProcessData pd){

        List<Message> msgToRmv = new ArrayList<Message>();

        synchronized (pd.myServer.msgRcvd) {

            Iterator<Message> it = pd.myServer.msgRcvd.iterator();
            while (it.hasNext()) {
                Message msgIn = it.next();
                int msgInd = pd.myClientIDToPeerID[msgIn.myClientID];

                if(checkHandshake(msgIn, msgInd))
                    continue;

                msgInd = findind(pd, msgIn, msgInd);
                if (msgInd != -1) {
                    if ((msgIn.msgTyp != MESSAGE.BITFIELD) &&
                            pd.peerNeighbours[msgInd].bitFieldForRcv == false &&
                            pd.peerNeighbours[msgInd].rcvdHandshake == true) {
                        continue;
                    }
                }

                messageProcessing(pd, msgIn, msgInd);
                msgToRmv.add(msgIn);
            }

            for (Message m : msgToRmv) {
                pd.myServer.msgRcvd.remove(m);
            }
        }

    }

    private static void messageProcessing(ProcessData pd, Message msgIn, int msgInd) {

        if (msgIn.msgTyp == MESSAGE.BITFIELD){
            bitFieldHandler(pd, msgIn, msgInd);
        }
        else if(msgIn.msgTyp == MESSAGE.HAVE) {
            haveMessageHandler(pd, msgIn, msgInd);
        }
        else if(msgIn.msgTyp = MESSAGE.HANDSHAKE){
            handShakeHandler(pd, msgIn, msgInd);
        }
        else if(msgIn.msgTyp == MESSAGE.INTERESTED){
            interestedNeighborMessage(pd, msgInd);
        }
        else if(msgIn.msgTyp == MESSAGE.PIECE){
            pieceMessageHandler(pd, msgIn, msgInd);
        }
        else if(msgIn.msgTyp == MESSAGE.NOT_INTERESTED){
            uninterestedMessage(pd, msgInd);
        }
        else if(msgIn.msgTyp == MESSAGE.REQUEST){
            requestMessage(pd, msgIn, msgInd);
        }
        else if(msgIn.msgTyp == MESSAGE.CHOKE){
            chokeMessage(pd, msgInd);
        }
        else if(msgIn.msgTyp == MESSAGE.UNCHOKE){
            unchokeMessage(pd, msgInd);
        }
        else{
            System.out.println("unknown Error which was caused by the following message:" + msgIn.msgTyp);
        }
    }



    private static void pieceMessageHandler(ProcessData pd, Message incomingMessage, int msgInd) {
        pd.filePieces[pd.peerNeighbours[msgInd].pieceNum] = incomingMessage.msgPayLd;

        pd.peerNeighbours[msgInd].waitingForPiece = false;

        BigInteger tempField = new BigInteger(pd.bitfield);

        tempField = tempField.setBit(pd.peerNeighbours[msgInd].pieceNum);

        pd.bitfield = tempField.toByteArray();

        pd.recvData[msgInd] += pd.sizeOfPiece;
        pd.writelogs.pieceDownloaded(pd.otherPeerIds[pd.myClID], pd.peerNeighbours[msgInd].peerId, pd.peerNeighbours[msgInd].pieceNum, ++pd.numOfPieces);

        boolean haveFile = true;
        for (int i = 0; i < pd.numOfBits; i++) {
            if (!tempField.testBit(i)) {
                haveFile = false;
                break;
            }
        }

        pd.hasAllFile[pd.myClID] = haveFile;

        if (haveFile){
            pd.writelogs.fileDownloaded(pd.myPeerId);
        }
        for (int i = 0; i < pd.otherPeerIds.len; i++) {
            if (i == pd.myClID)
                continue;
            Message.sendHave(i, pd.peerNeighbours[msgInd].pieceNum, pd);
        }

        pd.peerNeighbours[msgInd].pieceNum = -1;
        for (int i = 0; i < pd.peerNeighbours.len; i++) {
            if (i == pd.myClID)
                continue;

            boolean interested = false;

            if (pd.peerNeighbours[i].bitFieldMap != null)
                interested = checkNeededPieces(pd.peerNeighbours[i],pd);

            if (!interested)
                Message.sendNotInterested(i, pd);
        }

        boolean allHvFile = true;
        for (int i = 0; i < pd.otherPeerIds.len; i++) {
            if (!pd.hasAllFile[i]) {
                allHvFile = false;
                break;
            }
        }

        pd.allPeerHave = allHvFile;
    }

    private static void bitFieldHandler(ProcessData pd, Message incomingMessage, int msgInd) {
        pd.peerNeighbours[msgInd].bitFieldMap = incomingMessage.msgPayLd;
        pd.peerNeighbours[msgInd].bitFieldForRcv = true;
        if (checkNeededPieces(pd.peerNeighbours[msgInd],pd)) {
            Message.sendInterested(msgInd,pd);
        } else {
            Message.sendNotInterested(msgInd,pd);
        }
    }

    private static void handShakeHandler(ProcessData pd, Message incomingMessage, int msgInd) {
        pd.myClientIDToPeerID[incomingMessage.myClientID] = incomingMessage.len;

        for (int i = 0; i < pd.otherPeerIds.len; i++) {
            if (pd.otherPeerIds[i] == incomingMessage.len) {
                msgInd = i;
                break;
            }
        }
        pd.peerNeighbours[msgInd].rcvdHandshake = true;
    }

    private static void haveMessageHandler(ProcessData pd, Message incomingMessage, int msgInd) {
        BigInteger tempField = new BigInteger(pd.peerNeighbours[msgInd].bitFieldMap);
        ByteBuffer buffer = ByteBuffer.wrap(incomingMessage.msgPayLd);

        int currentIndex = buffer.getInt();
        tempField = tempField.setBit(currentIndex);

        pd.peerNeighbours[msgInd].bitFieldMap = tempField.toByteArray();

        boolean neighborHasFile = true;
        for (int i = 0; i < pd.numOfBits; i++) {
            if (!tempField.testBit(i)) {
                neighborHasFile = false;
                break;
            }
        }

        pd.hasAllFile[msgInd] = neighborHasFile;

        boolean temp = true;
        for (int i = 0; i < pd.otherPeerIds.len; i++) {
            if (!pd.hasAllFile[i]) {
                temp = false;
                break;
            }
        }

        pd.allPeerHave = temp;
        pd.writelogs.haveMsgType(pd.otherPeerIds[pd.myClID], pd.peerNeighbours[msgInd].peerId, currentIndex);

        BigInteger myField = new BigInteger(pd.bitfield);
        if (!myField.testBit(currentIndex))
            Message.sendInterested(msgInd,pd);
    }

    private static void interestedNeighborMessage(ProcessData pd, int msgInd) {
        pd.peerNeighbours[msgInd].isInt = true;
        pd.writelogs.interestedMsgType(pd.otherPeerIds[pd.myClID], pd.peerNeighbours[msgInd].peerId);
    }

    private static void uninterestedMessage(ProcessData pd, int msgInd) {
        pd.peerNeighbours[msgInd].isInt = false;
        pd.writelogs.notInterestedMsgType(pd.otherPeerIds[pd.myClID], pd.peerNeighbours[msgInd].peerId);
    }

    private static void requestMessage(ProcessData pd, Message incomingMessage, int msgInd) {
        ByteBuffer buffer = ByteBuffer.wrap(incomingMessage.msgPayLd);
        int pieceNumber = buffer.getInt();
        Message.sendFilePiece(msgInd, pieceNumber, pd);
    }

    private static void chokeMessage(ProcessData pd, int msgInd) {
        pd.peerNeighbours[msgInd].isChoked = true;
        pd.writelogs.chokedMsgType(pd.otherPeerIds[pd.myClID], pd.peerNeighbours[msgInd].peerId);
    }

    private static void unchokeMessage(ProcessData pd, int msgInd) {
        pd.peerNeighbours[msgInd].isChoked = false;
        pd.writelogs.unchokedMsgType(pd.otherPeerIds[pd.myClID], pd.peerNeighbours[msgInd].peerId);
    }

    private static int findind(ProcessData pd, Message incomingMessage, int msgInd) {
        if ((int)incomingMessage.msgTyp != Message.handshake) {
            for (int i = 0; i < pd.otherPeerIds.len; i++) {
                if (pd.otherPeerIds[i] == msgInd) {
                    msgInd = i;
                    break;
                }
            }
        }
        return msgInd;
    }

    private static boolean checkHandshake(Message incomingMessage, int msgInd) {
        return (msgInd == -1 && !((int)incomingMessage.msgTyp == Message.handshake)) ;
    }
    public static void sendNotInterested(int ind, ProcessData pd) {
        Message message = new Message(0,(byte)Message.notInt, null);
        sendMessage(message.getMessageBytes(), ind, pd);
    }

    public static void sendUnchoke(int ind, ProcessData pd) {
        Message message = new Message(0,(byte)Message.unchoke, null);
        sendMessage(message.getMessageBytes(), ind, pd);
    }

    public static void sendHave(int ind, int pieceNumber , ProcessData pd) {
        byte[] pieceind = ByteBuffer.allocate(4).putInt(pieceNumber).array();
        Message message = new Message(4,(byte)Message.have,pieceind);
        sendMessage(message.getMessageBytes(), ind, pd);
    }

    public static void sendFilePiece(int ind, int pieceNumber, ProcessData pd) {
        Message message = new Message(pd.sizeOfPiece, (byte)Message.piece, pd.filePieces[pieceNumber]);
        sendMessage(message.getMessageBytes(), ind ,pd);
    }

    public static void sendChoke(int ind, ProcessData pd) {
        Message message = new Message(0,(byte)Message.choke, null);
        sendMessage(message.getMessageBytes(), ind, pd);
    }

    public static void sendBitfield(int ind, ProcessData pd) {
        Message bitfieldMessage = new Message(pd.bitfield.len, (byte)Message.bitfield, pd.bitfield);
        sendMessage(bitfieldMessage.getMessageBytes(), ind,pd);
    }

    public static void sendInterested(int ind, ProcessData pd) {
        Message message = new Message(0,(byte)Message.interested, null);
        sendMessage(message.getMessageBytes(), ind, pd);
    }

    public static void sendRequest(int ind, int pieceNumber, ProcessData pd) {
        byte[] pieceind = ByteBuffer.allocate(4).putInt(pieceNumber).array();
        Message message = new Message(4,(byte)Message.request,pieceind);
        sendMessage(message.getMessageBytes(), ind, pd);

    }
    public static boolean checkNeededPieces(RemoteNeighbours neighbor,ProcessData pd) {
        BigInteger selfField = new BigInteger(pd.bitfield);
        BigInteger neighbourField = new BigInteger(neighbor.bitFieldMap);

        if (neighbourField.and(selfField.and(neighbourField).not()).doubleValue() > 0) {
            return true;
        }
        return false;
    }

}