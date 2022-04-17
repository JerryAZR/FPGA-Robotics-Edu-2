import sys
import re

autoAlias = [
    ("WF_LED",       31),
    ("WF_CLK",       35),
    ("WF_BUTTON",    42),
    ("WF_NEO",       32),
    ("WF_CPU1",      11),
    ("WF_CPU2",      12),
    ("WF_CPU3",      13),
    ("WF_CPU4",      10)
]

pinNameToNum = [
    17, 16, 14, 23, 20, 19, 18, 21,
    25, 26, 28, 27, 34, 35, 36, 37,
    40, 44, 46, 47, 45, 48,  2,  3,
     4,  9,  6, 43, 41, 39, 38, 15,
]
setIoTemplate = "set_io --warn-no-port {} {}\n"
mapRegex = "@MAP_IO[ \t]+([a-zA-Z_0-9]+)[ \t]+([0-9]+)"

if __name__ == "__main__":
    if (len(sys.argv) == 1):
        print("Missing input file")
    else:
        pinmap = open(sys.argv[1], "r")
        pcf = open("pinmap.pcf", "w")
        for key, val in autoAlias:
            pcf.write(setIoTemplate.format(key,val))
        f = pinmap.readline()
        while f:
            entry = re.search(mapRegex, f)
            if entry:
                portName = entry.group(1)
                portId = int(entry.group(2))
                pcf.write(setIoTemplate.format(portName, pinNameToNum[portId]))
            f = pinmap.readline()
        pinmap.close()
        pcf.close()
