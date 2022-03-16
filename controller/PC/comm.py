import bluetooth

def getDevices():
    return bluetooth.discover_devices(lookup_names=True)

def getDeviceName(mac):
    return bluetooth.lookup_name(mac)

