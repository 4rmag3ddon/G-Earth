package gearth.protocol.connection.proxy.unity;

import gearth.protocol.HConnection;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import gearth.protocol.connection.HProxy;
import gearth.protocol.connection.HProxySetter;
import gearth.protocol.connection.HState;
import gearth.protocol.connection.HStateSetter;
import gearth.protocol.connection.proxy.ProxyProvider;
import gearth.protocol.packethandler.unity.UnityPacketHandler;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

@ServerEndpoint(value = "/packethandler")
public class UnityCommunicator {

    private final HProxySetter proxySetter;
    private final HStateSetter stateSetter;
    private final HConnection hConnection;
    private final ProxyProvider proxyProvider;

    HProxy hProxy = null;

    public UnityCommunicator(HProxySetter proxySetter, HStateSetter stateSetter, HConnection hConnection, ProxyProvider proxyProvider) {
        this.proxySetter = proxySetter;
        this.stateSetter = stateSetter;
        this.hConnection = hConnection;
        this.proxyProvider = proxyProvider;
    }


    @OnOpen
    public void onOpen(Session session) throws IOException {

    }

    @OnMessage
    public void onMessage(byte[] b, Session session) throws IOException {
//        session.getBasicRemote().sendText(message.toUpperCase());
//        session.getBasicRemote().sendBinary(ByteBuffer.wrap(b));
//        System.out.println("received " + new HPacket(b).toString());

        byte[] packet = Arrays.copyOfRange(b, 1, b.length);

        if (hProxy == null && b[0] == 1) {
            HPacket maybe = new HPacket(packet);
            if (maybe.getBytesLength() > 6 && maybe.headerId() == 4000) {
                String hotelVersion = maybe.readString();
                hProxy = new HProxy("", "", -1, -1, "");
                hProxy.verifyProxy(
                        new UnityPacketHandler(hConnection.getExtensionHandler(), hConnection.getTrafficObservables(), session, HMessage.Direction.TOCLIENT),
                        new UnityPacketHandler(hConnection.getExtensionHandler(), hConnection.getTrafficObservables(), session, HMessage.Direction.TOSERVER),
                        hotelVersion
                );
                proxySetter.setProxy(hProxy);
                stateSetter.setState(HState.CONNECTED);
            }
        }


        if (hProxy != null && b[0] == 0) {
            hProxy.getInHandler().act(packet);
        }
        else if (hProxy != null && b[0] == 1) {
            hProxy.getOutHandler().act(packet);
        }
        else {
            proxyProvider.abort();
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        proxyProvider.abort();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        proxyProvider.abort();
    }
}