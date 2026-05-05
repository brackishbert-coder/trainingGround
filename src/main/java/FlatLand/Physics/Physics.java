package FlatLand.Physics;

import java.util.ArrayList;
import java.util.List;

import FlatLander.FlatLandFacebook;
import FlatLander.FlatLander;
import Logging.LOG;
import XMLLEVELLOADER.FlatLanderWrper;
import XMLLEVELLOADER.PlayerWrper;

public class Physics extends LOG implements Actions.Physics  {

	private Double gravity;
    private double airResistance; // New field for air resistance
    private int cameraPosYinFlatland;
    private int cameraHeight;

    public Physics(double gravity, double airResistance, int cameraPosYinFlatland, int cameraHeight) {
        this.gravity = gravity;
        this.airResistance = airResistance;
        this.cameraPosYinFlatland = cameraPosYinFlatland;
        this.cameraHeight = cameraHeight;
    }


	public Integer fallDistance(FlatLander flatLander) {
        double mass = flatLander.getMass(); // Assuming FlatLander has a getMass() method
        double time = UpdateTimeSingleton.getInstance().getCurrentTime();

        // Calculate acceleration with air resistance
        double acceleration = gravity - (airResistance / mass);

        // Ensure acceleration is not negative
        if (acceleration < 0) {
            acceleration = 0;
        }

        // Calculate velocity and distance
        double velocity = acceleration * time;
        double distance = 0.5 * acceleration * Math.pow(time, 2);

        if (distance > 0 && distance < 1) {
            return 1;
        }

        return (int) distance;
    }

	public void applyPhysics() {
		ArrayList<FlatLander> bookOfFlatLanders = FlatLandFacebook.getInstance().getFlatlanderFaceBook();

		for (FlatLander flatLander : bookOfFlatLanders) {
			if (flatLander.shouldPhysicsApply()) {
				Integer fallDistance = fallDistance(flatLander);
				int moveY = flatLander.getMoveY() + fallDistance;

				flatLander.changeMoveYBy(moveY);
				flatLander.updatecurrentBB();
				checkForCollisions(bookOfFlatLanders);
			}
		}

	}

private void checkForCollisions(List<FlatLander> entities) {

    for (FlatLander a : entities) {
        boolean above = false;
        CollisionSide side = null;
        FlatLander hit = null;

        for (FlatLander b : entities) {
            if (a == b) continue;

            if (!(a instanceof Collidable ca) || !(b instanceof Collidable cb)) {
                continue;
            }

            if (ca.collidesWith(cb) || ca.passesThrough(cb)) {
                side = ca.collidesFrom(cb);
                hit = b;
                break;
            }

            if (a.above(b)) {
                above = true;
                break;
            }
        }

        if (side != null) {
            resolveCollision(a, hit, side);
        } else if (above) {
            a.setMoveY(0);
        }
    }
}
private void resolveCollision(
        FlatLander mover,
        FlatLander obstacle,
        CollisionSide side) {

    int dx = 0;
    int dy = 0;

    switch (side) {
        case TOP -> dy = -1;
        case BOTTOM -> dy = 1;
        case LEFT -> dx = -1;
        case RIGHT -> dx = 1;
    }

    Collidable a = (Collidable) mover;
    Collidable b = (Collidable) obstacle;

    while (a.collidesWith(b)) {
    	mover.update();
    	obstacle.update();
        mover.setMoveX(dx);
        mover.setMoveY(dy);
        obstacle.setMoveX(-dx);
        obstacle.setMoveY(-dy);
    }
}


	private void checkEntity(FlatLander flatLander, int collidesFrom,
			FlatLander flatLanderToCheckForCollisionsCollided) {
		if (collidesFrom == 1) {
			// top
			if (flatLander instanceof PlayerWrper) {
				PlayerWrper flatLander2 = (PlayerWrper) flatLander;
				if (flatLanderToCheckForCollisionsCollided instanceof FlatLanderWrper) {
					while (flatLander2.collidesWith(((FlatLanderWrper) flatLanderToCheckForCollisionsCollided))
							|| flatLander2.passesThrough(((FlatLanderWrper) flatLanderToCheckForCollisionsCollided))) {
						flatLander2.changeMoveYBy(-1);
						flatLander2.update();

					}
				}
			} else {
				if (flatLanderToCheckForCollisionsCollided instanceof FlatLanderWrper
						&& flatLander instanceof FlatLanderWrper) {
					while (((FlatLanderWrper) flatLander)
							.collidesWith(((FlatLanderWrper) flatLanderToCheckForCollisionsCollided))
							|| ((FlatLanderWrper) flatLander)
									.passesThrough(((FlatLanderWrper) flatLanderToCheckForCollisionsCollided))) {
						flatLander.changeMoveYBy(-1);
						flatLander.update();
					}
				}
			}

		} else if (collidesFrom == 2) {
			// right
			if (flatLander instanceof PlayerWrper) {
				PlayerWrper flatLander2 = (PlayerWrper) flatLander;
				if (flatLanderToCheckForCollisionsCollided instanceof FlatLanderWrper) {
					while (flatLander2.collidesWith(((FlatLanderWrper) flatLanderToCheckForCollisionsCollided))
							|| flatLander2.passesThrough(((FlatLanderWrper) flatLanderToCheckForCollisionsCollided))) {
						flatLander2.changeMoveXBy(1);
						flatLander2.update();
					}
				}
			} else if (flatLander instanceof FlatLanderWrper) {
				{

					if (flatLanderToCheckForCollisionsCollided instanceof FlatLanderWrper) {
						while (((FlatLanderWrper) flatLander)
								.collidesWith(((FlatLanderWrper) flatLanderToCheckForCollisionsCollided))
								|| ((FlatLanderWrper) flatLander)
										.passesThrough(((FlatLanderWrper) flatLanderToCheckForCollisionsCollided))) {
							flatLander.changeMoveXBy(1);
							flatLander.update();
						}
					}
				}
			} else if (collidesFrom == 3) {
				// bottem

				if (flatLander instanceof PlayerWrper) {
					PlayerWrper flatLander2 = (PlayerWrper) flatLander;
					if (flatLanderToCheckForCollisionsCollided instanceof FlatLanderWrper) {
						while (flatLander2.collidesWith(((FlatLanderWrper) flatLanderToCheckForCollisionsCollided))
								|| flatLander2
										.passesThrough(((FlatLanderWrper) flatLanderToCheckForCollisionsCollided))) {
							flatLander2.changeMoveYBy(1);
							flatLander2.update();
						}
					}
					LOG.println("hey");
				} else {

					if (flatLanderToCheckForCollisionsCollided instanceof FlatLanderWrper
							&& flatLander instanceof FlatLanderWrper) {
						while (((FlatLanderWrper) flatLander)
								.collidesWith(((FlatLanderWrper) flatLanderToCheckForCollisionsCollided))
								|| ((FlatLanderWrper) flatLander)
										.passesThrough(((FlatLanderWrper) flatLanderToCheckForCollisionsCollided))) {
							flatLander.changeMoveYBy(1);
							flatLander.update();
						}
					}
				}

			} else if (collidesFrom == 4) {
				// left
				if (flatLander instanceof PlayerWrper) {
					PlayerWrper flatLander2 = (PlayerWrper) flatLander;
					if (flatLanderToCheckForCollisionsCollided instanceof FlatLanderWrper) {
						while (flatLander2.collidesWith(((FlatLanderWrper) flatLanderToCheckForCollisionsCollided))
								|| flatLander2
										.passesThrough(((FlatLanderWrper) flatLanderToCheckForCollisionsCollided))) {
							flatLander2.changeMoveXBy(-1);
							flatLander2.update();

						}
					}
				} else {

					if (flatLanderToCheckForCollisionsCollided instanceof FlatLanderWrper
							&& flatLander instanceof FlatLanderWrper) {
						while (((FlatLanderWrper) flatLander)
								.collidesWith(((FlatLanderWrper) flatLanderToCheckForCollisionsCollided))
								|| ((FlatLanderWrper) flatLander)
										.passesThrough(((FlatLanderWrper) flatLanderToCheckForCollisionsCollided))) {
							flatLander.changeMoveXBy(-1);
							flatLander.update();

						}
					}
				}
			}
		}
	}

	
    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public void setAirResistance(double airResistance) {
        this.airResistance = airResistance;
    }
	
	public void some_awesome_function_that_is_totaly_finished_and_not_made_up_oh_hey_look_over_there(
			double somefuckingnumberthatisjustfuckingmadeupbyheywhoare_you_what_are_you_doing_arrrrrrrrgh,
			int your_currentweighttimeforIT_seconds, int your_currentweighttimeforIT_minuts,
			int your_currentweighttimeforIT_hours, int your_currentweighttimeforIT_days,
			int your_currentweighttimeforIT_weeks, int your_currentweighttimeforIT_months,
			int your_currentweighttimeforIT_Years, int your_currentweighttimeforIT_decades,
			int somethingIcallAweekoyear, int s0m3_aBRACOBRDOBRADUBUCIAIcallYestevinsgiving,
			int mytotalbankedXXX_user_ACCESS_RESTRICTED_XXX) {
		// TODO Auto-generated method stub
		
	}
}
