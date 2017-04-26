/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myThingStudio;

/**
 *
 * @author alessiotirabasso
 */
 
import static java.lang.Thread.MAX_PRIORITY;
import static java.lang.Thread.sleep;
import java.math.BigInteger;
import java.util.Random;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.hyperic.sigar.Mem;



public class Cpu {
    
    private static Sigar sigar;
    
    public Cpu(Sigar s) throws SigarException {
        sigar = s;
        //System.out.println(cpuInfo());
    }
      public static double  ramInfo() throws SigarException {
        Mem memoria = sigar.getMem();
        System.out.println("memoria RAM: "+ memoria.getRam() + "MB");
        double x=memoria.getUsedPercent();
        System.out.println("percentuale mem used: "+ memoria.getUsedPercent());
        //System.out.println("percentuale memoria libera: "+memoria.getFreePercent());
        return x;
    }

    public static double cpuInfo() throws SigarException {
         CpuPerc cpu = sigar.getCpuPerc();
        double system = cpu.getSys();
        double user = cpu.getUser();
        double idle = cpu.getIdle();
        int perc=(int)(system+user);
        System.out.println("idle: " +CpuPerc.format(idle) +", system: "+CpuPerc.format(system)+ ", user: "+CpuPerc.format(user));
        
       return  idle;
    }
    
    public static void main(String[]args) throws SigarException, InterruptedException, MqttException {
        
        String topic        = "/alessio/ram";
        String topic2       ="/alessio/cpu";
        String content[]=new String[2];
        int qos             = 2;
        String broker       = "tcp://mqtt.thingstud.io:1883";
        String clientId     = "JavaSample";
        MemoryPersistence persistence = new MemoryPersistence();
        new Cpu (new Sigar());
        
        
        try{
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName("guest");
            connOpts.setPassword("guest".toCharArray());
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            
            
//-------------------------CONNESSO-----------------------------------------------------
            
            new Thread() {
                public void run() {
                    while (true) {
                        BigInteger.probablePrime(MAX_PRIORITY, new Random());
                    }
                }
            ;
            }.start();
        while (true) {
            
                double[] d;
                d = new double[2];
                d[0]=ramInfo();
                d[1]=cpuInfo();
                d[1]=d[1]*100;
                double res=100-d[1];
                String s = String.valueOf(d[0]);
                content[0]=s.substring(0,5);
                String t=String.valueOf(res);
                content[1]=t.substring(0,4);
                System.out.println("Publishing message: RAM = " +content[0]+
                        "%, "+"CPU = "+ content[1]+"%");
                MqttMessage messageRam = new MqttMessage(content[0].getBytes());
                MqttMessage messageCpu=new MqttMessage(content[1].getBytes());
                messageRam.setQos(qos);
                messageCpu.setQos(qos);
                sampleClient.publish(topic, messageRam);
                sampleClient.publish(topic2,messageCpu);
                System.out.println("Message published");
                System.out.println();
                Thread.sleep(1000);
            }
            
            /*sampleClient.disconnect();
            System.out.println("Disconnected");
            System.exit(0);*/

        }catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
}
