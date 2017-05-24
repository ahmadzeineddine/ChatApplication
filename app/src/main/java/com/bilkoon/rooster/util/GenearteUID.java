package com.bilkoon.rooster.util;

import java.util.UUID;

/**
 * Created by Ahmed on 4/11/2017.
 */

public class GenearteUID {
    public static String getFrom(){
        return UUID.randomUUID().toString();
    }
}
