package XMLLEVELLOADER;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Actions.ActionStack;
import Actions.ActionsInterface;
import Actions.ClearFlatLand;
import Actions.DrawABlob;
import Actions.DrawACircle;
import Actions.DrawAProtoCloud;
import Actions.DrawArc;
import Actions.DrawArcFasterVersion1;
import Actions.GoInAStrightLineFor;
import Actions.MoveBetween;
import Actions.MoveByXY;
import Actions.NoAction;
import Actions.Wonder;
import FlatLand.Physics.TypeOfEntity;
import FlatLandStructure.ViewableFlatLand;
import FlatLander.FlatLandFacebook;
import flatLand.trainingGround.EventHandler;
import flatLand.trainingGround.GameEvent;
import flatLand.trainingGround.Sprites.SceneObject;
import flatLand.trainingGround.Sprites.Skeleton;
import flatLand.trainingGround.Sprites.SkeletonTwo;
import flatLand.trainingGround.Sprites.ZombieBaby;

import theStart.theView.TheControls.GameScreen;




public class XmlLevelLoader {

	private String fileName;

	public XmlLevelLoader(String fileName, EventHandler eventHandler, ViewableFlatLand flatLand, GameScreen panel) {
		this.fileName = fileName;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document doc = db.parse(new File(fileName));
			doc.getDocumentElement().normalize();
			

			NodeList list = doc.getElementsByTagName("object");
			
			NodeList eventsList = doc.getElementsByTagName("event");
			
			
			
			
			eventLEVELDATA(eventsList,flatLand,panel,eventHandler);
			objectLEVELDATA(list,flatLand,(Element) doc.getElementsByTagName("levelstats").item(0),panel);
		} catch (ParserConfigurationException e1) {

			e1.printStackTrace();
		} catch (SAXException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	private void eventLEVELDATA(NodeList eventsList, ViewableFlatLand flatLand, GameScreen panel, EventHandler eventHandler) {
		for (int temp = 0; temp < eventsList.getLength(); temp++) {

			Node node = eventsList.item(temp);

			if (node.getNodeType() == Node.ELEMENT_NODE) {

				Element element = (Element) node;
				String type = element.getAttribute("type");

					Integer xpos = Integer.valueOf(element.getElementsByTagName("x").item(0).getTextContent());
					Integer ypos = Integer.valueOf(element.getElementsByTagName("y").item(0).getTextContent());
					String name = element.getElementsByTagName("name").item(0).getTextContent();
	                eventHandler.addEvent(new GameEvent(xpos,ypos,name,type));

			}
		}
		
	}

	private void objectLEVELDATA(NodeList list, ViewableFlatLand flatLand, Element levelElement, GameScreen panel) throws IOException {
		// TODO Auto-generated method stub

		for (int temp = 0; temp < list.getLength(); temp++) {

			Node node = list.item(temp);

			if (node.getNodeType() == Node.ELEMENT_NODE) {

				Element element = (Element) node;
				String type = element.getAttribute("type");
				String name = element.getElementsByTagName("name").item(0).getTextContent();
				Integer xpos = Integer.valueOf(element.getElementsByTagName("xpos").item(0).getTextContent());
				Integer ypos = Integer.valueOf(element.getElementsByTagName("ypos").item(0).getTextContent());
				Integer zpos = Integer.valueOf(element.getElementsByTagName("zpos").item(0).getTextContent());
				Integer scale = Integer.valueOf(element.getElementsByTagName("scale").item(0).getTextContent());

				if(type.equalsIgnoreCase("Item")){
					String sprite = element.getElementsByTagName("sprite").item(0).getTextContent();
					FlatLanderWrper mel = new FlatLanderWrper( xpos, ypos, zpos, name, 0,true,true, TypeOfEntity.MONSTER, Color.BLUE);
					
					
					mel.buildTerminal(flatLand, null);
					mel.setSprite(new SkeletonTwo(sprite,scale));
					
					while(!FlatLandFacebook.getInstance().requestToken(this)) {}
					FlatLandFacebook.getInstance().add(mel,this);
					FlatLandFacebook.getInstance().releaseToken(this);


				}else  if (type.equalsIgnoreCase("SkeletonTwo")) {
					String sprite = element.getElementsByTagName("sprite").item(0).getTextContent();
					FlatLanderWrper mel = new FlatLanderWrper( xpos, ypos, zpos, name, 0,true,true, TypeOfEntity.MONSTER, Color.BLUE);
					
					
					mel.setSprite(new SkeletonTwo(sprite,scale));
					while(!FlatLandFacebook.getInstance().requestToken(this)) {}
					FlatLandFacebook.getInstance().add(mel,this);
					FlatLandFacebook.getInstance().releaseToken(this);
					Node item = element.getElementsByTagName("preferdAction").item(0);
					Element prefAction = (Element) item;
					if (prefAction != null) {
						ActionsInterface prefdAction = getPreferedAction(prefAction, mel, flatLand);
						mel.setPreferedAction(prefdAction);
					}
					Node stack = element.getElementsByTagName("actionStack").item(0);
					Element actionStack = (Element) stack;
					if (actionStack != null) {
						ActionStack melsActionStack = buildTheActionStack(actionStack, mel, flatLand);
						mel.setActionStack(melsActionStack);
					}
				}
				//else  if (type.equalsIgnoreCase("ship")) {
				//	String sprite = element.getElementsByTagName("sprite").item(0).getTextContent();
				//	FlatLanderWrper mel = new FlatLanderWrper( xpos, ypos, zpos, name, 0,true,false, TypeOfEntity.MONSTER, Color.BLUE);
					
					
//					mel.setSprite(new Ship(sprite));
//					while(!FlatLandFacebook.getInstance().requestToken(this)) {}
//					FlatLandFacebook.getInstance().add(mel,this);
//					FlatLandFacebook.getInstance().releaseToken(this);
//					Node item = element.getElementsByTagName("preferdAction").item(0);
//					Element prefAction = (Element) item;
//					if (prefAction != null) {
//						ActionsInterface prefdAction = getPreferedAction(prefAction, mel, flatLand);
//						mel.setPreferedAction(prefdAction);
//					}
//					Node stack = element.getElementsByTagName("actionStack").item(0);
//					Element actionStack = (Element) stack;
//					if (actionStack != null) {
//						ActionStack melsActionStack = buildTheActionStack(actionStack, mel, flatLand);
//						mel.setActionStack(melsActionStack);
//					}
//				}else  if (type.equalsIgnoreCase("astroid")) {
//					String sprite = element.getElementsByTagName("sprite").item(0).getTextContent();
//					FlatLanderWrper mel = new FlatLanderWrper( xpos, ypos, zpos, name, 0,true,false, TypeOfEntity.MONSTER, Color.BLUE);
//					
//					
//					mel.setSprite(new Astroid(sprite,0,0,100,100));
//					while(!FlatLandFacebook.getInstance().requestToken(this)) {}
//					FlatLandFacebook.getInstance().add(mel,this);
//					FlatLandFacebook.getInstance().releaseToken(this);
//					Node item = element.getElementsByTagName("preferdAction").item(0);
//					Element prefAction = (Element) item;
//					if (prefAction != null) {
//						ActionsInterface prefdAction = getPreferedAction(prefAction, mel, flatLand);
//						mel.setPreferedAction(prefdAction);
//					}
//					Node stack = element.getElementsByTagName("actionStack").item(0);
//					Element actionStack = (Element) stack;
//					if (actionStack != null) {
//						ActionStack melsActionStack = buildTheActionStack(actionStack, mel, flatLand);
//						mel.setActionStack(melsActionStack);
//					}
//				}else  if (type.equalsIgnoreCase("glizzy")) {
//					String sprite = element.getElementsByTagName("sprite").item(0).getTextContent();
//					FlatLanderWrper mel = new FlatLanderWrper( xpos, ypos, zpos, name, 0,true,false, TypeOfEntity.MONSTER, Color.BLUE);
//					
//					
//					mel.setSprite(new Glizzy(sprite,32,32));
//					while(!FlatLandFacebook.getInstance().requestToken(this)) {}
//					FlatLandFacebook.getInstance().add(mel,this);
//					FlatLandFacebook.getInstance().releaseToken(this);
//					Node item = element.getElementsByTagName("preferdAction").item(0);
//					Element prefAction = (Element) item;
//					if (prefAction != null) {
//						ActionsInterface prefdAction = getPreferedAction(prefAction, mel, flatLand);
//						mel.setPreferedAction(prefdAction);
//					}
//					Node stack = element.getElementsByTagName("actionStack").item(0);
//					Element actionStack = (Element) stack;
//					if (actionStack != null) {
//						ActionStack melsActionStack = buildTheActionStack(actionStack, mel, flatLand);
//						mel.setActionStack(melsActionStack);
//					}
			//}
			else 
					if (type.equalsIgnoreCase("ZombieBaby")) {
					String sprite = element.getElementsByTagName("sprite").item(0).getTextContent();
					FlatLanderWrper mel = new FlatLanderWrper( xpos, ypos, zpos, name, 0,true,true, TypeOfEntity.MONSTER, Color.BLUE);
					mel.setSprite(new ZombieBaby(sprite,scale));
					while(!FlatLandFacebook.getInstance().requestToken(this)) {}
					FlatLandFacebook.getInstance().add(mel,this);
					FlatLandFacebook.getInstance().releaseToken(this);
					Node item = element.getElementsByTagName("preferdAction").item(0);
					Element prefAction = (Element) item;
					if (prefAction != null) {
						ActionsInterface prefdAction = getPreferedAction(prefAction, mel, flatLand);
						mel.setPreferedAction(prefdAction);
					}
					Node stack = element.getElementsByTagName("actionStack").item(0);
					Element actionStack = (Element) stack;
					if (actionStack != null) {
						ActionStack melsActionStack = buildTheActionStack(actionStack, mel, flatLand);
						mel.setActionStack(melsActionStack);
					}
				} else if (type.equalsIgnoreCase("Skeleton")) {
					String sprite1 = element.getElementsByTagName("sprite1").item(0).getTextContent();
					String sprite2 = element.getElementsByTagName("sprite2").item(0).getTextContent();
					FlatLanderWrper mel = new FlatLanderWrper(xpos, ypos, zpos, name, 0,true,true, TypeOfEntity.MONSTER, Color.BLUE);
					mel.setSprite(new Skeleton(sprite1, sprite2,scale));
					while(!FlatLandFacebook.getInstance().requestToken(this)) {}
					FlatLandFacebook.getInstance().add(mel,this);
					FlatLandFacebook.getInstance().releaseToken(this);
					Node item = element.getElementsByTagName("preferdAction").item(0);
					Element prefAction = (Element) item;
					if (prefAction != null) {
						ActionsInterface prefdAction = getPreferedAction(prefAction, mel, flatLand);
						mel.setPreferedAction(prefdAction);
					}
					Node stack = element.getElementsByTagName("actionStack").item(0);
					Element actionStack = (Element) stack;
					if (actionStack != null) {
						ActionStack melsActionStack = buildTheActionStack(actionStack, mel, flatLand);
						mel.setActionStack(melsActionStack);
					}
				} else if (type.equalsIgnoreCase("ground_dirt") || type.equalsIgnoreCase("ground_stone")) {
					String sprite = element.getElementsByTagName("sprite").item(0).getTextContent();
					Integer width = Integer.valueOf(element.getElementsByTagName("width").item(0).getTextContent());
					Integer levelHeight = Integer
							.valueOf( element.getElementsByTagName("height").item(0).getTextContent());
					
					FlatLanderWrper mel = new FlatLanderWrper(xpos, ypos, zpos, name, 0,true,false, TypeOfEntity.TERRAIN, Color.BLUE);
					mel.setSprite(new SceneObject(sprite, scale, type, width, levelHeight));
					while(!FlatLandFacebook.getInstance().requestToken(this)) {}
					FlatLandFacebook.getInstance().add(mel,this);
					FlatLandFacebook.getInstance().releaseToken(this);
					Node item = element.getElementsByTagName("preferdAction").item(0);
					Element prefAction = (Element) item;
					if (prefAction != null) {
						ActionsInterface prefdAction = getPreferedAction(prefAction, mel, flatLand);
						mel.setPreferedAction(prefdAction);
					}
					Node stack = element.getElementsByTagName("actionStack").item(0);
					Element actionStack = (Element) stack;
					if (actionStack != null) {
						ActionStack melsActionStack = buildTheActionStack(actionStack, mel, flatLand);
						mel.setActionStack(melsActionStack);
					}
				} else if (type.equalsIgnoreCase("platform_dirt")|| type.equalsIgnoreCase("platform_wood")) {
					String sprite = element.getElementsByTagName("sprite").item(0).getTextContent();
					Integer width = Integer.valueOf(element.getElementsByTagName("width").item(0).getTextContent());

					FlatLanderWrper mel = new FlatLanderWrper(xpos, ypos, zpos, name, 0,true,false, TypeOfEntity.TERRAIN, Color.BLUE);
					mel.setSprite(new SceneObject(sprite, scale, type, width, 0));
					while(!FlatLandFacebook.getInstance().requestToken(this)) {}
					FlatLandFacebook.getInstance().add(mel,this);
					FlatLandFacebook.getInstance().releaseToken(this);
					Node item = element.getElementsByTagName("preferdAction").item(0);
					Element prefAction = (Element) item;
					if (prefAction != null) {
						ActionsInterface prefdAction = getPreferedAction(prefAction, mel, flatLand);
						mel.setPreferedAction(prefdAction);
					}
					Node stack = element.getElementsByTagName("actionStack").item(0);
					Element actionStack = (Element) stack;
					if (actionStack != null) {
						ActionStack melsActionStack = buildTheActionStack(actionStack, mel, flatLand);
						mel.setActionStack(melsActionStack);
					}
				}

			}
		}
	}

	private ActionStack buildTheActionStack(Element actionStack, FlatLanderWrper actor, ViewableFlatLand flatLand) {
		ArrayList<ActionsInterface> acts = new ArrayList<ActionsInterface>();
		NodeList actions = actionStack.getElementsByTagName("action");
		for (int temp = 0; temp < actions.getLength(); temp++) {

			Node node = actions.item(temp);

			if (node.getNodeType() == Node.ELEMENT_NODE) {

				Element element = (Element) node;
				String type = element.getAttribute("type");
				String times = element.getAttribute("times");

				acts.addAll(getAction(type, times, element, actor, flatLand));

			}

		}

		return new ActionStack(acts);
	}

	private ArrayList<ActionsInterface> getAction(String type, String times, Element element, FlatLanderWrper actor,
			ViewableFlatLand flatLand) {
		ArrayList<ActionsInterface> acts = new ArrayList<ActionsInterface>();
		if (type.equalsIgnoreCase("wonder")) {
			acts.add(new Wonder(actor));
			return acts;
		} else if (type.equalsIgnoreCase("noaction")) {
			acts.add(new NoAction(actor));
			return acts;
		} else if (type.equalsIgnoreCase("movebyxy")) {
			Integer moveByx = getMoveByX(element);
			Integer moveByy = getMoveByY(element);
			Integer moveAngle = getMoveByAngle(element);
			acts.add(new MoveByXY(actor, moveByx, moveByy, moveAngle));
			return acts;
		} else if (type.equalsIgnoreCase("GoInAStrightLineFor")) {
			Integer dist = getDistance(element);

			acts.add(new GoInAStrightLineFor(actor, dist));
			return acts;

		} else if (type.equalsIgnoreCase("DrawArcFasterVersion1")) {
			acts.add(new DrawArcFasterVersion1(actor, flatLand));
			return acts;
		} else if (type.equalsIgnoreCase("DrawArc")) {
			acts.add(new DrawArc(actor, flatLand));
			return acts;
		} else if (type.equalsIgnoreCase("DrawAProtoCloud")) {
			acts.add(new DrawAProtoCloud(actor, flatLand));
			return acts;
		} else if (type.equalsIgnoreCase("DrawACircle")) {
			acts.add(new DrawACircle(actor, flatLand));
			return acts;
		} else if (type.equalsIgnoreCase("DrawABlob")) {
			acts.add(new DrawABlob(actor, flatLand));
			return acts;
		} else if (type.equalsIgnoreCase("ClearFlatLand")) {
			acts.add(new ClearFlatLand(actor, flatLand));
			return acts;
		} else if (type.equalsIgnoreCase("repeat")) {
			NodeList elementsByTagName = element.getElementsByTagName("action");
			Integer timesToRepeat = Integer.valueOf(times);
			for (int i = 0; i < timesToRepeat; i++) {
				for (int temp = 0; temp < elementsByTagName.getLength(); temp++) {

					Node node = elementsByTagName.item(temp);
					if (node.getNodeType() == Node.ELEMENT_NODE) {

						Element elementsub = (Element) node;
						String typesub = elementsub.getAttribute("type");
						String timessub = element.getAttribute("times");

						acts.addAll(getAction(typesub, timessub, elementsub, actor, flatLand));

					}
				}
			}
			return acts;
		}
		acts.add(new NoAction(actor));
		return acts;
	}

	private Integer getDistance(Element element) {
		String distance = element.getElementsByTagName("distance").item(0).getTextContent();
		if (distance.contains("random")) {
			String[] split = distance.split("\\*");
			String intToParse = "";
			for (String string : split) {
				if (!string.contains("random")) {
					intToParse = string;
					break;
				}
			}
			Integer toTimes = Integer.valueOf(intToParse);
			return (int) (Math.random() * toTimes);

		} else {
			return Integer.valueOf(distance);

		}
	}

	private Integer getMoveByAngle(Element element) {
		String angleMoveBy = element.getElementsByTagName("angle").item(0).getTextContent();
		if (angleMoveBy.contains("random")) {
			String[] split = angleMoveBy.split("\\*");
			String intToParse = "";
			for (String string : split) {
				if (!string.contains("random")) {
					intToParse = string;
					break;
				}
			}
			Integer toTimes = Integer.valueOf(intToParse);
			Integer dist = (int) (Math.random() * toTimes);
			return dist;
		} else {
			Integer dist = Integer.valueOf(angleMoveBy);
			return dist;
		}
	}

	private Integer getMoveByY(Element element) {
		String yMoveBy = element.getElementsByTagName("y").item(0).getTextContent();
		if (yMoveBy.contains("random")) {
			String[] split = yMoveBy.split("\\*");
			String intToParse = "";
			for (String string : split) {
				if (!string.contains("random")) {
					intToParse = string;
					break;
				}
			}
			Integer toTimes = Integer.valueOf(intToParse);
			Integer dist = (int) (Math.random() * toTimes);
			return dist;
		} else {
			Integer dist = Integer.valueOf(yMoveBy);
			return dist;
		}
	}

	private Integer getMoveByX(Element element) {
		String xMoveBy = element.getElementsByTagName("x").item(0).getTextContent();
		if (xMoveBy.contains("random")) {
			String[] split = xMoveBy.split("\\*");
			String intToParse = "";
			for (String string : split) {
				if (!string.contains("random")) {
					intToParse = string;
					break;
				}
			}
			Integer toTimes = Integer.valueOf(intToParse);
			Integer dist = (int) (Math.random() * toTimes);
			return dist;
		} else {
			Integer dist = Integer.valueOf(xMoveBy);
			return dist;
		}

	}
	private Integer getXLow(Element element) {
		String xMoveBy = element.getElementsByTagName("xlow").item(0).getTextContent();
		if (xMoveBy.contains("random")) {
			String[] split = xMoveBy.split("\\*");
			String intToParse = "";
			for (String string : split) {
				if (!string.contains("random")) {
					intToParse = string;
					break;
				}
			}
			Integer toTimes = Integer.valueOf(intToParse);
			Integer dist = (int) (Math.random() * toTimes);
			return dist;
		} else {
			Integer dist = Integer.valueOf(xMoveBy);
			return dist;
		}
		
	}
	private Integer getXHigh(Element element) {
		String xMoveBy = element.getElementsByTagName("xhigh").item(0).getTextContent();
		if (xMoveBy.contains("random")) {
			String[] split = xMoveBy.split("\\*");
			String intToParse = "";
			for (String string : split) {
				if (!string.contains("random")) {
					intToParse = string;
					break;
				}
			}
			Integer toTimes = Integer.valueOf(intToParse);
			Integer dist = (int) (Math.random() * toTimes);
			return dist;
		} else {
			Integer dist = Integer.valueOf(xMoveBy);
			return dist;
		}
		
	}
	private Integer getYLow(Element element) {
		String xMoveBy = element.getElementsByTagName("ylow").item(0).getTextContent();
		if (xMoveBy.contains("random")) {
			String[] split = xMoveBy.split("\\*");
			String intToParse = "";
			for (String string : split) {
				if (!string.contains("random")) {
					intToParse = string;
					break;
				}
			}
			Integer toTimes = Integer.valueOf(intToParse);
			Integer dist = (int) (Math.random() * toTimes);
			return dist;
		} else {
			Integer dist = Integer.valueOf(xMoveBy);
			return dist;
		}
		
	}
	private Integer getYHigh(Element element) {
		String xMoveBy = element.getElementsByTagName("yhigh").item(0).getTextContent();
		if (xMoveBy.contains("random")) {
			String[] split = xMoveBy.split("\\*");
			String intToParse = "";
			for (String string : split) {
				if (!string.contains("random")) {
					intToParse = string;
					break;
				}
			}
			Integer toTimes = Integer.valueOf(intToParse);
			Integer dist = (int) (Math.random() * toTimes);
			return dist;
		} else {
			Integer dist = Integer.valueOf(xMoveBy);
			return dist;
		}
		
	}


	private ActionsInterface getPreferedAction(Element prefAction, FlatLanderWrper actor, ViewableFlatLand flatLand) {

		NodeList elementsByTagName = prefAction.getElementsByTagName("action");
		if (elementsByTagName.getLength() > 0) {
			Node action = elementsByTagName.item(0);
			Element element = (Element) action;
			String type = element.getAttribute("type");

			if (type.equalsIgnoreCase("wonder")) {
				return new Wonder(actor);
			} else if (type.equalsIgnoreCase("noaction")) {
				return new NoAction(actor);
			} else if (type.equalsIgnoreCase("movebyxy")) {
				Integer moveByx = getMoveByX(element);
				Integer moveByy = getMoveByY(element);
				Integer moveAngle = getMoveByAngle(element);
				return new MoveByXY(actor, moveByx, moveByy, moveAngle);
			}else if(type.equalsIgnoreCase("movebetween")) {
				Integer moveByx = getMoveByX(element);
				Integer moveByy = getMoveByY(element);
				Integer xLow = getXLow(element);
				Integer xHigh = getXHigh(element);
				Integer yLow = getYLow(element);
				Integer yHigh = getYHigh(element);
				
				
				
				return new MoveBetween(actor,moveByx,moveByy,xLow,xHigh,yLow,yHigh);
				
				
			} else if (type.equalsIgnoreCase("GoInAStrightLineFor")) {
				Integer dist = getDistance(element);

				return new GoInAStrightLineFor(actor, dist);

			} else if (type.equalsIgnoreCase("DrawArcFasterVersion1")) {
				return new DrawArcFasterVersion1(actor, flatLand);
			} else if (type.equalsIgnoreCase("DrawArc")) {
				return new DrawArc(actor, flatLand);
			} else if (type.equalsIgnoreCase("DrawAProtoCloud")) {
				return new DrawAProtoCloud(actor, flatLand);
			} else if (type.equalsIgnoreCase("DrawACircle")) {
				return new DrawACircle(actor, flatLand);
			} else if (type.equalsIgnoreCase("DrawABlob")) {
				return new DrawABlob(actor, flatLand);
			} else if (type.equalsIgnoreCase("ClearFlatLand")) {
				return new ClearFlatLand(actor, flatLand);
			}
		}
		return new NoAction(actor);

	}
}
