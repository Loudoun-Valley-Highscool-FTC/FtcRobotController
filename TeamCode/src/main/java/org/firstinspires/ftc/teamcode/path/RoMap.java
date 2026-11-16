package org.firstinspires.ftc.teamcode.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoMap {

    public static class Obstacle {
        public final String name;
        public final double centerX, centerY;
        public final double width, height;
        public final double minX, maxX, minY, maxY;

         //Creates an Obstacle using center coordinates (centerX, centerY) and dimensions (width, height).

        public Obstacle(String name, double centerX, double centerY, double width, double height) {
            this.name = name;
            this.centerX = centerX;
            this.centerY = centerY;
            this.width = width;
            this.height = height;

            double halfWidth = width / 2.0;
            double halfHeight = height / 2.0;
            this.minX = centerX - halfWidth;
            this.maxX = centerX + halfWidth;
            this.minY = centerY - halfHeight;
            this.maxY = centerY + halfHeight;
        }

        public boolean contains(double x, double y) {
            return x >= minX && x <= maxX && y >= minY && y <= maxY;
        }

        public boolean intersectsSegment(double x1, double y1, double x2, double y2) {
            if (contains(x1, y1) || contains(x2, y2)) return true;

            double dx = x2 - x1;
            double dy = y2 - y1;
            double tMin = 0.0;
            double tMax = 1.0;

            double[] p = {-dx, dx, -dy, dy};
            double[] q = {x1 - minX, maxX - x1, y1 - minY, maxY - y1};

            for (int i = 0; i < 4; i++) {
                if (p[i] == 0) {
                    if (q[i] < 0) return false;
                } else {
                    double t = q[i] / p[i];
                    if (p[i] < 0) {
                        if (t > tMax) return false;
                        if (t > tMin) tMin = t;
                    } else {
                        if (t < tMin) return false;
                        if (t < tMax) tMax = t;
                    }
                }
            }
            return tMin <= tMax;
        }
    }



    public static class MovementStep {
        public final Position2D targetPosition;
        public final double speed;
        public final double timeSeconds;
        public final double distance;

        public MovementStep(double headingDegrees, double speed, double timeSeconds) {
            this(new Position2D(0, 0, headingDegrees), speed, timeSeconds);
        }

        public MovementStep(Position2D targetPosition, double speed, double timeSeconds) {
            this.targetPosition = targetPosition;
            this.speed = speed;
            this.timeSeconds = timeSeconds;
            this.distance = speed * timeSeconds;
        }

        public double getHeadingDegrees() { return targetPosition.getHeadingDegrees(); }
        public double getHeadingRadians() { return targetPosition.getHeadingRadians(); }
    }

    private final double fieldSizeMeters;
    private final Position2D homePosition;
    private Position2D currentPosition;
    private final List<Obstacle> obstacles;

    public RoMap(double fieldSizeMeters, Position2D homePosition) {
        this.fieldSizeMeters = fieldSizeMeters;
        this.homePosition = homePosition;
        this.currentPosition = homePosition;
        this.obstacles = new ArrayList<>();
    }

    public void addObstacle(Obstacle obstacle) {
        obstacles.add(obstacle);
    }

    public void addObstacle(String name, double centerX, double centerY, double width, double height) {
        obstacles.add(new Obstacle(name, centerX, centerY, width, height));
    }

    public boolean isPathClear(Position2D start, Position2D end) {
        for (int i = 0; i < obstacles.size(); i++) {
            if (obstacles.get(i).intersectsSegment(start.getX(), start.getY(), end.getX(), end.getY())) {
                return false;
            }
        }
        return true;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public void updatePosition(double headingDegrees, double speed, double timeSeconds) {
        updatePosition(new Position2D(0, 0, headingDegrees), speed, timeSeconds);
    }

    // Updates live position with vector calculations
    public void updatePosition(Position2D stepDirection, double speed, double timeSeconds) {
        MovementStep step = new MovementStep(stepDirection, speed, timeSeconds);

        double dx = step.distance * stepDirection.getCos();
        double dy = step.distance * stepDirection.getSin();

        Position2D nextPosition = currentPosition.plus(dx, dy).rotateHeading(stepDirection.getHeadingDegrees() - currentPosition.getHeadingDegrees());

        double boundaryLimit = fieldSizeMeters / 2.0;
        if (Math.abs(nextPosition.getX()) > boundaryLimit || Math.abs(nextPosition.getY()) > boundaryLimit) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "Movement exceeds field boundary limit (%.2f): %s", boundaryLimit, nextPosition));
        }

        currentPosition = nextPosition;
    }

    // Alias for updating position
    public void recordMovement(double headingDegrees, double speed, double timeSeconds) {
        updatePosition(headingDegrees, speed, timeSeconds);
    }

    public void recordMovement(Position2D stepDirection, double speed, double timeSeconds) {
        updatePosition(stepDirection, speed, timeSeconds);
    }

    //as crow flies
    public List<MovementStep> calculateDirectRoute(double speed) {
        List<MovementStep> steps = new ArrayList<>();
        double safeSpeed = (speed <= 0) ? 0.5 : speed;
        double distance = currentPosition.distanceTo(homePosition);

        if (distance > 0.001) {
            double headingDegrees = currentPosition.angleTo(homePosition);
            steps.add(new MovementStep(headingDegrees, safeSpeed, distance / safeSpeed));
        }
        return steps;
    }

    //hug edge so no crash
    public List<MovementStep> calculateEdgeRoute(double speed) {
        List<MovementStep> steps = new ArrayList<>();
        double safeSpeed = (speed <= 0) ? 0.5 : speed;

        double edgeMargin = (fieldSizeMeters / 2.0) * 0.85; // Using configurable or constant safety margin
        double edgeX = (currentPosition.getX() >= 0) ? edgeMargin : -edgeMargin;

        Position2D p1_wallEdge = new Position2D(edgeX, currentPosition.getY(), 0);
        Position2D p2_cornerEdge = new Position2D(edgeX, homePosition.getY(), 0);

        //move to wall
        double dist1 = currentPosition.distanceTo(p1_wallEdge);
        if (dist1 > 0.001) {
            double head1 = currentPosition.angleTo(p1_wallEdge);
            steps.add(new MovementStep(head1, safeSpeed, dist1 / safeSpeed));
        }

        //follow perimeter wall line to home corner
        double dist2 = p1_wallEdge.distanceTo(p2_cornerEdge);
        if (dist2 > 0.001) {
            double head2 = p1_wallEdge.angleTo(p2_cornerEdge);
            steps.add(new MovementStep(head2, safeSpeed, dist2 / safeSpeed));
        }
        double dist3 = p2_cornerEdge.distanceTo(homePosition);
        if (dist3 > 0.001) {
            double head3 = p2_cornerEdge.angleTo(homePosition);
            steps.add(new MovementStep(head3, safeSpeed, dist3 / safeSpeed));
        }

        return steps;
    }

    public Position2D getHomePosition() { return homePosition; }
    public Position2D getCurrentPosition() { return currentPosition; }
    public double getFieldSizeMeters() { return fieldSizeMeters; }
}
