import asyncio
from bluetooth import discover_devices
from bleak import BleakScanner
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


def getAnswer(question) -> bool:
    while True:
        response = input(question)
        if response.lower().startswith('y'):
            return True
        if response.lower().startswith('n'):
            return False

async def ble_scan(timeout=5):
    devices = await BleakScanner.discover(timeout=timeout)
    dev_list = []
    for d in devices:
        if (d.name.replace("-", ":") == d.address):
            # device name is set to the mac address if bleak can't read
            # its actual name
            d.name = ""
        dev_list.append((d.address, d.name))
    return dev_list

async def get_device_list(use_ble=False):
    rerun = True
    while rerun: # scan loop
        print("Scanning for nearby devices...")
        if use_ble:
            device_list = await ble_scan()
        else:
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
        if not rerun:
            return device_list[idx]

async def control_loop(device: Device):
    while True: # connect loop
        print(f"Connecting to {device.name}...")
        try:
            await device.connect()
            break
        except Exception as e:
            await device.reset()
            print(e)
            print(f"Failed to connect to device {device.name}.")
            retry = getAnswer("Would you like to retry? ([y]es/[n]o) ")
            if (retry):
                continue
            else:
                exit(0)

    print("Connected")
    print("Test: Use number keys to send numbers to the device")
    print(f"Controller: Use WASD / {ARROW_KEYS} to control robot movements")
    print("Quit: 'q' or Esc")
    ctrl = Controller(device)
    enable_echo(False)
    await ctrl.run()
    await device.disconnect()
    enable_echo(True)

async def main():
    print("HM-x and HC-08 modules use BLE technology")
    print("HC-05 and HC-06 modules use classic bluetooth technology")
    use_ble = getAnswer("Are you using a BLE module? [y]es/[n]o: ")
    mac, name = await get_device_list(use_ble)
    device = Device(name, mac, isBLE=use_ble)
    await control_loop(device)
    
if __name__ == "__main__":
    asyncio.run(main())
