from time import sleep
from multiprocessing import Lock
from pynput.keyboard import Key, Listener, KeyCode
from device import Device

NUMBER_KEYS = {KeyCode.from_char(i):i for i in range(10)}
QUIT_KEYS = [KeyCode.from_char('q'), Key.esc]

MOVE_SPD = 360
TURN_SPD = 180

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
        self.listener = Listener(
            on_press=self.on_press_handler,
            on_release=self.on_release_handler
        )

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

        # Controller
        if key in self.keyStates:
            self.keyStates[key] = True
            msg1, msg2 = self.get_ctrl_cmd()
            # Acquire lock and send commands
            self.lock.acquire()
            try:
                self.device.send(msg1)
                self.device.send(msg2)
                self.cached_msg = msg2
            except Exception as e:
                print(e)
                self.running = False
            self.lock.release()

        # End the program
        if key in QUIT_KEYS:
            self.lock.acquire()
            self.running = False
            self.lock.release()

    def on_release_handler(self, key):
        # only need to handle events of controller keys
        if key in self.keyStates:
            self.keyStates[key] = False
            msg1, msg2 = self.get_ctrl_cmd()
            # Acquire lock and send commands
            self.lock.acquire()
            try:
                self.device.send(msg1)
                self.device.send(msg2)
                self.cached_msg = msg2
            except Exception as e:
                print(e)
                self.running = False
            self.lock.release()

    def get_ctrl_cmd(self):
        # up
        left_spd = 0
        right_spd = 0
        if self.keyStates[Key.up] or self.keyStates[KeyCode.from_char('w')]:
            left_spd += MOVE_SPD
            right_spd += MOVE_SPD
        if self.keyStates[Key.down] or self.keyStates[KeyCode.from_char('s')]:
            left_spd -= MOVE_SPD
            right_spd -= MOVE_SPD
        if self.keyStates[Key.left] or self.keyStates[KeyCode.from_char('a')]:
            left_spd -= TURN_SPD
            right_spd += TURN_SPD
        if self.keyStates[Key.right] or self.keyStates[KeyCode.from_char('d')]:
            left_spd += TURN_SPD
            right_spd -= TURN_SPD
        return self.make_cmds(left_spd, right_spd)

     # Expect this function to be changed later
    def make_cmds(self, left_spd, right_spd):
        return left_spd, right_spd

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
