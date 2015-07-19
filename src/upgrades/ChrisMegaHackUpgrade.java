package edu.stuy.starlorn.upgrades;

public class ChrisMegaHackUpgrade extends GunUpgrade {

    public ChrisMegaHackUpgrade() {
        super();
        _name = "Chris' Mega Insane Upgrade";
        _description = "Just sit back and watch shit die";
    }

    @Override
    public String[] getSprites(String[] sprites) {
        String color = _ownedByPlayer ? "blue" : "purple";
        return new String[]{
            "bullet/missile/"+color+"/1", "bullet/missile/"+color+"/2", "bullet/missile/"+color+"/3",
            "bullet/missile/"+color+"/4", "bullet/missile/"+color+"/5"};   
    }

    @Override
    public int getNumShots() {
        return 12;
    }

    @Override
    public double getAimAngle() {
        // Random angle going forwards
        //return Math.PI * (2.0/3.0) * (Math.random() - .5);
        return Math.PI * ((Math.random() * 2) - 1);
    }

    @Override
    public double getShotSpeed(double shotspeed) {
        // Random speed, either 1, 2, or 3
        //return Math.random() * 3 + shotspeed;
	return shotspeed + 3;
    }

    @Override
    public double getCooldown(double cooldown) {
        //return cooldown * 3;
        //return cooldown * 0.1;
	return 1;
    }

    @Override
    public boolean getSeeking(boolean seeking) {
        //return Math.random() >= (3.0 / 4.0);
        return true;
   }

    @Override
    public double getAgility(double agility) {
        //return agility + (0.02 / (1 + 3 * agility));
	return Math.PI / 32;
    }

    @Override
    public Upgrade clone() {
        return new ChrisMegaHackUpgrade();
    }
}
