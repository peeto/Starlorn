package edu.stuy.starlorn.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import edu.stuy.starlorn.graphics.Anchor;

public class PlayerShip extends Ship {

    private static final int FRAMES_PER_SPRITE = 3;
    private static final int BULLETS_PER_ENEMY = 2;

    private Rectangle2D.Double hitbox;
    private float hitboxAlpha;
    private boolean goingUp, goingDown, goingLeft, goingRight, incHitboxAlpha;
    private int frame, invincibility;

    public PlayerShip(double displayx, double displayy) {
        super("player/straight/still");
        rect.x = displayx / 2 - rect.width / 2;
        rect.y = displayy - 200;
        hitbox = new Rectangle2D.Double(rect.x + rect.width / 2 - 3,
                                        rect.y + rect.height / 2 - 3, 6, 6);
        hitboxAlpha = 1;
        goingUp = goingDown = goingLeft = goingRight = incHitboxAlpha = false;
        frame = 0;
        invincibility = 120;
    }

    @Override
    public boolean isHit(Bullet b) {
        if (!b.wasFiredByPlayer() && invincibility == 0) {
            Rectangle2D.Double brect = b.getRect();
            return brect.intersects(hitbox);
        }
        return false;
    }

    @Override
    protected void spawnBullet(Bullet b) {
        super.spawnBullet(b);
        b.setFiredByPlayer(true);
    }

    @Override
    public void draw(Graphics2D graphics) {
        if ((invincibility / 2) % 3 != 1)
            super.draw(graphics);
        graphics.setColor(new Color(0.7f, 0.85f, 1f, hitboxAlpha));
        graphics.fill(hitbox);
    }

    @Override
    public void step() {
        updateVelocity();
        super.step();
        updateHitbox();
        keepOnScreen();
        updateSpriteImage();
        updateInvincibility();
    }

    private void updateVelocity() {
        if (goingUp) {
            yvel--;
            if (yvel < -maxSpeed)
                yvel = -maxSpeed;
        }
        if (goingDown) {
            yvel++;
            if (yvel > maxSpeed)
                yvel = maxSpeed;
        }
        if (!goingUp && !goingDown) {
            if (yvel > 0)
                yvel--;
            else if (yvel < 0)
                yvel++;
        }

        if (goingLeft) {
            xvel--;
            if (xvel < -maxSpeed)
                xvel = -maxSpeed;
        }
        if (goingRight) {
            xvel++;
            if (xvel > maxSpeed)
                xvel = maxSpeed;
        }
        if (!goingLeft && !goingRight) {
            if (xvel > 0)
                xvel--;
            else if (xvel < 0)
                xvel++;
        }
    }

    private void updateHitbox() {
        hitbox.x += xvel;
        hitbox.y += yvel;
        if (incHitboxAlpha) {
            hitboxAlpha += 0.1;
            if (hitboxAlpha > 1) {
                hitboxAlpha = 1;
                incHitboxAlpha = false;
            }
        }
        else {
            hitboxAlpha -= 0.1;
            if (hitboxAlpha < 0) {
                hitboxAlpha = 0;
                incHitboxAlpha = true;
            }
        }
    }

    private void keepOnScreen() {
        if (rect.x < 0) {
            rect.x = 0;
            hitbox.x = rect.x + rect.width / 2 - hitbox.width/2;
        }
        else if (rect.x > world.getScreen().getWidth() - rect.width) {
            rect.x = world.getScreen().getWidth() - rect.width;
            hitbox.x = rect.x + rect.width / 2 - hitbox.width/2;
        }
        if (rect.y < 0) {
            rect.y = 0;
            hitbox.y = rect.y + rect.height / 2 - hitbox.height;
        }
        else if (rect.y > world.getScreen().getHeight() - rect.height) {
            rect.y = world.getScreen().getHeight() - rect.height;
            hitbox.y = rect.y + rect.height / 2 - hitbox.height;
        }
    }

    private void updateSpriteImage() {
        String spritename = "player/";
        Anchor anchor;
        if (xvel < 0) {
            spritename += "left/";
            anchor = Anchor.TOP_RIGHT;
        }
        else if (xvel > 0) {
            spritename += "right/";
            anchor = Anchor.TOP_LEFT;
        }
        else {
            spritename += "straight/";
            if (sprite.getName().startsWith("player/left"))
                anchor = Anchor.TOP_RIGHT;
            else if (sprite.getName().startsWith("player/right"))
                anchor = Anchor.TOP_LEFT;
            else
                anchor = Anchor.TOP_CENTER;
        }
        if (yvel == 0)
            spritename += "still";
        else if (frame < FRAMES_PER_SPRITE)
            spritename += "fast";
        else
            spritename += "slow";
        updateSprite(spritename, anchor);
        frame++;
        if (frame >= FRAMES_PER_SPRITE * 2)
            frame = 0;
    }

    private void updateInvincibility() {
        if (invincibility > 0)
            invincibility--;
        else {
            for (Ship that : world.getShips()) {
                if (that != this && rect.intersects(that.getRect()))
                    collideWithEnemy((EnemyShip) that);
            }
        }
    }

    private void collideWithEnemy(EnemyShip enemy) {
        this.explode();
        enemy.explode();
        enemy.setKilledByPlayer(true);
        enemy.setKilledByCollision(true);
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public Ship getNearestTarget() {
        Ship closest = null;
        double distance = Double.MAX_VALUE;
        for (Ship ship : world.getShips()) {
            if (!ship.isPlayer()) {
                double newDistance = Math.pow(this.getRect().x - ship.getRect().x, 2) + Math.pow(this.getRect().y - ship.getRect().y, 2) ;
                if (newDistance < distance) {
                    closest = ship;
                    distance = newDistance;
                }
            }
        }
        return closest;
    }

    @Override
    public Ship getBestTarget() {
        Ship closest = null;
        double distance = Double.MAX_VALUE;
        ArrayList<Ship> allships = world.getShips();
        ArrayList<Ship> targeted = world.getTargetedShips();

	// get closest non-targeted ship
        for (Ship ship : allships) {
            if(!targeted.contains(ship) && !ship.isPlayer()) {
                double newDistance = Math.pow(this.getRect().x - ship.getRect().x, 2) + Math.pow(this.getRect().y - ship.getRect().y, 2) ;
                if (newDistance < distance) {
                    closest = ship;
                    distance = newDistance;
                }
            }
        }

        if (closest != null)
            return closest;

        // if all ships are already targeted, ensure each enemy gets targeted BULLETS_PER_ENEMY times.
        Set<Ship> unique = new HashSet<Ship>(targeted);
        for (Ship key : unique) {
            if ( Collections.frequency(targeted, key) <= BULLETS_PER_ENEMY) {
                closest = key;
                break;
            }
        }

        return closest;
    }

    public void setGoingUp(boolean value) {
        goingUp = value;
    }

    public void setGoingDown(boolean value) {
        goingDown = value;
    }

    public void setGoingLeft(boolean value) {
        goingLeft = value;
    }

    public void setGoingRight(boolean value) {
        goingRight = value;
    }

    public int getInvincibility() {
        return invincibility;
    }

    public void setInvincibility(int value) {
        invincibility = value;
    }
}
