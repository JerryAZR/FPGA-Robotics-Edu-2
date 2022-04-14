# Files
FILE = ./*.v ../common/*.v

.PHONY: all clean flash

all: synth flash

synth: $(FILE)
	webfpga synth $^

flash:
	webfpga flash bitstream.bin

clean:
	rm bitstream.bin
