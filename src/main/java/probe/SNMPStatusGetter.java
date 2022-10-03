package probe;

import java.io.IOException;

import command.Command;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SNMPStatusGetter implements StatusGetter {

    public static final int DEFAULT_VERSION = SnmpConstants.version2c;
    public static final String DEFAULT_PROTOCOL = "udp";
    public static final int DEFAULT_PORT = 161;
    public static final long DEFAULT_TIMEOUT = 3 * 1000L;
    public static final int DEFAULT_RETRY = 3;

    public StatusType getStatus(String augUrl, int minVal, int maxVal){
        Command command = new Command("CURCONFIG " + augUrl);
        try{
            String result = getValueFromOid(command.getMatcher().group("host"),
                    command.getMatcher().group("username"),
                    command.getMatcher().group("path").substring(1));
            return checkBounds(Integer.parseInt(result), minVal, maxVal);
        }catch (Exception e) {
            //Will return DOWN when connection time out
            return StatusType.DOWN;
        }
    }

    private StatusType checkBounds(int currentVal, int minVal, int maxVal) {
        if (currentVal > maxVal || currentVal < minVal) return StatusType.ALARM;
        else return StatusType.OK;
    }

    private CommunityTarget createDefault(String ip, String community) {
        Address address = GenericAddress.parse(DEFAULT_PROTOCOL + ":" + ip
                + "/" + DEFAULT_PORT);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(address);
        target.setVersion(DEFAULT_VERSION);
        target.setTimeout(DEFAULT_TIMEOUT); // milliseconds
        target.setRetries(DEFAULT_RETRY);
        return target;
    }

    private String getValueFromOid(String ip, String community, String oid) {
        CommunityTarget target = createDefault(ip, community);
        Snmp snmp = null;
        try {
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));

            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.listen();
            pdu.setType(PDU.GET);
            ResponseEvent respEvent = snmp.send(pdu, target);
            PDU response = respEvent.getResponse();

            if (response != null) {
                VariableBinding vb = response.get(0);
                return vb.getVariable().toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (IOException ex1) {
                    snmp = null; //TODO catch exception
                }
            }
        }
        return "";
    }
}
