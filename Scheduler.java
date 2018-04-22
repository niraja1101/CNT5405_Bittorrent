
public class Scheduler {

	
	
	public static double calculateDownloadRate(Handler handler,int peerIdIndex ){
		if(peerIdIndex!=handler.clientId ){
			if(handler.remotePeers.get(peerIdIndex).isConnection && handler.remotePeers.get(peerIdIndex).is_interested){
				double rate=handler.receivedData.get(peerIdIndex)/CommonConfigParser.UnchokingInterval;
				handler.receivedData.set(peerIdIndex,0);
				return rate;
			}
		}
		else{
			return 0;
		}
	}
	
	public static void decideCandUC(Handler handler){
		
	}
	
	
	
	public static void initiateNeighbourTaskSchedulers(Handler handler){
		pd.pref_neighbours_scheduler.scheduleAtFixedRate(new TimerTask() {
		     @Override
		      public void run(){
		         determinePreferredNeighbors(handler);
		      }
		     },0, peerProcess.cconfig.unchokingInterval * 1000);
		
	}
	
	determinePreferredNeighbors
}
