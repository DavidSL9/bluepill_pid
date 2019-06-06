/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base;

import java.util.ArrayList;

/**
 *
 * @author David
 */
public class ArduinoInterface {
    
    private final SPort sPort;
    
    public ArduinoInterface(SPort sPort) {
        this.sPort = sPort;
    }
    
    public void setRef(double ref) {
        sPort.printLine("REF " + ref);
    }
    
    public String[] sr() {
        return callSend("SR");
    }
    
    public String[] su() {
        return callSend("SU");
    }
    
    public String[] callSend(String command) {
        int numberOfResultLines = 750;
        final ArrayList<String> results = new ArrayList<>();
        sPort.setListener((String line) -> {
            // System.out.println(line);
            synchronized (results) {
                results.add(line);
            }
        });
        sPort.printLine(command);
        int i;
        do {
            synchronized (results) {
                i = results.size();
            }
        } while (i < numberOfResultLines);
        return results.toArray(new String[results.size()]);
    }
    
    public void setKp(double kp) {
        sPort.printLine("KP " + kp);
    }
    
    public void setTd(double td) {
        sPort.printLine("TD " + td);
    }
    
    public void close() {
        sPort.close();
    }
}
