package com.netease.cloudmusic.gloomy;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * for test
 * */
public class Client {
    private static int[] parseUleb128(int offset){
        //以上结构都是根据uleb128格式进行存储的
        int[] ret = new int[2];
        String[] mDexHex = {"01", "01", "03", "01"};
        int count = 0;  //用于表示总共几位
        StringBuilder sb = new StringBuilder();
        while (true){
            if((Integer.parseInt(mDexHex[offset + count], 16) & 0x80) == 0){
                //表示结束，最高位不是0
                break;
            } else {
                count++;
            }
        }
        for (int i = count; i > -1; i--) {
            //开始计算实际上这些数据表示多少，高位不计入计算
            int temp = Integer.parseInt(mDexHex[offset + i], 16) & 0x7f;
            String s = Integer.toBinaryString(temp);
            while(s.length() < 7){
                s = "0" + s;        //补到7位
            }
            sb.append(s);  //转换为2进制存储
        }
        System.out.println(sb);
        int res = Integer.parseInt(sb.toString(), 2);
        ret[0] = res;
        ret[1] = offset + count + 1;    //下一个起始位置
        return ret;
    }



    public static void main(String[] args) {
        File apk = new File("TestActivity.apk");
        ApkTranslator t = new ApkTranslator(apk);
        ApkTranslator.Translator translator = t.analyseApk();
        //translator.printHexCode("classes.dex");
        List<String> dexHex = translator.getHexCode("classes.dex");
        DexTranslator dexTranslator = new DexTranslator(dexHex);
        //dexTranslator.initClassList();
    }
}
