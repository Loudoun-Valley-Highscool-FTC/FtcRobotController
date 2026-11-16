package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.hardware.RobotHardware;
import org.firstinspires.ftc.teamcode.path.Position2D;
import org.firstinspires.ftc.teamcode.path.RoMap;

import java.util.List;

@Autonomous(name = "Return To Home Auto", group = "Autonomous")
public class ReturnToHomeAuto extends LinearOpMode {

    @Override
    public void runOpMode() {
        RobotHardware robot = new RobotHardware(this);
        robot.init();

        Position2D homePosition = new Position2D(0.0, 0.0, 0.0);
        RoMap map = new RoMap(3.6576, homePosition);

        waitForStart();

        // --- OUTBOUND MOVEMENT ---
        map.recordMovement(45.0, 0.5, 2.0);
        robot.arcadeDrive(0.5, 0.5);
        sleep(2000);

        map.recordMovement(90.0, 0.4, 1.5);
        robot.arcadeDrive(0.4, 0.0);
        sleep(1500);

        robot.stop();
        telemetry.addData("Outbound Current Position", map.getCurrentPosition().toString());
        telemetry.update();
        sleep(1000);

        // --- PATH GENERATION OPTIONS ---
        // 1. Direct Straight Vector Route to Home
        List<RoMap.MovementStep> directRoute = map.calculateDirectRoute(0.5);
        telemetry.addData("Direct Route Generated", "%d step(s)", directRoute.size());

        // 2. Edge-of-the-Map Perimeter Route to Home
        List<RoMap.MovementStep> edgeRoute = map.calculateEdgeRoute(0.5);
        telemetry.addData("Edge Route Generated", "%d step(s)", edgeRoute.size());
        
        telemetry.update();
        sleep(2000);
    }
}
