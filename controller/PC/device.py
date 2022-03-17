import socket

class Device:
    def __init__(self, name="", mac="", port=1) -> None:
        self.name = name
        self.mac = mac
        self.port = 1
        self.socket = socket.socket(
            socket.AF_BLUETOOTH, socket.SOCK_STREAM, socket.BTPROTO_RFCOMM)

    def setName(self, name) -> None:
        self.name = name

    def setMac(self, mac) -> None:
        self.mac = mac

    def connect(self) -> None:
        self.socket.connect((self.mac, self.port))

    def send(self, msg: int) -> None:
        tmp = (msg & 0xFF).to_bytes(1, "little")
        self.socket.send(tmp)

    def disconnect(self) -> None:
        self.socket.close()

    def reset(self) -> None:
        # reset the socket
        self.socket.close()
        self.socket = socket.socket(
            socket.AF_BLUETOOTH, socket.SOCK_STREAM, socket.BTPROTO_RFCOMM)
