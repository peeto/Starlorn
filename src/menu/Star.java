package edu.stuy.starlorn.menu;

import org.lwjgl.opengl.GL11;

public class Star extends Rectangle {

    private double size, svel, dispwidth, dispheight;
    private int color;
    private boolean show;

    public Star(double displayx, double displayy, double s) {
        super(Math.random() * (displayx - s + 1),
              Math.random() * (displayy - s + 1), s, s);
        size = s;
        svel = 0;
        dispwidth = displayx;
        dispheight = displayy;
        show = false;
    }

    public void draw() {
        if (show) {
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2d(getXcor(), getYcor());
            GL11.glVertex2d(getXcor() + size, getYcor());
            GL11.glVertex2d(getXcor() + size, getYcor() + size);
            GL11.glVertex2d(getXcor(), getYcor() + size);
            GL11.glEnd();
        }
    }

    public double setSize(double s) {
        double temp = size;
        size = s;
        return size;
    }

    public void update(int delta) {
        super.update(delta);
        double cx = dispwidth / 2, cy = dispheight / 2;
        double dist = Math.sqrt(
              Math.abs(cx - getXcor()) * Math.abs(cx - getXcor())
            + Math.abs(cy - getYcor()) * Math.abs(cy - getYcor()));

        setXvel((getXcor() - cx) / 1);
        setYvel((getYcor() - cy) / 1);

        size += svel;
        svel = dist * dist / 700000;

        if (getXcor() <= 0 || getYcor() <= 0 || getXcor() >= dispwidth
                || getYcor() >= dispheight) {
            setXcor(Math.random() * dispwidth);
            setYcor(Math.random() * dispheight);
            setXvel(0);
            setYvel(0);
            size = 1;
            show = true;
        }

    }

}
