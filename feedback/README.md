# Feedback Control

## Build & Run Instructions

Run `make synth` to generate the bitstream and `make flash` to program the FPGA.

## Summary

The proportional–integral–derivative (PID) controller is a feedback control
system widely used in industry. Because the hardware implementations of the
integral (I) and derivative (D) terms are not very intuitive, we will only use
the proportional (P) term to build a simple feedback control system.

The control system implementation can be found in [speedctl.v](../common/speedctl.v).
We have also updated the [stepctl module](stepctl.v) to generate PWM using the
`speedctl` module. The top level implementation is almost identical to the one
in [encoder test 1](../encoder_test1). With the updates modules, the two
motors now run at the same speed!

## Tutorial

Also available on [hackster.io](https://www.hackster.io/jerryazr/lab-5-5-feedback-control-747153)

### Prerequisites

* [Lab 4: Motors and Movement](https://www.hackster.io/fpga-for-robotics-education/lab-4-motors-and-movement-5b9a55)

* [Lab 5: Encoders and Precision Movement](https://www.hackster.io/fpga-for-robotics-education/lab-5-encoders-and-precision-movement-b87cd3)

We assume that you already know how to control the motor speed using PWM
(pulse-width modulation) and how to read encoder outputs, so we will not cover
the basics here.

### Introduction

Recall that each motor on the TI-RSLK robot (car) is driven by a PWM signal.
A higher PWM duty cycle would mean higher speed, which would lead to more
frequent encoder pulses. While PWM alone is enough for coarse-grained speed
control, sensor (encoder in this case) inputs must also be used if we want
fine-grained control over the speed (for example, if we want a speed of
exactly 2 rotations per second)

### Raw PWM Control

Here comes the first exercise of this lab:

Modify your Lab 5 code and make both motors perform 100 rotations (36000
degrees). Feel free to define your own start condition (e.g. button press).
Please make sure that both motors are driven with the same duty cycle so
they are *theoretically* running at the same speed.

Depending on the duty cycle used, it may take a minute or two for both motors
to stop. The motors started at the same time and were driven with the same
duty cycle, but did they stop at the same time? Probably not. Because of
variations in the manufacturing process, it is common to see two devices or
components of the same model having slightly different behavior under the same
condition (motors having different speeds in this case). Our next goal is to
use feedback control to mitigate this variation.

### PID Control

The proportional–integral–derivative (PID) controller is a feedback control
system widely used in industry. Because the hardware implementations of the
integral (I) and derivative (D) terms are not very intuitive, we will only use
the proportional (P) term to build a simple feedback control system.

In a PID controller with only the P term (i.e. P controller), the input to the
system-under-control is proportional to the error (i.e. difference between the
desired and actual output value).

We define the encoder pulse count (over a predefined interval) as the system
output, and the change in duty cycle (rather than the duty cycle itself) as the
input. Intuitively, we increase the duty cycle if we see too few encoder pulses,
or decrease it if we see too many. The amount of increase or decrease is
proportional to the difference between the desired and actual number of pulses.
The following pseudo code implements this control logic:

```
# p - The p parameter in a PID system
# target_count - The desired number of pulses (degrees) per sampling interval
# actual_count - The actual number of pulses per sampling interval
# speed - the number of clock cycles in each PWM period that the signal is high

speed = speed - p * (actual_count - target_count)
```
Suppose the current `speed` is set to `8000`, `p` is set to `4`, the
`target_count` is `360`, and we observed `339` encoder pulses (`actual_count`)
in the last interval. We would then change the speed to
`8000 - 4 * (339 - 360) = 8084` in the current interval. In general, if the `p`
value is small enough, we would be one step closer to the desired speed after
each interval.

Your second exercise in this lab is to implement this control logic in hardware.

### Comparison of Control Logics

Try repeating the first exercise in this lab, but use the feedback control
module you just created to drive the motors (instead of raw PWM controls).
This time you should see both motors stopping at the same time. Hooray!

### Conclusion

**Congratulations!** You are now able to precisely control the speed of your
car. The modules you created just now will surely come in handy in future
labs.

If you are interested in PID control, we encourage you to learn more about the
topic yourself. If you would like a challenge, try adding an integral (I) term
and/or a derivative (D) term to your controller! (Hint: while we cannot
accurately model or manipulate a continuous function in our system,
it is possible to obtain approximated values that are good enough in this case.)
