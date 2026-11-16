package org.firstinspires.ftc.teamcode.path;

import java.util.Locale;
public class Position2D {
    private final double x;
    private final double y;
    private final double headingDegrees;
    private final double headingRadians;
    private final double cos;
    private final double sin;

    public Position2D() {
        this(0.0, 0.0, 0.0);
    }

    public Position2D(double x, double y) { //hopefully faster if rotation doesn't change
        this.x = x;
        this.y = y;
        this.headingDegrees = 0.0;
        this.headingRadians = 0.0;
        this.cos = 1.0;
        this.sin = 0.0;
    }

    public Position2D(double x, double y, double headingDegrees) { //full calc
        this.x = x;
        this.y = y;
        this.headingDegrees = normalizeDegrees(headingDegrees);
        this.headingRadians = Math.toRadians(this.headingDegrees);
        this.cos = Math.cos(this.headingRadians);
        this.sin = Math.sin(this.headingRadians);
    }
//translate jsut in case
    public static Position2D fromRadians(double x, double y, double radians) {
        return new Position2D(x, y, Math.toDegrees(radians));
    }

    public static Position2D fromDegrees(double x, double y, double degrees) {
        return new Position2D(x, y, degrees);
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getHeadingDegrees() {
        return headingDegrees;
    }
    public double getHeadingRadians() {
        return headingRadians;
    }
    public double getCos() {
        return cos;
    }
    public double getSin() {
        return sin;
    }

    public Position2D plus(double dx, double dy) { //only x, y no rotation
        return new Position2D(x + dx, y + dy, headingDegrees);
    }

    public Position2D plus(Position2D delta) { //full
        return new Position2D(x + delta.x, y + delta.y, headingDegrees + delta.headingDegrees);
    }

    public Position2D rotateHeading(double deltaDegrees) { //rotation
        return new Position2D(x, y, headingDegrees + deltaDegrees);
    }

    public double distanceTo(Position2D target) { //for return to home nav
        return Math.hypot(target.x - this.x, target.y - this.y);
    }

    public double angleTo(Position2D target) { //for return to home nav
        return Math.toDegrees(Math.atan2(target.y - this.y, target.x - this.x));
    }

    private static double normalizeDegrees(double degrees) { //fix big num degree
        double norm = degrees % 360.0;
        if (norm > 180.0) norm -= 360.0;
        if (norm <= -180.0) norm += 360.0;
        return norm;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Position2D(X: %.2fm, Y: %.2fm, Heading: %.1f°)", x, y, headingDegrees);
    }
}
