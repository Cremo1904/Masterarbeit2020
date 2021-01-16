package mas;

import java.util.*;

public abstract class AbstractCOHDAAgent extends AbstractAgent {

	public class demand {
		private double rest;
		private double quantity;
		private Set<String> edges;

		public demand (double rest, double quantity, Set<String> edges) {
			this.setRest(rest);
			this.setQuantity(quantity);
			this.setEdges(edges);
		}

		public double getRest() {
			return rest;
		}

		public void setRest(double rest) {
			this.rest = rest;
		}

		public double getQuantity () {
			return quantity;
		}

		public void setQuantity (double quantity) {
			this.quantity = quantity;
		}

		public Set<String> getEdges () {
			if (edges != null) {
				return edges;
			} else {
				return new HashSet<String>();
			}
		}

		public void setEdges (Set<String> edges) {
			this.edges = edges;
		}
	}

	public class edges {
		public int supply;
		public String demand;
		public int quantity;
		public int delta;
		public double distance;

		public edges(int supply, String demand, int quantity, int delta, double distance) {
			this.setSupply(supply);
			this.setDemand(demand);
			this.setQuantity(quantity);
			this.setDelta(delta);
			this.setDistance(distance);
		}

		public double getDistance() {
			return distance;
		}

		public void setDistance(double distance) {
			this.distance = distance;
		}

		public int getSupply() {
			return supply;
		}

		public String getDemand() {
			return demand;
		}

		public int getQuantity() {
			return quantity;
		}

		public int getDelta() {
			return delta;
		}

		public void setSupply(int supply) {
			this.supply = supply;
		}

		public void setDemand(String demand) {
			this.demand = demand;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}

		public void setDelta(int delta) {
			this.delta = delta;
		}
	}

	public class WorkingMemory {
		private HashMap<String, Object> supplyRest;
		private HashMap<String, Object> myDemand;
		private HashMap<String, Object> myEdges;
		private HashMap<String, String> contribution;
		private int lambda;
		private double targetValue;

		private WorkingMemory(HashMap<String, Object> supplyRest, HashMap<String, Object> myDemand, double targetValue, HashMap<String, Object> myEdges, HashMap<String, String> contribution, int lambda) {
			this.setSupplyRest(supplyRest);
			this.setMyDemand(myDemand);
			this.setTargetValue(targetValue);
			this.setContribution(contribution);
			this.setMyEdges(myEdges);
			this.setLambda(lambda);
		}

		public WorkingMemory(double targetValue) {
			this.setTargetValue(targetValue);
		}

		public WorkingMemory copy() {
			return new WorkingMemory(this.getSupplyRestCopy(), this.getMyDemandCopy(), this.getTargetValue(), this.getMyEdgesCopy(), this.getContributionCopy(), this.getLambda());
		}

		public int getLambda() {
			return lambda;
		}

		public void setLambda(int lambda) {
			this.lambda = lambda;
		}

		public void incLambda() {
			this.lambda++;
		}

		public HashMap<String, Object> getMyEdges() {
			if (myEdges != null) {
				return myEdges;
			} else {
				return new HashMap<>();
			}
		}

		public HashMap<String, Object> getMyEdgesCopy() {
			if (myEdges != null) {
				return new HashMap<String, Object>(myEdges);
			} else {
				return new HashMap<String, Object>();
			}
		}

		public void setMyEdges(HashMap<String, Object> myEdges) {
			this.myEdges = myEdges;
		}

		public HashMap<String, Object> getSupplyRestCopy() {
			if (supplyRest != null) {
				return new HashMap<String, Object>(supplyRest);
			} else {
				return new HashMap<String, Object>();
			}
		}

		public HashMap<String, Object> getMyDemandCopy() {
			if (myDemand != null) {
				return new HashMap<String, Object>(myDemand);
			} else {
				return new HashMap<String, Object>();
			}
		}

		public HashMap<String, Object> getSupplyRest() {
			if (supplyRest != null) {
				return supplyRest;
			} else {
				return new HashMap<>();
			}
		}

		public void setSupplyRest(HashMap<String, Object> supplyRest) {
			this.supplyRest = supplyRest;
		}

		public HashMap<String, Object> getMyDemand() {
			if (myDemand != null) {
				return myDemand;
			} else {
				return new HashMap<>();
			}
		}

		public void setContribution(HashMap<String, String> contribution) {
			this.contribution = contribution;
		}

		public HashMap<String, String> getContribution() {
			if (contribution != null) {
				return contribution;
			} else {
				return new HashMap<String, String>();
			}
		}
		public HashMap<String, String> getContributionCopy() {
			if (contribution != null) {
				return new HashMap<String, String>(contribution);
			} else {
				return new HashMap<String, String>();
			}
		}

		public void setMyDemand(HashMap<String, Object> myDemand) {
			this.myDemand = myDemand;
		}

		public void setTargetValue(double targetValue) {
			this.targetValue = targetValue;
		}

		public double getTargetValue() {
			return targetValue;
		}
	}

	protected WorkingMemory kappa;

	public AbstractCOHDAAgent(String id) {
		super(id);
	}

	@Override
	public void step(long time) {

		/*
		 * The agent follows the classical perceive--decide--act behavior.
		 */

		// Termination criterion
		boolean dirty = false;

		this.kappa.setContribution(null);
		while (this.hasMessage()) {
			Message msg = this.getMessage();

			//if (!this.getNeighbours().contains(msg.getSender())) {
			if (msg.getSubject() == "start") {
				dirty = true;
				continue;
			}
			WorkingMemory kappa = (WorkingMemory) msg.getContent();
			HashMap<String, Object> myDemand = kappa.getMyDemandCopy();
			HashMap<String, Object> myEdges = kappa.getMyEdgesCopy();

			Set<String> keys_own = this.kappa.getMyDemand().keySet();
			Set<String> keys_other = kappa.getMyDemand().keySet();
			Set<String> keys_in = new HashSet<String>(keys_other);
			keys_in.removeAll(keys_own);
			assert !keys_in.contains(this.getId());
			Set<String> keys_out = new HashSet<String>(keys_own);
			keys_out.removeAll(keys_other);
			if (!keys_own.equals(keys_other)) {
				if (keys_other.containsAll(keys_own)) {
					// Case 1: Received candidate is larger
					this.kappa.setTargetValue(kappa.getTargetValue());
					this.kappa.setMyEdges(kappa.getMyEdges());
					this.kappa.setContribution(kappa.getContribution());
					this.kappa.setMyDemand(kappa.getMyDemand());
					this.kappa.setSupplyRest(kappa.getSupplyRest());
					dirty = true;
				} else {
					for (String aid : keys_in) {
						demand demandSpec = (demand) myDemand.get(aid);
						HashMap<String, Object> supplyRestTemp = this.kappa.getSupplyRestCopy();
						HashMap<String, Object> myEdgesTemp = this.kappa.getMyEdgesCopy();
						boolean notCompatible = false;
						for (String str : demandSpec.getEdges()) {
							edges edgeSpec = (edges) myEdges.get(str);
							int supply = edgeSpec.getSupply();
							int quantity = edgeSpec.getQuantity();
							if (supplyRestTemp.containsKey(Integer.toString(supply))) {
								if (quantity > (int) supplyRestTemp.get(Integer.toString(supply))) {
									notCompatible = true;
									break;
								} else {
									supplyRestTemp.put(Integer.toString(supply), ((int) supplyRestTemp.get(Integer.toString(supply)) - quantity));
									myEdgesTemp.put(str, edgeSpec);
								}
							} else {
								HashMap<String, Object> angebot = (HashMap) Blackboard.get(Integer.toString(supply));
								supplyRestTemp.put(Integer.toString(supply), ((int) angebot.get("quantity") - quantity));
								myEdgesTemp.put(str, edgeSpec);
							}
						}

						if (!notCompatible) {
							this.kappa.setSupplyRest(supplyRestTemp);
							HashMap<String, Object> demand = this.kappa.getMyDemand();
							demand.put(aid, demandSpec);
							this.kappa.setMyDemand(demand);
							this.kappa.setMyEdges(myEdgesTemp);
							HashMap<String, String> contribution = this.kappa.getContribution();
							contribution.put(Integer.toString(contribution.size()+1), aid);
							this.kappa.setContribution(contribution);
							this.kappa.setTargetValue(objective(this.kappa.getMyDemand(), myEdgesTemp));
							dirty = true;
						}
					}
				}
			} else {
				double tv_own = this.kappa.getTargetValue();
				double tv_other = kappa.getTargetValue();
				if (tv_other < tv_own) {
					boolean notCompatible;
					HashMap <String, String> myContribution = kappa.getContribution();
					for (int i = 1; i <= myContribution.size(); i++) {
						String aid = myContribution.get(Integer.toString(i));
						if (!(aid == getId())) {
						HashMap<String, Object> supplyRestLocal = this.kappa.getSupplyRestCopy();
						HashMap<String, Object> demandLocal = this.kappa.getMyDemandCopy();
						HashMap<String, Object> edgesLocal = this.kappa.getMyEdgesCopy();
						double tv_old = objective(demandLocal, edgesLocal);
						demand demandSpecLocal = (demand)demandLocal.get(aid);
						for (String str : demandSpecLocal.getEdges()) {
							edges edgeSpec = (edges)edgesLocal.get(str);
							int supply = edgeSpec.getSupply();
							int quantity = edgeSpec.getQuantity();
							supplyRestLocal.put(Integer.toString(supply), (int)supplyRestLocal.get(Integer.toString(supply)) + quantity);
							edgesLocal.remove(str);
						}
						demand demandSpecNew = (demand)myDemand.get(aid);
						notCompatible = false;
						for (String str : demandSpecNew.getEdges()) {
							edges edgeSpec = (edges) myEdges.get(str);
							int supply = edgeSpec.getSupply();
							int quantity = edgeSpec.getQuantity();
							if (supplyRestLocal.containsKey(Integer.toString(supply))) {
								if (quantity > (int) supplyRestLocal.get(Integer.toString(supply))) {
									notCompatible = true;
									break;
								} else {
									supplyRestLocal.put(Integer.toString(supply), ((int) supplyRestLocal.get(Integer.toString(supply)) - quantity));
									edgesLocal.put(str, edgeSpec);
								}
							} else {
								HashMap<String, Object> angebot = (HashMap) Blackboard.get(Integer.toString(supply));
								supplyRestLocal.put(Integer.toString(supply), ((int) angebot.get("quantity") - quantity));
								edgesLocal.put(str, edgeSpec);
							}
						}

						if (!notCompatible) {
							demandLocal.put(aid, demandSpecNew);
							double tv_new = objective(demandLocal, edgesLocal);
							if (tv_new < tv_old) {
								this.kappa.setSupplyRest(supplyRestLocal);
								HashMap<String, Object> demand = this.kappa.getMyDemand();
								demand.put(aid, demandSpecNew);
								this.kappa.setMyDemand(demand);
								this.kappa.setMyEdges(edgesLocal);
								HashMap<String, String> contribution = this.kappa.getContribution();
								contribution.put(Integer.toString(contribution.size()+1), aid);
								this.kappa.setContribution(contribution);
								this.kappa.setTargetValue(tv_new);
								dirty = true;
							}
						}
						}
					}
				}
			}
			if (getMAS().isFirstRound()) {
				dirty = true;
			}
		}


		// Decide
		if (dirty) {
			decide_internal();
			this.kappa.incLambda();
		}

		// Act
		if (dirty) {
			for (String aid : this.getNeighbours()) {
				this.sendMessage(new Message(aid, this.getId(), null, this.kappa.copy()));
			}
		}
	}

	protected void decide_internal() {

		WorkingMemory kappaNew = decide();

		if (kappaNew.getTargetValue() < kappa.getTargetValue()) {
			// Resulting configuration is larger or better, a new solution
			// candidate has been found.

			kappa.setSupplyRest(kappaNew.getSupplyRest());
			kappa.setMyEdges(kappaNew.getMyEdges());
			kappa.setMyDemand(kappaNew.getMyDemand());
			kappa.setTargetValue(kappaNew.getTargetValue());
			kappa.setContribution(kappaNew.getContribution());
		}
	}

	protected void createKappa(double tv) {
		kappa=new WorkingMemory(tv);
	}

	protected abstract WorkingMemory decide();

	public abstract double objective(HashMap<String, Object> demandInput,HashMap<String, Object> edgesInput);

	protected HashMap<String, Object> toMap(String key, Object value) {
		HashMap<String,Object> map=new HashMap<>();
		map.put(key, value);
		return map;
	}


}
