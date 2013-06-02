package edu.stuy.starlorn.world;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import edu.stuy.starlorn.graphics.DefaultHook;
import edu.stuy.starlorn.graphics.Screen;
import edu.stuy.starlorn.entities.Bullet;
import edu.stuy.starlorn.entities.Entity;
import edu.stuy.starlorn.entities.EnemyShip;
import edu.stuy.starlorn.entities.Pickup;
import edu.stuy.starlorn.entities.PlayerShip;
import edu.stuy.starlorn.entities.Ship;
import edu.stuy.starlorn.menu.Menu;
import edu.stuy.starlorn.upgrades.Upgrade;
import edu.stuy.starlorn.util.Generator;
import edu.stuy.starlorn.util.Preferences;

/*
 * Represents a world with entities in it
 */
public class World extends DefaultHook {

    private Screen screen;
    private Font smallFont, mediumFont, bigFont;
    private ConcurrentLinkedQueue<Entity> entities;
    private ArrayList<Ship> ships;
    private PlayerShip player;
    private Level level;
    private Wave wave;
    private int playerLives, levelNo, waveNo, spawnedInWave, spawnedInLevel, killedInLevel, remaining;
    private int spawnTicks, respawnTicks;
    private boolean paused, playerAlive, waitForPickup;
    private String pickupMessage;

    public World(Screen scr) {
        screen = scr;
        smallFont = screen.getFont().deriveFont(12f);
        mediumFont = screen.getFont().deriveFont(24f);
        bigFont = screen.getFont().deriveFont(36f);
        entities = new ConcurrentLinkedQueue<Entity>();
        ships = new ArrayList<Ship>();
        player = new PlayerShip(screen.getWidth(), screen.getHeight());
        player.setWorld(this);
        ships.add(player);
        level = Generator.generateLevel(1);
        wave = level.popWave();

        playerLives = 3;
        spawnedInWave = spawnedInLevel = killedInLevel = remaining = 0;
        levelNo = waveNo = 1;
        spawnTicks = respawnTicks = 0;
        playerAlive = true;
        paused = waitForPickup = false;
        pickupMessage = null;
    }

    public void addEntity(Entity e) {
        entities.add(e);
    }

    public void removeEntity(Entity e) {
        entities.remove(e);
    }

    public void step(Graphics2D graphics) {
        stepLevelProgress();
        stepEntities(graphics);
        drawHUD(graphics);
    }

    private void stepLevelProgress() {
        if (paused)
            return;
        if (spawnedInWave < wave.getNumEnemies()) {
            if (spawnTicks < wave.getIntermission())
                spawnTicks++;
            else
                spawnEnemy();
        }
        else if (remaining == 0) {
            if (!level.isLastWave() && spawnTicks == 120)
                spawnNextWave();
            else if (level.isLastWave() && spawnTicks == 300)
                spawnNextLevel();
            else if (!waitForPickup && playerAlive)
                spawnTicks++;
        }
        if (!playerAlive) {
            if (respawnTicks == 90) {
                if (playerLives == 0) {
                    endGame();
                    return;
                }
                spawnPlayer();
            }
            else
                respawnTicks++;
        }
    }

    private void spawnEnemy() {
        spawnedInWave++;
        spawnedInLevel++;
        remaining++;
        spawnTicks = 0;
        EnemyShip ship = wave.getEnemyType().clone();
        int speed = ship.getMovementSpeed() + ((int) (2 * (Math.random() - .5)));
        ship.setMovementSpeed(speed);
        ship.setPath(Generator.generatePath(ship.getPath().getPathLength(),
                     ship.getPath().getCoords(0)[0], ship.getPath().getCoords(0)[1]));
        if (Math.random() < .1)
            ship.addUpgrade(Generator.getRandomUpgrade());
        ship.setWorld(this);
        ships.add(ship);
    }

    private void spawnNextWave() {
        waveNo++;
        spawnTicks = spawnedInWave = remaining = 0;
        wave = level.popWave();
    }

    private void spawnNextLevel() {
        levelNo++;
        waveNo = 1;
        spawnTicks = spawnedInWave = spawnedInLevel = killedInLevel = remaining = 0;
        pickupMessage = null;
        level = Generator.generateLevel(levelNo);
        wave = level.popWave();
    }

    private void spawnPlayer() {
        player = new PlayerShip(screen.getWidth(), screen.getHeight());
        player.setWorld(this);
        ships.add(player);
        playerAlive = true;
        respawnTicks = 0;
    }

    private void stepEntities(Graphics2D graphics) {
        Iterator<Entity> it = entities.iterator();
        while (it.hasNext()) {
            Entity entity = it.next();
            if (paused) {
                entity.draw(graphics);
                continue;
            }
            entity.step();
            if (entity.isDead()) {
                if (entity instanceof PlayerShip)
                    killPlayer((PlayerShip) entity);
                else if (entity instanceof EnemyShip)
                    killEnemy((EnemyShip) entity);
                else if (entity instanceof Pickup) {
                    waitForPickup = false;
                    pickupMessage = ((Pickup) entity).getUpgrade().getName();
                }
                it.remove();
            }
            else
                entity.draw(graphics);
        }
    }

    private void killPlayer(PlayerShip player) {
        playerAlive = false;
        playerLives--;
        ships.remove(player);
    }

    private void killEnemy(EnemyShip enemy) {
        remaining--;
        if (enemy.wasKilledByPlayer()) {
            killedInLevel++;
            if (killedInLevel == spawnedInLevel && spawnedInWave == wave.getNumEnemies() && level.isLastWave())
                spawnPickup(enemy);
        }
        ships.remove(enemy);
    }

    private void drawHUD(Graphics2D graphics) {
        graphics.setFont(smallFont);
        graphics.setColor(Color.WHITE);
        graphics.drawString(String.format("Level %d, Wave %d/%d", levelNo,
                            waveNo, level.numWaves()), 50, 50);
        graphics.drawString(String.format("Lives: %d", playerLives), 50, 75);
        graphics.setColor(Color.GRAY);
        graphics.drawString(String.format("Enemies: %d/%d",
                            remaining, spawnedInWave), 50, 100);

        if (paused) {
            drawPaused(graphics);
        }
        if (playerLives == 0 || (spawnedInWave == wave.getNumEnemies() && remaining == 0)) {
            drawLevelProgress(graphics);
            if (pickupMessage != null)
                drawPickupMessage(graphics);
        }
    }

    private void drawPaused(Graphics2D graphics) {
        String message = "PAUSED";
        int xOffset = (int) (screen.getWidth() - bigFont.getStringBounds(message, graphics.getFontRenderContext()).getWidth()) / 2;
        graphics.setFont(bigFont);
        graphics.setColor(Color.WHITE);
        graphics.drawString(message, xOffset, screen.getHeight() / 2 - 50);
    }

    private void drawLevelProgress(Graphics2D graphics) {
        Color color;
        String message;
        if (playerLives == 0) {
            color = Color.RED;
            message = "GAME OVER";
        }
        else if (level.isLastWave() && spawnTicks >= 30 && spawnTicks <= 270) {
            color = Color.YELLOW;
            message = String.format("LEVEL %d COMPLETE", levelNo);
        }
        else if (spawnTicks >= 30 && spawnTicks <= 90) {
            color = Color.WHITE;
            message = String.format("WAVE %d OF %d COMPLETE", waveNo, level.numWaves());
        }
        else
            return;
        int xOffset = (int) (screen.getWidth() - bigFont.getStringBounds(message, graphics.getFontRenderContext()).getWidth()) / 2;
        graphics.setFont(bigFont);
        graphics.setColor(color);
        graphics.drawString(message, xOffset, screen.getHeight() / 2);
    }

    private void drawPickupMessage(Graphics2D graphics) {
        String message = "YOU GOT: " + pickupMessage.toUpperCase();
        int xOffset = (int) (screen.getWidth() - mediumFont.getStringBounds(message, graphics.getFontRenderContext()).getWidth()) / 2;
        graphics.setFont(mediumFont);
        graphics.setColor(Color.WHITE);
        graphics.drawString(message, xOffset, screen.getHeight() / 2 + 50);
    }

    private void spawnPickup(Entity source) {
        Upgrade up = Generator.getRandomUpgrade();
        Pickup bonus = new Pickup(up, source.getRect().x, source.getRect().y);
        bonus.setWorld(this);
        waitForPickup = true;
    }

    private void endGame() {
        Menu menu = new Menu(screen);
        menu.setup();
        screen.removeHook(this);
        screen.addHook(menu);
    }

    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == Preferences.getValue("upKey"))
            player.setGoingUp(true);
        else if (event.getKeyCode() == Preferences.getValue("leftKey"))
            player.setGoingLeft(true);
        else if (event.getKeyCode() == Preferences.getValue("downKey"))
            player.setGoingDown(true);
        else if (event.getKeyCode() == Preferences.getValue("rightKey"))
            player.setGoingRight(true);
        else if (event.getKeyCode() == Preferences.getValue("shootKey"))
            player.setShootRequested(true);
        else if (event.getKeyCode() == Preferences.getValue("pauseKey"))
            paused = !paused;
    }

    public void keyReleased(KeyEvent event) {
        if (event.getKeyCode() == Preferences.getValue("upKey"))
            player.setGoingUp(false);
        else if (event.getKeyCode() == Preferences.getValue("leftKey"))
            player.setGoingLeft(false);
        else if (event.getKeyCode() == Preferences.getValue("downKey"))
            player.setGoingDown(false);
        else if (event.getKeyCode() == Preferences.getValue("rightKey"))
            player.setGoingRight(false);
        else if (event.getKeyCode() == Preferences.getValue("shootKey"))
            player.setShootRequested(false);
    }

    public Screen getScreen() {
        return screen;
    }

    public PlayerShip getPlayer() {
        return player;
    }

    public ArrayList<Ship> getShips() {
        return ships;
    }
}
