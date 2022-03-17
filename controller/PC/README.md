# Remote Controller (PC)

## Notes

### Dependencies

* PyBluez
* pynput

The **PyBluez** setup script uses the `use_2to3` command which is not supported
in `setuptools>=58`, so I had to install from source
```
sudo apt install libbluetooth-dev # on Linux
pip3 install git+https://github.com/pybluez/pybluez.git#egg=pybluez
```

We need a module to handle keypress events. Although the **keyboard** module
works on Windows, it requires root privilege on Linux and I do not like that.
Therefore I'm using the **pynput** module in this project instead.
```
pip3 install pynput
```

### HC-06

Config      | Default
------------|------
Baud rate   | 9600
Parity      | None
Data bits   | 8
Stop bit(s) | 1
PIN code    | 1234
