package de.mixedfx.network.overlay;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class MasterNetworkHandler {
    /**
     * @return Returns a list of all networks I'm connected to (which are active)!
     */
    public static List<OverlayNetwork> getAll() {
        List<OverlayNetwork> networks = new ArrayList<>();
        try {
            networks.addAll(getListOfIPsFromNIs().stream().map(MasterNetworkHandler::get).collect(Collectors.toList()));
        } catch (SocketException e) {
        }
        return networks;
    }

    private static List<InetAddress> getListOfIPsFromNIs() throws SocketException {
        List<InetAddress> addrList = new ArrayList<InetAddress>();
        Enumeration<NetworkInterface> enumNI = NetworkInterface.getNetworkInterfaces();
        while (enumNI.hasMoreElements()) {
            NetworkInterface ifc = enumNI.nextElement();
            if (ifc.isUp()) {
                Enumeration<InetAddress> enumAdds = ifc.getInetAddresses();
                while (enumAdds.hasMoreElements()) {
                    InetAddress addr = enumAdds.nextElement();
                    addrList.add(addr);
                }
            }
        }
        return addrList;
    }

    public static OverlayNetwork get(InetAddress ip) {
        OverlayNetwork ov;
        if (OverlayNetwork.testInRange(ip, TunngleNetwork.class))
            ov = new TunngleNetwork();
        else if (OverlayNetwork.testInRange(ip, HamachiNetwork.class))
            ov = new HamachiNetwork();
        else if (OverlayNetwork.testInRange(ip, LANNetwork.class))
            ov = new LANNetwork();
        else
            ov = new OtherNetwork();
        ov.setIP(ip);
        return ov;
    }

}
