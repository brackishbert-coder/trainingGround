package Nuron;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import theStart.thePeople.FlatLander;
import theStart.thePeople.FlatLanderFaceBook;
import theStart.thePeople.FlatLander.XYPair;
import theStart.thePeople.FlatLander.XYWrapper;
import theStart.theStuff.BranchType;
import theStart.theStuff.ClassOfFlatLander;
import theStart.theStuff.SynapseFaceBook;
import theStart.theStuff.SynapsePair;

public class NuronForDisplay {

	private int width;
	private int height;
	private int posxinflatland = 0;
	private int posyinflatland = 0;
	boolean display = true;
	boolean displayContacts = true;
	boolean displayBody = true;
	boolean displayBranches = true;
	private Color inputnuronFireColor = Color.ORANGE;
	private Color inputnuronrestcolor = Color.green;
	private Color nuronFirecolor = Color.YELLOW;
	private Color nuronrestcolor = Color.pink;
	private FlatLander flatLander;
	private ClassOfFlatLander nuronType;


	public NuronForDisplay(int width, int height, FlatLander flatLander) {
		this.width = width;
		this.height = height;
		this.flatLander = flatLander;
		this.getFlatLander().setNuronForDisplay(this);
		nuronType = this.getFlatLander().getNuronType();
	}

	public void renderNurons(Graphics graphics, ArrayList<FlatLander> flatlanderFaceBook,
			ArrayList<FlatLander> flatlanderFaceBookPool, boolean display2, boolean displayBody2,
			boolean displayBranches2, boolean displayContacts2,int cmaeraX, int cameraY) {
		this.display = display2;
		this.displayBody = displayBody2;
		this.displayBranches = displayBranches2;
		this.displayContacts = displayContacts2;
		long start = System.currentTimeMillis();

		renderAxonsDendrites(graphics, flatlanderFaceBookPool, cmaeraX, cameraY);
		long end = System.currentTimeMillis();

		// System.err.println("time to renderAxonsDendrites: "+(end-start));

		start = System.currentTimeMillis();

		renderSynapse(graphics, flatlanderFaceBookPool, cmaeraX, cameraY);

		end = System.currentTimeMillis();

		// System.err.println("time to renderSynapse: "+(end-start));

		start = System.currentTimeMillis();
		renderBody(graphics, flatlanderFaceBook, flatlanderFaceBookPool, cmaeraX, cameraY);
		end = System.currentTimeMillis();

		// System.err.println("time to renderBody: "+(end-start));
		// System.err.println();System.err.println();System.err.println();
	}

	private void renderSynapse(Graphics graphics, ArrayList<FlatLander> flatlanderFaceBookPool, int cmaeraX, int cameraY) {
		ArrayList<SynapsePair> synapseFaceBook = SynapseFaceBook.getInstance().getSynapseFaceBook();
		SynapseFaceBook.getInstance().removeAll();
		for (FlatLander flatLander2 : flatlanderFaceBookPool) {
			if (!flatLander2.equals(getFlatLander())) {
				XYWrapper axon = flatLander2.axon;
				ArrayList<XYWrapper> dendrites = getFlatLander().getDendrites();
				for (XYWrapper dend : dendrites) {
					if (checkXYAgainstTerminal(dend, axon.getAtEnd())) {
						axon.setGrow(false);

						XYPair dendXYPair = getXYAgainstTerminal(dend, axon.getAtEnd());
						dendXYPair.formAxionSynapse();

						SynapseFaceBook.getInstance().add(new SynapsePair(axon.getAtEnd(), dendXYPair, axon, dend));

					}

					if (checkXYAgainstTerminal(axon, dend.getAtEnd())) {
						dend.setGrow(false);

						XYPair axonXYPair = getXYAgainstTerminal(axon, dend.getAtEnd());
						axonXYPair.formDenriteSynapse();
						SynapseFaceBook.getInstance().add(new SynapsePair(axonXYPair, dend.getAtEnd(), axon, dend));

					}

				}
			}

		}

		for (SynapsePair synapsePair : synapseFaceBook) {
			synapsePair.update();
			if (this.display) {
				if (this.displayContacts) {
					XYWrapper axon = synapsePair.getAxonBranch();
					Color axonColor2 = axon.getAxonSynapseColor();
					XYWrapper dendrite = synapsePair.getDendBranch();
					Color dendColor = dendrite.getDendriteSynapseColor();

					graphics.setColor(axonColor2);
					graphics.drawRect(mapFromFlatLandToScreenSpaceX(synapsePair.getAxon().getX())+cmaeraX,
							mapFromFlatLandToScreenSpaceY(synapsePair.getAxon().getY())+cameraY, 5, 5);
					graphics.setColor(dendColor);
					graphics.drawRect(mapFromFlatLandToScreenSpaceX((synapsePair.getDendrite().getX() - 5))+cmaeraX,
							mapFromFlatLandToScreenSpaceY(synapsePair.getDendrite().getY() - 5)+cameraY, 5, 5);
				}
			}
		}
	}

	private void renderBody(Graphics graphics, ArrayList<FlatLander> flatlanderFaceBook,
			ArrayList<FlatLander> flatlanderFaceBookPool, int cmaeraX, int cameraY) {

		getFlatLander().updateBody();
		if (this.display) {
			if (this.displayBody) {
				if (getFlatLander().getXposinflatland() >= posxinflatland - width / 2
						&& getFlatLander().getXposinflatland() <= posxinflatland + width / 2) {
					if (getFlatLander().getYposinflatland() >= posyinflatland - height / 2
							&& getFlatLander().getYposinflatland() <= posyinflatland + height / 2) {

						if (getFlatLander().bodyFire) {
							graphics.setColor(nuronFirecolor);
						} else {
							graphics.setColor(
									new Color((int) (Math.random() * 254), 0, (int) (Math.random() * 150), 255));
						}

						int i = (int) (Math.random() * 10);
						int width2 = (int) (Math.random() * 20 + 5);
						int height2 = (int) (Math.random() * 20 + 10);
						graphics.fillOval(mapFromFlatLandToScreenSpaceX(getFlatLander().getXposinflatland() - i)+cmaeraX,
								mapFromFlatLandToScreenSpaceY(getFlatLander().getYposinflatland() - i)+cameraY, width2, height2);
					}
				}
			}
		}
	}

	private void renderAxonsDendrites(Graphics graphics, ArrayList<FlatLander> flatlanderFaceBookPool, int cmaeraX, int cameraY) {

		if (getFlatLander().getXposinflatland() >= posxinflatland - width / 2
				&& getFlatLander().getXposinflatland() <= posxinflatland + width / 2) {
			if (getFlatLander().getYposinflatland() >= posyinflatland - height / 2
					&& getFlatLander().getYposinflatland() <= posyinflatland + height / 2) {
				getFlatLander().updateBranches();
				getFlatLander().growBranches();
				if (this.display) {
					ArrayList<XYWrapper> branches = getFlatLander().getDendrites();
					for (XYWrapper xyPair : branches) {
						if (xyPair != null)
							drawBranches(graphics, xyPair,cmaeraX, cameraY);
					}

					XYWrapper axon = getFlatLander().getAxon();
					drawBranches(graphics, axon,cmaeraX, cameraY);
				}
			}

		}
	}

	private void checkAndUpdateInputNuron(Graphics graphics,int cmaeraX, int cameraY) {
		ArrayList<FlatLander> InputNurons = FlatLanderFaceBook.getInstance().getInputNurons();

		for (FlatLander InputNuron : InputNurons) {

			InputNuron.updateBranches();
			InputNuron.growBranches();
			if (this.display) {
				drawBranches(graphics, InputNuron.getAxon(),cmaeraX, cameraY);
				ArrayList<XYWrapper> branches = InputNuron.getDendrites();
				for (XYWrapper xyPair : branches) {

					drawBranches(graphics, xyPair,cmaeraX, cameraY);
				}
				if (this.displayBody) {
					if (InputNuron.fire()) {

						graphics.setColor(inputnuronFireColor);
					} else {

						graphics.setColor(inputnuronrestcolor);
					}
					int i = (int) (Math.random() * 10);
					int width2 = (int) (Math.random() * 20 + 5);
					int height2 = (int) (Math.random() * 20 + 10);
					graphics.fillOval(mapFromFlatLandToScreenSpaceX(InputNuron.getXposinflatland() - i),
							mapFromFlatLandToScreenSpaceY(InputNuron.getYposinflatland() - i), width2, height2);

				}

			}
		}
	}

	private void drawBranches(Graphics graphics, XYWrapper xyPair, int cmaeraX, int cameraY) {

		for (int i = 0; i < xyPair.getEndIndex(); i++) {

			if (xyPair.getXYPairAt(i).isFire() && !xyPair.getXYPairAt(i).getBackFire()) {
				graphics.setColor(new Color((int) (Math.random() * 254), 0, (int) (Math.random() * 150), 255));

			} else if (!xyPair.getXYPairAt(i).isFire() && xyPair.getXYPairAt(i).getBackFire()) {
				graphics.setColor(new Color(0, (int) (Math.random() * 254), (int) (Math.random() * 150), 255));

			} else if (xyPair.getXYPairAt(i).isFire() && xyPair.getXYPairAt(i).getBackFire()) {
				graphics.setColor(xyPair.getFireAndBackFireColor());

			} else {
				if (xyPair.getType() == BranchType.Axon) {

					graphics.setColor(xyPair.getAxonColor());
				} else if (xyPair.getType() == BranchType.Dendrite) {

					graphics.setColor(xyPair.getDendriteColor());
				}

			}

			if (!this.displayBranches) {
				if (xyPair.getXYPairAt(i).isFire()) {
					graphics.drawRect(mapFromFlatLandToScreenSpaceX(xyPair.getXYPairAt(i).getX())+cmaeraX,
							mapFromFlatLandToScreenSpaceY(xyPair.getXYPairAt(i).getY())+cameraY, 1, 1);
				}
			} else {

				graphics.drawRect(mapFromFlatLandToScreenSpaceX(xyPair.getXYPairAt(i).getX())+cmaeraX,
						mapFromFlatLandToScreenSpaceY(xyPair.getXYPairAt(i).getY())+cameraY, 1, 1);

			}

			if (!this.displayBranches) {
				if (xyPair.getXYPairAt(i).getBackFire()) {
					graphics.drawRect(mapFromFlatLandToScreenSpaceX(xyPair.getXYPairAt(i).getX())+cmaeraX,
							mapFromFlatLandToScreenSpaceY(xyPair.getXYPairAt(i).getY())+cameraY, 1, 1);
				}
			} else {

				graphics.drawRect(mapFromFlatLandToScreenSpaceX(xyPair.getXYPairAt(i).getX())+cmaeraX,
						mapFromFlatLandToScreenSpaceY(xyPair.getXYPairAt(i).getY())+cameraY, 1, 1);

			}
		}

	}

	private boolean checkXYAgainstTerminal(XYWrapper xyPair2, XYPair terminal) {

		for (int i = 0; i < xyPair2.end; i++) {
			if ((xyPair2.getXYPairAt(i).getX() == terminal.getX() + 1
					&& xyPair2.getXYPairAt(i).getY() == terminal.getY() + 1)
					|| (xyPair2.getXYPairAt(i).getX() == terminal.getX() + 1
							&& xyPair2.getXYPairAt(i).getY() == terminal.getY() - 1)
					|| (xyPair2.getXYPairAt(i).getX() == terminal.getX() - 1
							&& xyPair2.getXYPairAt(i).getY() == terminal.getY() + 1)
					|| (xyPair2.getXYPairAt(i).getX() == terminal.getX() - 1
							&& xyPair2.getXYPairAt(i).getY() == terminal.getY() - 1)
					|| (xyPair2.getXYPairAt(i).getX() == terminal.getX()
							&& xyPair2.getXYPairAt(i).getY() == terminal.getY() + 1)
					|| (xyPair2.getXYPairAt(i).getX() == terminal.getX()
							&& xyPair2.getXYPairAt(i).getY() == terminal.getY() - 1)
					|| (xyPair2.getXYPairAt(i).getX() == terminal.getX() - 1
							&& xyPair2.getXYPairAt(i).getY() == terminal.getY())
					|| (xyPair2.getXYPairAt(i).getX() == terminal.getX() + 1
							&& xyPair2.getXYPairAt(i).getY() == terminal.getY()))
				return true;

		}

		return false;

	}

	private XYPair getXYAgainstTerminal(XYWrapper xyPair2, XYPair terminal) {

		XYPair found = null;
		for (int i = 0; i < xyPair2.end; i++) {
			if ((xyPair2.getXYPairAt(i).getX() == terminal.getX() + 1
					&& xyPair2.getXYPairAt(i).getY() == terminal.getY() + 1)
					|| (xyPair2.getXYPairAt(i).getX() == terminal.getX() + 1
							&& xyPair2.getXYPairAt(i).getY() == terminal.getY() - 1)
					|| (xyPair2.getXYPairAt(i).getX() == terminal.getX() - 1
							&& xyPair2.getXYPairAt(i).getY() == terminal.getY() + 1)
					|| (xyPair2.getXYPairAt(i).getX() == terminal.getX() - 1
							&& xyPair2.getXYPairAt(i).getY() == terminal.getY() - 1)
					|| (xyPair2.getXYPairAt(i).getX() == terminal.getX()
							&& xyPair2.getXYPairAt(i).getY() == terminal.getY() + 1)
					|| (xyPair2.getXYPairAt(i).getX() == terminal.getX()
							&& xyPair2.getXYPairAt(i).getY() == terminal.getY() - 1)
					|| (xyPair2.getXYPairAt(i).getX() == terminal.getX() - 1
							&& xyPair2.getXYPairAt(i).getY() == terminal.getY())
					|| (xyPair2.getXYPairAt(i).getX() == terminal.getX() + 1
							&& xyPair2.getXYPairAt(i).getY() == terminal.getY()))
				found = xyPair2.getXYPairAt(i);

		}
		return found;
	}

	public int mapFromFlatLandToScreenSpaceX(int input) {
		int input_end = posxinflatland + width / 2;
		int input_start = posxinflatland - width / 2;
		int output_start = 0;
		int output_end = width;
		double slope = 1.0 * (output_end - output_start) / (input_end - input_start);
		int i = output_start + (int) Math.round(slope * (input - input_start));

		return i;
	}

	public int mapFromFlatLandToScreenSpaceY(int input) {
		int input_end = posyinflatland + height / 2;
		int input_start = posyinflatland - height / 2;
		int output_start = 0;
		int output_end = height;
		double slope = 1.0 * (output_end - output_start) / (input_end - input_start);
		int i = output_start + (int) Math.round(slope * (input - input_start));

		return i;
	}

	public ClassOfFlatLander getNuronType() {
		return nuronType;
	}

	public FlatLander getFlatLander() {
		return flatLander;
	}
}
