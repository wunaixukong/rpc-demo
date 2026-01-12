package tech.insight.rpc.provider;

import tech.insight.rpc.api.Add;

public class AddImpl implements Add {
    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
