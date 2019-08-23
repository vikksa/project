package com.vikson.projects;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class LevelSquarification {

    @ParameterizedTest
    @ValueSource(ints = {1,3,5,6,7,9,10,11,12,13,14,15})
    public void findNearesPowerOfTwo(int a) {
        int power = a == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(a - 1);
        power = (int)Math.pow(2, power);
        if(a < 2)
            assertEquals(1, power);
        else if(a < 4)
            assertEquals(4, power);
        else if(a < 8)
            assertEquals(8, power);
        else if(a < 16)
            assertEquals(16, power);
        else
            fail(String.format("Unknown value %d!", a));
    }

}
