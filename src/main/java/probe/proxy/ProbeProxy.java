package probe.proxy;

public class ProbeProxy {
    QUICProbe quicProbe = null;
    MQTTProbe mqttProbe = null;

    public void runQUICProbe(){
        if (quicProbe == null){
            QUICProbe quicProbe = new QUICProbe();
            quicProbe.run();
        }
    }

    public void runMQTTProbe() {
        if (mqttProbe == null){
            MQTTProbe mqttProbe = new MQTTProbe();
            mqttProbe.run();
        }
    }
}
