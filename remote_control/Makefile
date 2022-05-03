# Files
FILE = ./*.v ../common/*.v

.PHONY: all clean flash local

all: synth flash

synth: $(FILE)
	webfpga synth $^

flash:
	webfpga flash bitstream.bin

local:
	# if build folder doesn't exist, create it
	mkdir -p build

	# synthesize using Yosys
	yosys -p "synth_ice40 -top fpga_top -json build/out.json" $(FILE)

	# Place and route using arachne
	nextpnr-ice40 --up5k --package sg48 \
		--asc "build/out.asc" --pcf "../common/pinmap.pcf"\
		--json "build/out.json"

	# Convert to bitstream using IcePack
	icepack "build/out.asc" "build/out.bin"

clean:
	rm -rf bitstream.bin build
