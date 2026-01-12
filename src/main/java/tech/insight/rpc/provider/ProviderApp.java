package tech.insight.rpc.provider;

public class ProviderApp {
    public static void main(String[] args) {
        ProviderServer server = new ProviderServer(8888);
        server.start();
    }
}
