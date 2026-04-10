package theStart.theStuff;

import theStart.thePeople.FlatLander.XYPair;
import theStart.thePeople.FlatLander.XYWrapper;

public class SynapsePair {
	private  XYPair dendrite;
	private  XYPair axon;
	private XYWrapper axonBranch;
	private XYWrapper dendBranch;

	public SynapsePair(XYPair axon, XYPair dendrite, XYWrapper axonBranch, XYWrapper dendBranch) {
		this.setAxonBranch(axonBranch);
		this.setDendBranch(dendBranch);
		this.setAxon(axon);
		this.setDendrite(dendrite);
	}

	public XYPair getDendrite() {
		return dendrite;
	}

	public void setDendrite(XYPair dendrite) {
		this.dendrite = dendrite;
	}

	public XYPair getAxon() {
		return axon;
	}

	public void setAxon(XYPair axon) {
		this.axon = axon;
	}

	public void update() {
		if (axon.isFire()) {
			dendrite.setBackFire(true);
			dendrite.setBackFireCount(1);
		} 

		

	}

	public XYWrapper getDendBranch() {
		return dendBranch;
	}

	public void setDendBranch(XYWrapper dendBranch) {
		this.dendBranch = dendBranch;
	}

	public XYWrapper getAxonBranch() {
		return axonBranch;
	}

	public void setAxonBranch(XYWrapper axonBranch) {
		this.axonBranch = axonBranch;
	}

}
