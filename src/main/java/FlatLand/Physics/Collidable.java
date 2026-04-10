package FlatLand.Physics;

import FlatLander.BoundingBox;

public interface Collidable {
    boolean collidesWith(Collidable other);
    boolean passesThrough(Collidable other);
    CollisionSide collidesFrom(Collidable other);
	BoundingBox getCurrentBoundingBox();
}
