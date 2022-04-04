import socket
from bleak import BleakClient

CHAR_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb"

class Device:
    def __init__(self, name="", mac="", port=1, isBLE=False, adapter=None):
        self.name = name
        self.mac = mac
        self.port = 1
        self.isBLE = isBLE
        self.socket = socket.socket(
            socket.AF_BLUETOOTH, socket.SOCK_STREAM, socket.BTPROTO_RFCOMM)
        if adapter is None:
            self.client = BleakClient(self.mac)
        else:
            self.client = BleakClient(self.mac, adapter=adapter)

    def setName(self, name) -> None:
        self.name = name

    def setMac(self, mac) -> None:
        self.mac = mac

    async def connect(self) -> None:
        if (self.isBLE):
            await self.client.connect()
        else:
            self.socket.connect((self.mac, self.port))

    async def send(self, msg: bytearray) -> None:
        if self.isBLE:
            await self.client.write_gatt_char(CHAR_UUID, msg)
        else:
            self.socket.send(msg)

    async def disconnect(self) -> None:
        if self.isBLE:
            await self.client.disconnect()
        else:
            self.socket.close()

    async def reset(self) -> None:
        # reset the socket
        if not self.isBLE:
            self.socket.close()
            self.socket = socket.socket(
                socket.AF_BLUETOOTH, socket.SOCK_STREAM, socket.BTPROTO_RFCOMM)
