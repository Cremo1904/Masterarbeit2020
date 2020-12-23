package mas;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;


public abstract class AbstractAgent {
	private MiniMAS mas;
	private ArrayDeque<Message> msgs=new ArrayDeque<>();
	private String id;
	private List<String> neighbours=new ArrayList<>();
	
	protected void reportImprovement(double v) {
		mas.reportImprovement(v);
	}
	
	protected AbstractAgent(String id) {
		this.id=id;
	}

	public abstract void step(long time);

	public void setMAS(MiniMAS miniMAS) {
		this.mas=miniMAS;
	}
	
	protected void stop() {
		mas.stop();
	}
	
	protected MiniMAS getMAS() {
		return mas;
	}
	
	protected void sendMessage(Message msg) {
		mas.sendMessage(msg);
	}
	
	protected Message getMessage() {
		return msgs.poll();
	}
	
	protected boolean hasMessage() {
		return !msgs.isEmpty();
	}
	
	void addMessage(Message msg) {
		msgs.add(msg);
	}

	public String getId() {
		return this.id;
	}
	
		
	public List<String> getNeighbours() {
		return neighbours;
	}

	void addNeighbour(String b) {
		this.neighbours.add(b);
	}

	protected void init() {
		
	}
	protected double getValue() {
		return 0;
	}
}
