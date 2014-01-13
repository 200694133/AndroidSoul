package com.hyn.app;

/**
 * Created with IntelliJ IDEA.
 * User: yananh
 * Date: 13-11-20
 * Time: 上午9:15
 * To change this template use File | Settings | File Templates.
 */
public class FunctionUtil {
    public static boolean locationEquals(double d1, double d2){
        if(Math.abs(d1-d2) <= 0.00000000001){
            return true;
        }
        return false;
    }

}
