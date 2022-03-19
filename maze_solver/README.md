# Maze Solver

## Build & Run Instructions

Run `make synth` to generate the bitstream and `make flash` to program the FPGA.

## Summary

This directory contains an implemenation of the maze solver project.

## Tutorial

*Also availablle on [hackster.io](https://www.hackster.io)*

### Prerequisites

* [Lab 6: IR Sensors and Line Following](https://www.hackster.io/fpga-for-robotics-education/lab-6-ir-sensors-and-line-following-c01f78)

### Introduction

In this project we will build a maze solver using the TI-RSLK robotics kit,
the IR line sensor, and a WebFPGA. To simplify the control logic, we will
only target a specific type of mazes: the "perfect" ones.

### Perfect Maze

A perfect maze contains no loops. Therefore, they can be solved using the right-hand rule,
in which one would keep the right hand on the wall until an exit (a.k.a. the goal) is found.
Our robot has no hand (unless you consider the bumper switches as "hands"), therefore we
will use another strategy: turning right whenever possible. Note that this strategt is
actually equivalent to the right-hand rule.

In this example, the maze would be drawn using black lines, and the goal would be a special
pattern (three parallel lines). Alternatively you could use the bumper switch to detect the goal
(e.g. by placing a water bottle there).

### Recognizing Patterns

Recall that the line sensor is essentially 8 infrared (IR) sensors in a row. These IR sensors
would respond differently to black and white surface. For the sake of this discussion,
we assume that thr IR sensor would be 1 if it sees black, or 0 if it sees white.
For example, the pattern

 Sensor Index | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
--------------|---|---|---|---|---|---|---|---|
 Reading      | 0 | 0 | 0 | 1 | 1 | 0 | 0 | 0 |

would mean that only the two IR sensors in the middle are directly above a black line.
This is also the "forward pattern" which indicates that the car can safely move forward.

We want the robot to turn right whenever possible, but how would the robot know when it
is possible to turn? The answer is when the line sensor sees a "right-turn pattern":

 Sensor Index | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
--------------|---|---|---|---|---|---|---|---|
 Reading      | X | X | X | 1 | 1 | 1 | 1 | 1 |

(note: the X's in this pattern means don't cares). If we would like to have more tolerance,
we could even consider IR sensor 4 and 3 to be don't cares. We encourage you to try out
different patterns here to see which one works best in your case.

Similarly, there is the "left-turn only pattern" which we would like to ignore
(because our maze-solving strategy states that the car should turn right whenever possible
while ignoring left turns).

 Sensor Index | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
--------------|---|---|---|---|---|---|---|---|
 Reading      | 1 | 1 | 1 | 1 | 1 | 0 | 0 | 0 |

If the car happens to reach the end of the road (the "empty pattern": all sensor readings are 0's),
then we want the car to make a 180-degree turn. If a left turn was ignored right before reaching
the end of the road, it would now become a right turn which the robot would take based on our strategy.
However, Because the line sensor is not installed right at the centor of the car, the sensor would
likely only see a "forward pattern" after the 180-degree turn is complete, even if a right turn is
actually possible. To avoid missing these right turns, we want the robot to move backward a little
to look for a "right-turn pattern".

To save you some work, we have found out that if we rotate the left wheel forward by 270 degrees,
and the right wheel backward by 90 degrees, the car would make a near-perfect 90-degree right turn
while staying on track. A 180-degree turn can be achieved by rotating one wheel forward by 360 degrees
and the other one backward by 360 degrees.

### Line Following

With pattern recognition and the corresponding control logic implemented, the robot should
be able to solve small perfect mazes. However, it will like fail when given large mazes
because the non-perfect turns and initial position could mess up the direction. We could solve
this problem by reusing the line-following control logic used in Lab 6. In the code example on GitHub,
we made a state machine as the controller, there are two "LINE_FOLLOW" states designed for
pattern recognition and direction adjustments.

### Demo

Before starting to program your own maze solver, feel free to try out the code example on GitHub.
When powered on, the system needs to be initialized before it can properly recognize the black line.
Place the car such that only some of the IR sensors cover the black line while the rest are above
the white surface (background), then press and of the bump switches. After initialization,
press the white button on the FPGA to start the car.

No starter code is provided this time because there are too many design choices one could make
when implementing the control logic.
