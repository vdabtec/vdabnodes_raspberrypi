package vdab.extnodes.raspberrypi;

import java.io.IOException;

import com.lcrc.af.AnalysisCompoundData;
import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisEvent;
import com.lcrc.af.AnalysisPolledSource;
import com.lcrc.af.input.AnalysisInput;
import com.pi4j.gpio.extension.ads.ADS1015GpioProvider;
import com.pi4j.gpio.extension.ads.ADS1015Pin;
import com.pi4j.gpio.extension.ads.ADS1x15GpioProvider.ProgrammableGainAmplifierValue;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.wiringpi.Gertboard;

public class AnalogInputGert extends AnalysisPolledSource {
	private static final double VOLTSFACTOR = 3.3D/1023D;
	public void _start(){
		super._start();
		int errNo = Gertboard.gertboardSPISetup();
		if (errNo < 0){
			_disable();
			setError("Error initializing Gertboard ERR="+errNo);
		}
	}

	@Override
	public void processData() {
		// TODO Auto-generated method stub
		int val0 = Gertboard.gertboardAnalogRead(0);
		double volts0 = ((double)val0)*VOLTSFACTOR;
		int val1 = Gertboard.gertboardAnalogRead(1);
		double volts1 = ((double)val1)*VOLTSFACTOR;
		publish(new AnalysisEvent(this, new AnalysisData("raw0",Double.valueOf(val0)))); 
		publish(new AnalysisEvent(this, new AnalysisData("raw1",Double.valueOf(val1)))); 
		publish(new AnalysisEvent(this, new AnalysisData("v0",Double.valueOf(volts0)))); 
		publish(new AnalysisEvent(this, new AnalysisData("v1",Double.valueOf(volts1)))); 
	}

}
