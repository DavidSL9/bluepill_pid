/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base;

import static java.lang.Math.*;

/**
 *
 * @author David
 */
public class PD {

    private final double SET_POINT = 50;
    final private ArduinoInterface arduinoInterface;

    private double ck;
    private double cp;

    private double wn;
    private double zeta;

    private double pt;
    private double os;

    private double kp;
    private double td;
    
    private double[] positionResponse;
    private double[] uResponse;
    
    // desired parameters
    private double dwn;
    private double dzeta;
    private double dpt;
    private double dos;

    public PD(ArduinoInterface arduinoInterface) {
        this.arduinoInterface = arduinoInterface;
    }

    public void runIdentification() {
        this.kp = 12.5;
        this.td = 0;
        run(SET_POINT);
        identify();
    }

    private void identify() {
        ck = pow(wn, 2) / kp;
        cp = 2 * zeta * wn;
    }

    public void performanceSintonization(double pt, double os) {
        this.dpt = pt;
        this.dos = os;
        
        dzeta = getRelativeDamping(dos);
        dwn = getNaturalFrequency(dpt, dzeta);
        
        kp = pow(dwn, 2) / ck;
        td = (2 * dzeta * dwn - cp) / kp / ck;
    }

    public void dampFreqSintonization(double zeta, double wn) {
        dzeta = zeta;
        dwn = wn;
        
        double det = sqrt(1 - pow(dzeta, 2));
        dos = pow(E, -dzeta * PI / det);
        dpt = PI / wn / det;

        kp = pow(dwn, 2) / ck;
        td = (2 * dzeta * dwn - cp) / kp / ck;
    }
    
    private double getRelativeDamping(double os) {
        double slnos = pow(log(os), 2);
        return sqrt(slnos / (slnos + pow(PI, 2)));
    }
    
    private double getNaturalFrequency(double pt, double zeta) {
        double det = sqrt(1 - pow(zeta, 2));
        return PI / pt / det;
    }

    public void runGains(double kp, double td) {
        this.kp = kp;
        this.td = td;
        run(SET_POINT);
    }
    
    public void run(double reference) {
        arduinoInterface.setKp(kp);
        arduinoInterface.setTd(td);
        arduinoInterface.setRef(0);
        try {
            Thread.sleep(1500);
            arduinoInterface.setRef(reference);
            Thread.sleep(1500);
            
            String[] response = arduinoInterface.sr();
            DoubleWrapper PT = new DoubleWrapper();
            DoubleWrapper PV = new DoubleWrapper();
            positionResponse = getDoubleArray(response, PT, PV);
            pt = PT.v / 1000;
            os = PV.v / reference - 1;
            zeta = getRelativeDamping(os);
            wn = getNaturalFrequency(pt, zeta);
            
            response = arduinoInterface.su();
            uResponse = getDoubleArray(response, null, null);
            for (int i = 0; i < uResponse.length; i++) {
                uResponse[i] = uResponse[i] * 2.5 / 1000;
            }
        } catch (InterruptedException | NumberFormatException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    private double[] getDoubleArray(String[] results, DoubleWrapper PT, DoubleWrapper PV) { // PT and PV are PeakTime and PeakValue
        double[] dresults = new double[results.length - 1];
        double pv = 0;
        double pt = 0;
        for(int i = 1; i < results.length; i++) {
            if (i == 0) {
                results[i] = results[i].replace(":", "");
            }
            double v = Double.parseDouble(results[i]);
            if (v > pv) {
                pv = v;
                pt = i - 1;
            }
            dresults[i - 1] = v;
        }
        if (PT != null) PT.v = pt;
        if (PV != null) PV.v = pv;
        return dresults;
    }

    public void closeArduinoInterface() {
        if (arduinoInterface != null) {
            arduinoInterface.close();
        }
    }

    public double getCk() {
        return ck;
    }

    public double getCp() {
        return cp;
    }

    public double getWn() {
        return wn;
    }

    public double getZeta() {
        return zeta;
    }

    public double getPt() {
        return pt;
    }

    public double getOs() {
        return os;
    }

    public double getKp() {
        return kp;
    }

    public double getTd() {
        return td;
    }

    public double[] getPositionResponse() {
        return positionResponse;
    }

    public double[] getuResponse() {
        return uResponse;
    }

    public double getDwn() {
        return dwn;
    }

    public double getDzeta() {
        return dzeta;
    }

    public double getDpt() {
        return dpt;
    }

    public double getDos() {
        return dos;
    }
    
    
    class DoubleWrapper {
        public double v;
    }
}
