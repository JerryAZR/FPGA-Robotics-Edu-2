# FPGA for Robotics Education (Part 2)

[![Android Build](https://github.com/JerryAZR/FPGA-Robotics-Edu-2/actions/workflows/build-android.yml/badge.svg)](https://github.com/JerryAZR/FPGA-Robotics-Edu-2/actions/workflows/build-android.yml)
[![Synthesis](https://github.com/JerryAZR/FPGA-Robotics-Edu-2/actions/workflows/synthesis.yml/badge.svg)](https://github.com/JerryAZR/FPGA-Robotics-Edu-2/actions/workflows/synthesis.yml)

## TODO

* Documentation on hackster.io
* Reduce speedctl response time
* Auto-stop on disconnect
* Disable forward movement on bump

## Overview

This repository contains the source code of the **FPGA for Robotics Education**
project (part 2).

Part 1 of the project can be found
[here](https://www.hackster.io/fpga-for-robotics-education/interfacing-the-ti-rslk-max-with-webfpga-for-fpga-education-7eeff0)

## Directory Organization

The [common](common/) directory contains generic modules that are used in multiple
projects.

The other directories each corresponds to a project and contains a
top level module, a Makefile, and some other private modules.

## Generic Build Guide

All the projects are systhesised using webfpga, which can be installed using
```
pip install webfpga
```

After the module is installed, run the following command to synthesis and flash bitstream (assuming python is python3).
```
python webfpga_local.py synth ./common/*.v ./<project_folder>/*.v
python webfpga_local.py flash bitstream.bin
```

On Windows, the `libusb` backend needs to be manually installed for the `flash` command to work
```
pip install libusb
pip install pyusb
```

After libusb is installed, you should be able to find `libusb-1.0.dll` at the following locations
```
<Python-install-path>\lib\site-packages\libusb\_platform\_windows\x64
<Python-install-path>\lib\site-packages\libusb\_platform\_windows\x86
```

Add the one that matches your python and system architecture to PATH and restart PowerShell. The `flash` command should work now.

For example, I have Python 3.9 (64-bit) installed and my libusb module was installed without using
a virtual environment, so the `libusb-1.0.dll` I want to use is located at:
```
C:\Users\<my-user-name>\AppData\Local\Programs\Python\Python39\lib\site-packages\libusb\_platform\_windows\x64
```

We are aware that the WebFPGA CLI utility is not exactly easy to set up on Windows, so we have built
an unofficial GUI wrapper of the command line utility.
See [the WebFPGA-GUI repo](https://github.com/JerryAZR/WebFPGA-GUI) for details.

## Notes

Bluetooth RC command format

| Bit | 7 | 6 | 5 | 4:0 |
|-----|---|---|---|-----|
| **Notes** | test=0; command=1 | left=0; right=1 | forward=0; backward=1 | speed / 32 |

## Tutorial

Also available on [hackster.io](https://www.hackster.io/jerryazr/interfacing-the-ti-rslk-max-with-webfpga-part-2-003630)

### Before You Start

Please make sure that you have completed the original project [(Interfacing the
TI RSLK MAX with WebFPGA for FPGA Education)](https://www.hackster.io/fpga-for-robotics-education/interfacing-the-ti-rslk-max-with-webfpga-for-fpga-education-7eeff0)
before starting the activities discussed in this project.

### Introduction

If you have completed the labs in the original project, you are probably
familiar with FPGA and basic Verilog programming by now. In this project, we
will demonstrate some more advanced use cases of the WebFPGA as a control unit.

### Labs

* Lab 5.5: Feedback Control ([hackster.io link](https://www.hackster.io/452951/lab-5-5-feedback-control-747153))

This lab can be attempted once you have completed [Lab 4: Motors and Movement](https://www.hackster.io/fpga-for-robotics-education/lab-4-motors-and-movement-5b9a55)
and [Lab 5: Encoders and Precision Movement](https://www.hackster.io/fpga-for-robotics-education/lab-5-encoders-and-precision-movement-b87cd3)
in the original project. Lab 6 is not a prerequisite because the IR sensor
is not used in this lab.

* Lab 7: Maze Solver ([hackster.io link](https://www.hackster.io/454408/lab-7-maze-solver-ba0cd3))

In this lab we will implement a maze solver using everything you have learned so
far. It is recommended that you complete [Lab 6: IR Sensors and Line Following](https://www.hackster.io/fpga-for-robotics-education/lab-6-ir-sensors-and-line-following-c01f78)
before starting this lab.

* Lab 8: Bluetooth UART Module ([hackster.io link](https://www.hackster.io/457040/lab-8-bluetooth-uart-module-ee2259))

In this lab we will connect a Bluetooth UART module to the robot and see how we
can send data to the robot via Bluetooth.
