package mas;

public class MMMsgEvent extends MMEvent {

	private Message message;
	
	

	public MMMsgEvent(long time, Message message) {
		this.message = message;
		this.time=time;
	}


	public Message getMessage() {
		return this.message;
	}

}
