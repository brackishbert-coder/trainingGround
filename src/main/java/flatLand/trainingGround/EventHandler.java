package flatLand.trainingGround;

import FlatLandStructure.ViewableFlatLand;
import java.util.ArrayList;

public class EventHandler {
	private ArrayList<GameEvent> events = new ArrayList<GameEvent>();
	private double dist;

	public ArrayList<GameEvent> getEvents() {
		return events;
	}

	public void setEvents(ArrayList<GameEvent> events) {
		this.events = events;
	}

	public boolean eventAt(int x, int y) {
		for (GameEvent event : events) {
			if (event.getX() == x && event.getY() == y) {
				return true;
			}
		}
		return false;
	}

	public void addEvent(GameEvent event) {
		events.add(event);
	}

	public void removeEvent(GameEvent event) {
		events.remove(event);
	}
	public void removeEvent(String eventname) {
		GameEvent gameEvent2 =null;
		for (GameEvent gameEvent : events) {
			if(gameEvent.getName().equalsIgnoreCase(eventname)) {
				gameEvent2 = gameEvent;
				break;
			}
		}
		
		if(gameEvent2!=null)
			events.remove(gameEvent2);
		
		
	}

	public void clearEvents() {
		events.clear();// TODO Auto-generated method stub
		
	}



}
