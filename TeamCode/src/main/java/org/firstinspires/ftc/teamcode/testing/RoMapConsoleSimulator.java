package org.firstinspires.ftc.teamcode.testing;

import org.firstinspires.ftc.teamcode.path.Position2D;
import org.firstinspires.ftc.teamcode.path.RoMap;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Interactive Console Simulator for RoMap & Position2D.
 * Runs standalone on any desktop JVM or IDE Run configuration without Android dependencies.
 * Allows entering single-line movement commands (speed, time, angle)
 * or 'RTH' (Return To Home) in the terminal and renders a visual ASCII field map.
 */
public class RoMapConsoleSimulator {

    private static final double FIELD_SIZE_FEET = 12.0;
    private static final Position2D HOME_POSITION = new Position2D(-6.0, -6.0, 0.0);
    private static final int GRID_SIZE = 13; // 13x13 ASCII grid

    public static void main(String[] args) {
        RoMap map = new RoMap(FIELD_SIZE_FEET, HOME_POSITION);

        printWelcomeHeader();
        drawFieldMap(map);

        try (Scanner scanner = new Scanner(System.in)) {
            // Check if interactive console input is available
            if (!scanner.hasNextLine()) {
                runAutomatedDemo(map);
                return;
            }

            while (true) {
                System.out.print("\n[RoMap Console] Enter command (or 'help'): ");
                if (!scanner.hasNextLine()) break;

                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                String commandUpper = input.toUpperCase();

                if (commandUpper.equals("EXIT") || commandUpper.equals("QUIT") || commandUpper.equals("Q")) {
                    System.out.println("Exiting RoMap Console Simulator. Goodbye!");
                    break;
                }

                if (commandUpper.equals("HELP") || commandUpper.equals("H")) {
                    printHelp();
                    continue;
                }

                if (commandUpper.equals("RESET")) {
                    map = new RoMap(FIELD_SIZE_FEET, HOME_POSITION);
                    System.out.println("✓ Map reset back to Home (-6, -6).");
                    drawFieldMap(map);
                    continue;
                }

                if (commandUpper.startsWith("RTH") || commandUpper.equals("HOME") || commandUpper.equals("RETURN")) {
                    handleReturnToHome(map, input);
                    continue;
                }

                handleMovementCommand(map, input);
            }
        } catch (NoSuchElementException e) {
            // Non-interactive IDE input stream ended; run automated demonstration
            runAutomatedDemo(map);
        } catch (Exception e) {
            System.err.println("Console Simulator Error: " + e.getMessage());
        }
    }

    private static void runAutomatedDemo(RoMap map) {
        System.out.println("\n--- RUNNING AUTOMATED DEMONSTRATION MODE ---");
        
        System.out.println("\n>>> Step 1: Move outbound (Speed=0.5, Time=2.0s, Angle=45.0°)");
        handleMovementCommand(map, "0.5 2.0 45");

        System.out.println("\n>>> Step 2: Move outbound (Speed=0.4, Time=3.0s, Angle=90.0°)");
        handleMovementCommand(map, "0.4 3.0 90");

        System.out.println("\n>>> Step 3: Trigger Return To Home (RTH)");
        handleReturnToHome(map, "RTH");

        System.out.println("\n>>> Demonstration Complete!");
    }

    private static void handleMovementCommand(RoMap map, String input) {
        String[] parts = input.split("\\s+");
        if (parts.length < 3) {
            System.out.println("❌ Invalid Format! Enter: <speed> <time> <angle> (e.g. '0.5 2.0 45') or 'RTH'");
            return;
        }

        try {
            double speed = Double.parseDouble(parts[0]);
            double time = Double.parseDouble(parts[1]);
            double angle = Double.parseDouble(parts[2]);

            if (speed < 0 || time < 0) {
                System.out.println("⚠️ Warning: Speed and time should be non-negative values.");
            }

            map.updatePosition(angle, speed, time);
            System.out.printf("✓ Moved: Speed=%.2f, Time=%.2fs, Angle=%.1f° | New Pos: %s%n",
                    speed, time, angle, map.getCurrentPosition());

            drawFieldMap(map);

        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid Number Format! Please enter numeric values (e.g. '0.5 2.0 45').");
        } catch (IllegalArgumentException e) {
            System.out.println("⛔ Movement Blocked: " + e.getMessage());
        }
    }

    private static void handleReturnToHome(RoMap map, String input) {
        Position2D current = map.getCurrentPosition();
        if (current.distanceTo(HOME_POSITION) < 0.001) {
            System.out.println("ℹ️ Robot is already at Home position (-6.0, -6.0)!");
            drawFieldMap(map);
            return;
        }

        boolean useEdgeRoute = input.toUpperCase().contains("EDGE");
        double returnSpeed = 0.8;

        List<RoMap.MovementStep> returnRoute = useEdgeRoute ?
                map.calculateEdgeRoute(returnSpeed) :
                map.calculateDirectRoute(returnSpeed);

        String routeType = useEdgeRoute ? "Edge Perimeter Route" : "Direct Vector Route (As Crow Flies)";
        System.out.printf("🚀 Initiating RTH via %s (%d step(s))...%n", routeType, returnRoute.size());

        for (int i = 0; i < returnRoute.size(); i++) {
            RoMap.MovementStep step = returnRoute.get(i);
            System.out.printf("  [Step %d/%d] Heading: %.1f°, Speed: %.2f, Time: %.2fs (Dist: %.2fft)%n",
                    i + 1, returnRoute.size(), step.getHeadingDegrees(), step.speed, step.timeSeconds, step.distance);

            map.updatePosition(step.getHeadingDegrees(), step.speed, step.timeSeconds);
        }

        System.out.printf("✅ Return To Home Complete! Current Pos: %s%n", map.getCurrentPosition());
        drawFieldMap(map);
    }

    private static void drawFieldMap(RoMap map) {
        char[][] grid = new char[GRID_SIZE][GRID_SIZE];

        // Fill background
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                grid[r][c] = '.';
            }
        }

        // Mark center of field (0,0)
        grid[GRID_SIZE / 2][GRID_SIZE / 2] = '+';

        // Mark Home position (-6, -6)
        int homeR = posToRow(-6.0);
        int homeC = posToCol(-6.0);
        grid[homeR][homeC] = 'H';

        // Mark Obstacles
        for (RoMap.Obstacle obs : map.getObstacles()) {
            int obsR = posToRow(obs.centerY);
            int obsC = posToCol(obs.centerX);
            if (isValidGridPoint(obsR, obsC)) {
                grid[obsR][obsC] = 'O';
            }
        }

        // Mark Robot position
        Position2D robotPos = map.getCurrentPosition();
        int robotR = posToRow(robotPos.getY());
        int robotC = posToCol(robotPos.getX());

        if (isValidGridPoint(robotR, robotC)) {
            grid[robotR][robotC] = 'R';
        }

        // Render ASCII Board
        System.out.println("\n============== 12x12 FT FIELD MAP ==============");
        System.out.println("  +X -> Right (+6ft) | +Y -> Top (+6ft)");
        System.out.println("  H = Home (-6,-6)   | R = Robot  | + = Center (0,0)");
        System.out.println("  ---------------------------------------------");

        for (int r = 0; r < GRID_SIZE; r++) {
            double worldY = rowToPos(r);
            System.out.printf("%+5.1ff | ", worldY);
            for (int c = 0; c < GRID_SIZE; c++) {
                System.out.print(grid[r][c] + " ");
            }
            System.out.println("|");
        }

        System.out.println("  ---------------------------------------------");
        System.out.print("         ");
        for (int c = 0; c < GRID_SIZE; c += 2) {
            System.out.printf("%+5.1ff", colToPos(c));
        }
        System.out.println("\n================================================");
    }

    private static int posToRow(double y) {
        int r = (int) Math.round((6.0 - y) / FIELD_SIZE_FEET * (GRID_SIZE - 1));
        return Math.max(0, Math.min(GRID_SIZE - 1, r));
    }

    private static int posToCol(double x) {
        int c = (int) Math.round((x + 6.0) / FIELD_SIZE_FEET * (GRID_SIZE - 1));
        return Math.max(0, Math.min(GRID_SIZE - 1, c));
    }

    private static double rowToPos(int r) {
        return 6.0 - (r * FIELD_SIZE_FEET / (GRID_SIZE - 1));
    }

    private static double colToPos(int c) {
        return -6.0 + (c * FIELD_SIZE_FEET / (GRID_SIZE - 1));
    }

    private static boolean isValidGridPoint(int r, int c) {
        return r >= 0 && r < GRID_SIZE && c >= 0 && c < GRID_SIZE;
    }

    private static void printWelcomeHeader() {
        System.out.println("=================================================");
        System.out.println("   RoMap & Position2D Interactive Console Sim   ");
        System.out.println("=================================================");
        System.out.println("Controls:");
        System.out.println(" • Move: Enter '<speed> <time> <angle>' (e.g. '0.5 2.0 45')");
        System.out.println(" • Return To Home: Enter 'RTH' or 'RTH edge'");
        System.out.println(" • Reset Map: Enter 'RESET'");
        System.out.println(" • Help: Enter 'HELP'");
        System.out.println(" • Exit: Enter 'EXIT'");
    }

    private static void printHelp() {
        System.out.println("\n--- HELP MENU ---");
        System.out.println("1. Movement Command: '<speed> <time> <angle>'");
        System.out.println("   Example: '0.8 3.5 90' moves at 0.8 speed for 3.5 sec at 90° heading.");
        System.out.println("2. Return To Home: 'RTH' or 'RTH direct'");
        System.out.println("   Calculates straight 'as-the-crow-flies' direct vector back to (-6, -6).");
        System.out.println("3. Edge Return: 'RTH edge'");
        System.out.println("   Calculates perimeter route hugging field wall edges back to (-6, -6).");
        System.out.println("4. Reset: 'RESET'");
        System.out.println("   Resets robot location back to starting home corner (-6, -6).");
    }
}
