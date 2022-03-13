# Bluetooth Test

## Build & Run Instructions

Run `make synth` to generate the bitstream and `make flash` to program the FPGA.

Connect the TXD pin on the Bluetooth module to the slot for ir sensor 0.
Connect GND to ground and VCC to 3.3V power supply.

Pair your phone with the Bluetooth module. The one used in this example is named
"HC-06", but yours might have a different name.

After the module is connected to your phone, you may send numbers to it from
the phone app and see the LED blink.

## Summary