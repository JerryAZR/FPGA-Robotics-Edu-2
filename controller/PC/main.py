from bluetooth import discover_devices
from device import Device
from controller import Controller
import sys

platform = sys.platform
table_format = "| {:<6}| {:<12}| {:<18}|"
ARROW_KEYS = u"\u2191 \u2190 \u2193 \u2192"

if platform.lower().startswith("win"):
    import msvcrt
    def enable_echo(enable):
        while msvcrt.kbhit():
            msvcrt.getch()
else:
    import termios
    def enable_echo(enable):
        fd = sys.stdin.fileno()
        new = termios.tcgetattr(fd)
        if enable:
            new[3] |= termios.ECHO
        else:
            new[3] &= ~termios.ECHO

        termios.tcsetattr(fd, termios.TCSANOW, new)
        termios.tcflush(fd, termios.TCIFLUSH)

if __name__ == "__main__":
    rerun = True
    while rerun: # scan loop
        print("Scanning for nearby devices...")
        device_list = discover_devices(lookup_names=True)

        if (len(device_list) == 0):
            print("No device found.")
            cmd = input("Enter 'q' to quit, or any other key to retry: ")
            if (cmd == 'q'):
                exit(0)
            else:
                continue

        # We have found a list of devices
        print()
        print(table_format.format("Index", "Device Name", "Device Mac Address"))
        print("-" * len(table_format.format("", "", "")))
        for i in range(len(device_list)):
            mac, name = device_list[i]
            print(table_format.format(i, name, mac))
        print()

        # Let the user choose a device to connect to
        while True: # input loop
            print("Enter an index to connect to the selected device,")
            cmd = input("'q' to quit, or any other key to re-scan: ")
            if cmd.isnumeric():
                idx = int(cmd)
                if (idx >= len(device_list)):
                    print("Index out of range.")
                else:
                    # Valid index
                    rerun = False
                    break
            else:
                if (cmd == 'q'): # quit
                    exit(0)
                # re-scan
                else:
                    break

    # We are done with the scan phase. Now connect to the device
    mac, name = device_list[idx]
    hc06 = Device(name, mac)
    while True: # connect loop
        print(f"Connecting to {name}...")
        try:
            hc06.connect()
            break
        except Exception as e:
            hc06.reset()
            print(e)
            print(f"Failed to connect to device {name}.")
            cmd = input("Would you like to retry? ([y]es/no) ")
            if (cmd.lower().startswith('y')):
                continue
            else:
                exit(0)

    print("Connected")
    print("Test: Use number keys to send numbers to the device")
    print(f"Controller: Use WASD / {ARROW_KEYS} to control robot movements")
    print("Quit: 'q' or Esc")
    ctrl = Controller(hc06)
    enable_echo(False)
    ctrl.run()
    hc06.disconnect()
    enable_echo(True)

