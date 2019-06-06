/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.TooManyListenersException;

/**
 *
 * @author David
 */
public class SPort implements SerialPortEventListener {

    private final int TIME_OUT = 1;

    private SerialPort serialPort;
    private BufferedReader input;
    private PrintWriter output;
    private InputListener listener;
    
    public synchronized static SPort openPort(String portName, int dataRate) {
        SPort sPort = new SPort(portName, dataRate);
        sPort.setDataListener();
        return sPort;
    }

    private SPort(String portName, int dataRate) {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            if (currPortId.getName().equals(portName)) {
                portId = currPortId;
                break;
            }
        }
        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }

        try {
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                    TIME_OUT);
            serialPort.setSerialPortParams(dataRate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // open the streams
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = new PrintWriter(serialPort.getOutputStream());
        } catch (PortInUseException | UnsupportedCommOperationException | IOException e) {
            System.err.println(e.toString());
        }
    }
    
    private synchronized void setDataListener() {
        try {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (TooManyListenersException e) {
            System.err.println(e.getMessage());
        }
    }

    public synchronized void setListener(InputListener listener) {
        this.listener = listener;
    }

    public synchronized void close() {
        if (serialPort != null) {
            this.listener = null;
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    private StringBuilder sb = new StringBuilder();
    @Override
    public void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                while (input.ready()) {
                    int c = input.read();
                    if (c != '\r' && c != '\n') {
                        sb.append(String.format("%c", c));
                    } else if (c == '\n') {
                        if (listener != null) {
                            listener.lineRead(sb.toString());
                        }
                        sb.setLength(0);
                    } 
                }
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
    }

    public synchronized void printLine(String line) {
        output.println(line);
        output.flush();
    }

    public interface InputListener {
        void lineRead(String line);
    }
}
