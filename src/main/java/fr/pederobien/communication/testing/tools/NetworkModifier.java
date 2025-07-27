package fr.pederobien.communication.testing.tools;

import java.util.Random;
import java.util.function.Function;

public class NetworkModifier implements Function<byte[], byte[]> {
    private int min, max;
    private int counter;

    public NetworkModifier(int min, int max) {
        this.min = min;
        this.max = max;
        counter = 0;
    }

    @Override
    public byte[] apply(byte[] data) {
        if (min <= counter && counter < max) {
            int random = new Random().nextInt(data.length);
            data[random] += 1;
        }

        counter++;
        return data;
    }
}
