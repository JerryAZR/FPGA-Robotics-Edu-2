all: synth flash

synth: ./*.v ../common/*.v
	webfpga synth $^

flash:
	webfpga flash bitstream.bin