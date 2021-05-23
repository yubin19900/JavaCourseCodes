package io.github.kimmking.gateway;


import io.github.kimmking.gateway.server.NettyHttpServer;

public class NettyServerApplication {

    public static void main(String[] args) {
        NettyHttpServer server = new NettyHttpServer(8080);
        try {
            server.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
