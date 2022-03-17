from time import sleep
from multiprocessing import Lock
from pynput.keyboard import Key, Listener, KeyCode
from device import Device

NUMBER_KEYS = {KeyCode.from_char(i):i for i in range(10)}
QUIT_KEYS = [KeyCode.from_char('q'), Key.esc]

class Controller:
    def __init__(self, device: Device) -> None:
        self.lock = Lock()
        self.device = device
        self.running = False
        self.cached_msg = 0
        self.keyStates = {
            Key.up: False, Key.down: False, Key.left: False, Key.right: False,
            KeyCode.from_char('w'): False, KeyCode.from_char('s'): False,
            KeyCode.from_char('a'): False, KeyCode.from_char('d'): False
        }
        self.listener = Listener(on_press=self.on_press_handler)

    def on_press_handler(self, key) -> None:
        # Test Program: send numbers to device
        if key in NUMBER_KEYS:
            print(f"Sending number {NUMBER_KEYS[key]} to device.")
            self.lock.acquire()
            try:
                self.device.send(NUMBER_KEYS[key])
                self.cached_msg = NUMBER_KEYS[key]
            except Exception as e:
                print(e)
                self.running = False
            self.lock.release()
        if key in QUIT_KEYS:
            self.lock.acquire()
            self.running = False
            self.lock.release()

    def run(self) -> None:
        # Set up a listener, then enter a while true loop
        self.running = True
        running = True

        self.listener.start()
        
        while running:
            sleep(2)
            # Acquire lock and send cached msg
            self.lock.acquire()
            try:
                self.device.send(self.cached_msg)
                # Check if we should continue to run
                running = self.running
            except Exception as e:
                print(e)
                running = False
            self.lock.release()
