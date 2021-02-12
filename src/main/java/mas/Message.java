package mas;

/**
 * Message to be sent by agents
 * @author Christian Hinrichs
 * @author Jörg Bremer
 */
public class Message {
	
	private String addressee; 
	private String sender;
	private String subject;
	private Object content;
	
	
	
	public Message(String addressee, String sender, String subject,	Object content) {
		super();
		this.addressee = addressee;
		this.sender = sender;
		this.subject = subject;
		this.content = content;
	}
	public String getAddressee() {
		return addressee;
	}
	public void setAddressee(String addressee) {
		this.addressee = addressee;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public Object getContent() {
		return content;
	}
	public void setContent(Object content) {
		this.content = content;
	}

	

}
