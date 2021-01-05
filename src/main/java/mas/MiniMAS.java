package mas;

import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;


public class MiniMAS {

	Map<String, AbstractAgent> agents = new HashMap<String, AbstractAgent>();
	PriorityQueue<MMEvent> queue = new PriorityQueue<MMEvent>(1000,new MMEventComparator());
	long time;
	long runden = 0;
	double aktuell;
	String summen ="";
	private List<AbstractAgent> alist = new ArrayList<>();
	private boolean done;
	private int msgCount;
	private double[] series;
	private int maxt=Integer.MAX_VALUE;
	public com.graphhopper.GraphHopper hopper;
	
	public MiniMAS(int maxt) {
		this.maxt=maxt;
		this.series=new double[maxt];
	}
	

	public MiniMAS() {
	}


	public void setAgents(List<AbstractAgent> agents) {
		HashMap<String, AbstractAgent> amap=new HashMap<>();
		for (AbstractAgent a:agents) amap.put(a.getId(), a);
		this.agents=amap;
		this.alist=agents;
		for (AbstractAgent a:this.agents.values()) {
			a.setMAS(this);
		}
	}

	public void add(AbstractAgent agent) {
		agent.setMAS(this);
		this.agents.put(agent.getId(), agent);		
		this.alist.add(agent);
	}

	public void naturallyExecute() {
		while(true) {
			MMEvent evt=queue.poll();
			if (evt!=null) time=evt.getTime();

			if (evt instanceof MMMsgEvent) {
				Message msg=((MMMsgEvent)evt).getMessage();
				AbstractAgent agent=agents.get(msg.getAddressee());
				agent.addMessage(msg);				
				agent.step(time);
			}

			if (evt == null) {
				for (AbstractAgent a:agents.values()) a.step(time);
			}

			if (queue.isEmpty()) break;
		}
	}

	
	public void asyncExecute() {

		this.initAgents();


		while(true) {
			MMEvent evt=queue.poll();
			//System.out.println("time: "+evt.getTime());
			while (evt!=null) {
				Message msg=((MMMsgEvent)evt).getMessage();
				AbstractAgent agent=agents.get(msg.getAddressee());
				agent.addMessage(msg);
				time=evt.getTime();
				if (!queue.isEmpty()&&time==queue.peek().getTime()) {
					evt=queue.poll();
				} else {
					evt=null;
				}
			}

			Thread[] ts=new Thread[agents.size()];
			int i=0;
			for (AbstractAgent a:agents.values()) {
				final AbstractAgent b = a;
				ts[i]=new Thread() {
					public void run() {
						b.step(time);
					}
				};
				ts[i].start();
				i++;
			}
			
			for (Thread t:ts) {
				try {
					t.join(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (queue.isEmpty()) {
				System.out.println("queue is empty: done");
				break;
			}
			if (done) break;
		}
	}

	public void initHopper() {

		hopper = new GraphHopperOSM().forServer();
		hopper.setDataReaderFile("niedersachsen-latest.osm.pbf");
		// specify where to store graphhopper files
		hopper.setGraphHopperLocation("target/routing-graph-cache");
		hopper.setEncodingManager(EncodingManager.create("car"));

		// see docs/core/profiles.md to learn more about profiles
		hopper.setProfiles(new Profile("car").setVehicle("car").setWeighting("shortest").setTurnCosts(false));
		//für umstellen auf "fastest" files aus routing-graph-cache löschen

		// this enables speed mode for the profile we called car
		hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car"));
		// explicitly allow that the calling code can disable this speed mode
		hopper.getRouterConfig().setCHDisablingAllowed(true);

		// now this can take minutes if it imports or a few seconds for loading of course this is dependent on the area you import
		hopper.importOrLoad();

	}

	public void execute() {

		this.initAgents();
		runden = 0;
		summen = "";
		//long timeToPlay = System.currentTimeMillis() + 50;
		//while(System.currentTimeMillis()< timeToPlay) {
		while (true) {
			aktuell = 0;
			double i = 0.0;
			for (AbstractAgent a:agents.values()) {
				aktuell = aktuell + a.getValue();
				i++;
			}
			aktuell = aktuell / i;
			System.out.println("Runde " + runden + " ; Lösung aktuell: " + aktuell + "\n");
			summen = summen + runden + "   " + aktuell + System.lineSeparator();
			runden = runden + 1;
			MMEvent evt=queue.poll();
			while (evt!=null) {
				Message msg=((MMMsgEvent)evt).getMessage();
				AbstractAgent agent=agents.get(msg.getAddressee());
				agent.addMessage(msg);
				time=evt.getTime();
				if (!queue.isEmpty()&&time==queue.peek().getTime()) {
					evt=queue.poll();
				} else {
					evt=null;
				}
			}

			for (AbstractAgent a:agents.values()) a.step(time);

			if (queue.isEmpty()) break;
		}
	}


	public void loop() {

		this.done=false;

		while(!done) {
			MMEvent evt=queue.poll();
			if (evt!=null) time=evt.getTime();

			if (evt instanceof MMMsgEvent) {
				Message msg=((MMMsgEvent)evt).getMessage();
				AbstractAgent agent=agents.get(msg.getAddressee());
				agent.addMessage(msg);				
			}

			for (AbstractAgent a:agents.values()) {				
				a.step(time);
			}

		}
	}

	public void step(long time) {	
		while (queue.peek() != null && queue.peek().time==time) {
			MMEvent evt=queue.poll();
			if (evt instanceof MMMsgEvent) {
				Message msg=((MMMsgEvent)evt).getMessage();
				AbstractAgent agent=agents.get(msg.getAddressee());
				agent.addMessage(msg);
				agent.step(time);
			}
		}
	}

	public void sendMessage(Message msg) {
		msgCount++;
		queue.add(new MMMsgEvent(time+1, msg));		
	}


	public int getMessageCount() {
		return this.msgCount;
	}

	public void buildSmallWorld() {
		int n=this.agents.size();

		System.out.println("n= "+n);
		List<Integer[]> edges=WattsStrogatzRandomGraph.connectSmallWorld(n, n/10, 0.5f); //hier das n/whatever variieren
		System.out.println("Kanten erstellt: "+ edges.size());
		for (Integer[] e:edges) {
			connect(alist.get(e[0]),alist.get(e[1]));
		}
	}

	public void connect(AbstractAgent a, AbstractAgent b) {
		a.addNeighbour(b.getId());
		//b.addNeighbour(a.getId());
	}

	/*
	private void connect(String a, String b) {
		connect(agents.get(a),agents.get(b));
	}*/

	public AbstractAgent getAgent(String id) {
		return this.agents.get(id);		
	}

	public void stop() {
		this.done=true;
	}

	double best=Double.MAX_VALUE;
	public synchronized void reportImprovement(double v) {
		if (time>maxt) {
			System.out.println("max iterations reached: stopping sim");
			stop();
			return;
		}
		if (v<best) {
			best=v;
			System.out.println(this.time+"  :  "+best);
			if (this.maxt<Integer.MAX_VALUE) this.series[(int) time-1]=v;
		}
	}


	public double[] getSeries() {
		return this.series;
	}
	
	public double getBestReported() {
		return best;
	}

	public long getRunden() {
		return this.runden;
	}

	public boolean isFirstRound() {
		if (this.runden < this.agents.size()) {
			return true;
		}
		return false;
	}

	public String getSummen() {
		return this.summen;
	}

	public void initAgents() {
		for (AbstractAgent a:agents.values()) a.init();
	}
}
