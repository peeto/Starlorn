package edu.stuy.starlorn.util;

import edu.stuy.starlorn.world.Path;
import edu.stuy.starlorn.world.Wave;
import edu.stuy.starlorn.world.Level;
import edu.stuy.starlorn.entities.EnemyShip;

public class Generator {
    public static Path generatePath(int numPoints) {
        Path p = new Path();
        int screenWidth = 100;
        int screenHeight = 100;
        int firstx = (int) Math.round(Math.random()) * screenWidth; // Spawn us at one edge
        int firsty = (int) (Math.random() * screenHeight);
        p.addCoords(firstx, firsty);
        int prevx = firstx;
        int prevy = firsty;
        for (int i = 0; i < numPoints; i++) {
            // Pick locations within the size of the screen / 10 nearby the previous value, and make sure it's in the range of the screen
            int x = (prevx + (int) ((Math.random() - .5) * (screenWidth/10))) % screenWidth;
            int y = (prevy + (int) ((Math.random() - .5) * (screenHeight/10))) % screenHeight;
            p.addCoords(x, y);
            prevx = x;
            prevy = y;
        }
        p.addCoords(Math.abs(firstx - screenWidth), (int) (Math.random() *screenHeight)); // Add a point somewhere on the opposite side of spawn
        return p;
    }

    public static Path generatePath() {
        return generatePath(10);
    }

    public static Wave generateWave(int dificulty) {
        Wave wave = new Wave();
        int numEnemies = (int) (Math.random() * dificulty * dificulty);
        EnemyShip enemyType = generateEnemy(dificulty);
        Path path = generatePath();
        wave.setPath(path);
        wave.setEnemyType(enemyType);
        wave.setNumEnemies(numEnemies);
        return wave;
    }

    public static Wave generateWave() {
        return generateWave(1);
    }

    public static Level generateLevel(int numWaves, int dificulty) {
        Level level = new Level();
        for (int i = 0; i < numWaves; i++) {
            level.addWave(generateWave(dificulty));
        }
        return level;
    }

    public static Level generateLevel() {
        return generateLevel(1, 1);
    }

    public static EnemyShip generateEnemy(int dificulty) {
        EnemyShip enemy = new EnemyShip();
        Path path = generatePath(dificulty + 5);
        int damage = (int) (Math.random() * dificulty);
        int shotSpeed = (int) (Math.random() * dificulty);
        int health = (int) (Math.random() * dificulty * 10);
        int cooldown = (int) (Math.random() * (100 / dificulty) + 5);
        int cooldownRate = (int) (Math.random() * Math.log(dificulty)); // increase the cooldown rate logarithmically
        int maxSpeed = (int) (Math.random() * dificulty);
        enemy.setPath(path);
        enemy.setBaseDamage(damage);
        enemy.setBaseShotSpeed(shotSpeed);
        enemy.setMaxHealth(health);
        enemy.setBaseCooldown(cooldown);
        enemy.setCooldownRate(cooldownRate);
        enemy.setMovementSpeed(maxSpeed);
        return enemy;
    }

    public static EnemyShip generateEnemy() {
        return generateEnemy(1);
    }
}
