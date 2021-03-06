/*
 * Copyright (c) FIRST 2008-2013. All Rights Reserved.
 * Open Source Software - may be modified and shared by FRC teams. The code
 * must be accompanied by the FIRST BSD license file in the root directory of
 * the project.
 */
package org.frc4931.prototype.subsystem;

import org.frc4931.prototype.Robot;
import org.frc4931.prototype.command.ArcadeDriveWithJoystick;
import org.frc4931.prototype.command.TankDriveWithJoysticks;
import org.frc4931.prototype.device.LogitechController.DriveStyle;
import org.frc4931.prototype.device.Throttle;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.tables.ITable;

/**
 * The drive train, which sets up and uses an {@link ArcadeDriveWithJoystick} command by default.
 */
public abstract class DriveTrain extends Subsystem {

    private static final double MAX_SPEED_FACTOR = 1.0d;
    private static final double MIN_SPEED_FACTOR = 0.0d;

    protected final SpeedController leftMotor;
    protected final SpeedController rightMotor;
    private final Throttle throttle;
    private final RobotDrive drive;

    private volatile double speedFactor = MAX_SPEED_FACTOR;

    protected DriveTrain( SpeedController leftMotor,
                          SpeedController rightMotor,
                          Throttle throttle ) {
        // Set up the motors ...
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
        this.throttle = throttle;

        // And the drive controller ...
        drive = new RobotDrive(leftMotor, rightMotor);
        drive.setInvertedMotor(Robot.DriveMotors.LEFT_POSITION, Robot.DriveMotors.LEFT_REVERSED);
        drive.setInvertedMotor(Robot.DriveMotors.RIGHT_POSITION, Robot.DriveMotors.RIGHT_REVERSED);
        drive.setSafetyEnabled(false);
        setMaxDriveSpeed(Robot.DriveMotors.INITIAL_MAX_DRIVE_SPEED);
    }

    /**
     * Initialize the default command that will be run whenever no commands for this subsystem are enqueued.
     */
    protected void initDefaultCommand() {
        setDefaultCommand(new TankDriveWithJoysticks());
    }

    /**
     * Change the default command that will be run whenever no commands for this subsystem are enqueued, using an arcade drive
     * style.
     * 
     * @param style the drive style that should be used
     */
    public void changeDefaultDriveStyleTo( DriveStyle style ) {
        setDefaultCommand(new ArcadeDriveWithJoystick());
    }

    /**
     * Change the drive style for the default command that will be run whenever no commands for this subsystem are enqueued, using
     * a tank drive style.
     */
    public void changeDefaultDriveStyle() {
        if (getDefaultCommand() instanceof TankDriveWithJoysticks) {
            setDefaultCommand(new ArcadeDriveWithJoystick());
        } else {
            setDefaultCommand(new TankDriveWithJoysticks());
        }
    }

    /**
     * Drive forward or backward.
     * <p>
     * This can be called within commands.
     * </p>
     * 
     * @param speedFactor the fraction of full-speed to drive, ranging from -1.0 (backward at full power) to 1.0 (forward at full
     *        power)
     */
    public void driveStraight( double speedFactor ) {
        drive.tankDrive(speedFactor, speedFactor);
    }

    /**
     * Stop driving.
     * <p>
     * This can be called within commands.
     * </p>
     */
    public void stopAllMotors() {
        drive.stopMotor();
    }

    /**
     * Drive using the left joystick in arcade-style.
     * <p>
     * This can be called within commands.
     * </p>
     */
    public void driveWithArcadeJoystick() {
        Robot.operatorInterface.getController().setStyle(DriveStyle.ARCADE_LEFT).drive(drive);
    }

    /**
     * Drive using two joysticks in tank style.
     * <p>
     * This can be called within commands.
     * </p>
     */
    public void driveWithTankJoysticks() {
        Robot.operatorInterface.getController().setStyle(DriveStyle.TANK).drive(drive);
    }

    /**
     * Increase or decrease the maximum drive speed by the given delta. Calling this method is safe even if the delta is out of
     * range, because the maximum drive speed will never be set smaller than 0.0 or larger than 1.0.
     * <p>
     * This can be called within commands.
     * </p>
     * 
     * @param delta the change in the maximum drive speed, between -1.0 and 1.0
     */
    public void changeMaxDriveSpeed( double delta ) {
        setMaxDriveSpeed(speedFactor + delta);
    }

    /**
     * Set the maximum drive speed by the given delta.
     * <p>
     * This can be called within commands.
     * </p>
     * 
     * @param newSpeed the new maximum drive speed, between 0.0 and 1.0; if negative, then 0.0 will be used; if greater than 1.0,
     *        then 1.0 will be used.
     */
    public void setMaxDriveSpeed( double newSpeed ) {
        Robot.print("Setting max drive speed to " + newSpeed);
        // Make sure the new speed is in range ...
        newSpeed = Math.max(newSpeed, MIN_SPEED_FACTOR);
        newSpeed = Math.min(newSpeed, MAX_SPEED_FACTOR);
        speedFactor = newSpeed;
        drive.setMaxOutput(newSpeed);
    }

    public void checkThrottleForChange() {
        if (throttle != null && throttle.hasChanged()) {
            // The throttle has been moved, so set the max drive speed ...
            setMaxDriveSpeed(throttle.getCurrentSpeed());
        }
    }

    public void initTable( ITable table ) {
        super.initTable(table);
        ITable t = getTable();
        if (t != null) {
            t.putNumber("Motor (left)", currentLeftSpeed());
            t.putNumber("Motor (right)", currentRightSpeed());
            t.putNumber("Max speed", speedFactor);
        }
    }

    protected abstract double currentLeftSpeed();

    protected abstract double currentRightSpeed();

    public abstract void addInLiveWindow();
}
