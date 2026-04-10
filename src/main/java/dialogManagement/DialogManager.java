package dialogManagement;

import java.util.ArrayList;

import Box.Box.PromptObserver;
import flatLand.trainingGround.Sprites.ObserverPrompt;
import theStart.thePeople.FlatLander;
import theStart.thePeople.FlatLanderFaceBook;

public class DialogManager {
	private static DialogManager instance;
	private static ArrayList<PromptObserver> promptObs = new ArrayList<PromptObserver>();
	private static ArrayList<ObserverPrompt> obvPrompts = new ArrayList<ObserverPrompt>();

	private static ArrayList<XMLLEVELLOADER.FlatLanderWrper> terminals = new ArrayList<XMLLEVELLOADER.FlatLanderWrper>();
	
	
	
	public static DialogManager getInstance() {
		if (instance == null) {
			instance = new DialogManager();
		}
		return instance;
	}


	public static ArrayList<PromptObserver> getPromptObservers() {
		return promptObs;
	}
	public static ArrayList<ObserverPrompt> getObserverPrompts() {
		return obvPrompts;
	}


	public void add(PromptObserver pO, ObserverPrompt oP,XMLLEVELLOADER.FlatLanderWrper fL) {
		promptObs.add(pO);
		obvPrompts.add(oP);
		terminals.add(fL);
	}


	public static ArrayList<XMLLEVELLOADER.FlatLanderWrper> getTerminals() {
		return terminals;
	}


	public static void notifyAllPO(String string) {
		for (int i = 0; i < promptObs.size(); i++) {
			promptObs.get(i).notify(string);
		}
	}

}
