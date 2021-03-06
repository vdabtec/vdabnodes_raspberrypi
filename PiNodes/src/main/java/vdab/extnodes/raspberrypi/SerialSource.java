/*LICENSE*
 * Copyright (C) 2013 - 2018 MJA Technology LLC 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package vdab.extnodes.raspberrypi;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.lcrc.af.AnalysisData;
import com.lcrc.af.AnalysisDataDef;
import com.lcrc.af.AnalysisEvent;
import com.lcrc.af.AnalysisSource;
import com.lcrc.af.util.StringUtility;

public class SerialSource extends AnalysisSource {
	private String c_SerialDevice;
	private Boolean c_ByLine = Boolean.FALSE;
	private SerialConnection c_SerialConnection = SerialConnection.getSampleConnection();
	private StringBuilder c_Buffer = new StringBuilder();
	
	public SerialSource(){
		c_SerialConnection.addInput(this);
	}
	public Boolean get_ByLine(){
		return  c_ByLine;
	}
	public void set_ByLine(Boolean byline){
		 c_ByLine = byline;
	}
	public String get_SerialDevice(){
		return c_SerialDevice;
	}
	public void set_SerialDevice(String device){
		c_SerialDevice = device;
		SerialConnection nextConnection = SerialConnection.getSerialConnection(c_SerialDevice);
		if (nextConnection != c_SerialConnection){
			if (this.isTraceLogging()){
				logTrace(">>>> Resetting Serial Connection to "+c_SerialDevice);
			}
			c_SerialConnection = nextConnection;
			c_SerialConnection.addInput(this);
		}
	}
	public AnalysisDataDef def_SerialDevice(AnalysisDataDef theDataDef){
		ArrayList<String> l = new ArrayList<String>();

		for (String defDevice: SerialConnection.DEFAULT_DEVICES){
			if (!SerialConnection.hasAvailableDevice(defDevice)){
				if (!defDevice.equals(c_SerialDevice))
					l.add(defDevice);
			}
		}
		
		for (String con: SerialConnection.getAllSerialDevices()){
			if (!con.equals(c_SerialDevice))
				l.add(con);
		}

		theDataDef.setAllPickValues(l.toArray(new String[l.size()]));
		return theDataDef;
	}
	public String get_AllSerialDevices(){
		return StringUtility.getDelimitedStrings(SerialConnection.getAllSerialDevices(),",");	
	}

	public void _start() {
		super._start();
		c_SerialConnection.addInput(this);
		c_Buffer = new StringBuilder();
		try {
			c_SerialConnection.init();
			c_SerialConnection.open();
		}
		// Had to catch everything here
		catch (Exception e){
			setError(" Unable to start serial connecton e>"+e);
			_disable();
		}
		catch (Throwable e){
			setError("SERIOUS THROWABLE: Unable to start serial connecton e>"+e);
			_disable();
		}

	}
	public void _stop() {


		if (c_SerialConnection != null){
			c_SerialConnection.removeInput(this);
		}

		super._stop();
	}

	public  synchronized void publishIndividualEvent(String data){
		
	
		if (!c_ByLine.booleanValue()){
			publishEvent0(data);
			return;
		}
		
		StringTokenizer st = new StringTokenizer(data,"\n\r", true);
		
		while (st.hasMoreTokens()){
			String next = st.nextToken();
			if (next.equals("\n") || next.equals("\r")){
				if (c_Buffer.length() > 0)
					publishEvent0(c_Buffer.toString());
			}
			else {
				c_Buffer.append(next);
			}
			
		}
	
	}
	private void publishEvent0(String data){

		String label = "Data";
		if (get_NewDataLabel() != null)
			label = get_NewDataLabel();
		
		 publish(new AnalysisEvent(this, new AnalysisData(label,data)));    
		 c_Buffer = new StringBuilder();
	}


}
