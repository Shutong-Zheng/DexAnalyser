package com.netease.cloudmusic.gloomy;

import java.util.List;

public class ParseDataUtil {
    private static ParseDataUtil instance;

    private ParseDataUtil(){}

    public static ParseDataUtil getInstance(){
        if (instance == null){
            instance = new ParseDataUtil();
        }
        return instance;
    }

    public int getData(List<String> mDexHex, int offset, int length){
        StringBuilder sb = new StringBuilder();
        for(int i = offset + length - 1;i >= offset;i--){
            String str = mDexHex.get(i);
            if (str.length() == 1){
                str = "0" + str;
            }
            sb.append(str);
        }
        int ret = Integer.parseInt(sb.toString(), 16);
        return ret;
    }

    /**
     * @param offset 偏移量
     * @return ret[0]当前读取到的数据 ret[1]表示下一个数据开始的偏移地址
     **/
    public int[] parseUleb128(List<String> mDexHex, int offset){
        //以上结构都是根据uleb128格式进行存储的
        int[] ret = new int[2];
        int count = 0;  //用于表示总共几位
        StringBuilder sb = new StringBuilder();
        while (true){
            if((Integer.parseInt(mDexHex.get(offset + count), 16) & 0x80) == 0 || count == 5){
                //表示结束，最高位不是0
                break;
            } else {
                count++;
            }
        }
        for (int i = count; i > -1; i--) {
            //开始计算实际上这些数据表示多少，高位不计入计算
            int temp = Integer.parseInt(mDexHex.get(offset + i), 16) & 0x7f;
            String s = Integer.toBinaryString(temp);
            while(s.length() < 7){
                s = "0" + s;        //补到7位
            }
            sb.append(s);  //转换为2进制存储
        }
        int res = Integer.parseInt(sb.toString(), 2);
        ret[0] = res;
        ret[1] = offset + count + 1;    //下一个起始位置
        return ret;
    }
}
