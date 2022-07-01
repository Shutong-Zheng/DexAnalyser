package com.netease.cloudmusic.gloomy;


import java.util.List;

import static com.netease.cloudmusic.gloomy.AnnotationUtil.AnnotationVisibility.*;
import static com.netease.cloudmusic.gloomy.AnnotationUtil.ValueType.*;

public class AnnotationUtil {
    //工具类
    ParseDataUtil parseDataUtil;
    private static AnnotationUtil instance;

    private List<String> stringList;
    private List<String> typeList;
    private List<String> protoList;
    private List<String> fieldList;
    private List<String> methodList;
    private AnnotationUtil(){
        parseDataUtil = ParseDataUtil.getInstance();
    }

    public static AnnotationUtil getInstance(){
        if (instance == null){
            instance = new AnnotationUtil();
        }
        return instance;
    }

    public void init(List<String> stringList, List<String> typeList,
                     List<String> protoList, List<String> fieldList,
                     List<String> methodList){
        this.stringList = stringList;
        this.typeList = typeList;
        this.protoList = protoList;
        this.fieldList = fieldList;
        this.methodList = methodList;
    }

    public void analyseVisibility(int visibility){
        switch (visibility){
            case VISIBILITY_BUILD:{
                System.out.println("编译时注解");
            }
            case VISIBILITY_RUNTIME:{
                System.out.println("运行时注解");
            }
            case VISIBILITY_SYSTEM:{
                System.out.println("运行时系统注解");
            }
            default:{
                throw new RuntimeException("不存在该可见类型");
            }
        }
    }

    /**
     * @param valueArg 第五位表示类型 高三位 + 1表示value的size
     * */
    public int parseEncodedValue(List<String> mDexHex, int valueArg, int off){
        int type = 0x1f & valueArg;
        //System.out.println("type is " + type);
        int size = 1 + (valueArg >> 5);
        //System.out.println("size is " + size);
        switch (type){
            case VALUE_BYTE -> {
                int data = parseDataUtil.getData(mDexHex, off, size);
                int value;
                if((data & 0x80) == 1){
                    value = -(data & 0x7f);
                }
                value = data & 0x7f;
                System.out.println(value);
                break;
            }
            case VALUE_SHORT -> {
                int data = parseDataUtil.getData(mDexHex, off, size);
                int value;
                if((data & 0x8000) == 1){
                    value = -(data & 0x7fff);
                }
                value = data & 0x7fff;
                System.out.println(value);
                break;
            }
            case VALUE_CHAR -> {
                int data = parseDataUtil.getData(mDexHex, off, size);
                System.out.println(data);
                break;
            }
            case VALUE_INT -> {
                int data = parseDataUtil.getData(mDexHex, off, size);
                int value;
                if((data & 0x80000000) == 1){
                    value = -(data & 0x7fffffff);
                }
                value = data & 0x7fffffff;
                System.out.println(value);
                break;
            }
            case VALUE_LONG -> {
//                long data = parseDataUtil.getData(mDexHex, off, size);
//                long value;
//                if((data & 0x800000000000000000) == 1){
//                    value = -(data & 0x7fffffff);
//                }
//                value = data & 0x7fffffff;
//                System.out.println(value);
//                break;
                System.out.println("long");
                break;
            }
            case VALUE_FLOAT -> {
                System.out.println("float");
                break;
            }
            case VALUE_DOUBLE -> {
                System.out.println("double");
                break;
            }
            case VALUE_METHOD_TYPE -> {
                int data = parseDataUtil.getData(mDexHex, off, size);
                System.out.println(protoList.get(data));
                break;
            }
//            case VALUE_METHOD_HANDLE -> {
//
//            }
            case VALUE_STRING -> {
                int data = parseDataUtil.getData(mDexHex, off, size);
                System.out.println(stringList.get(data));
                break;
            }
            case VALUE_TYPE -> {
                int data = parseDataUtil.getData(mDexHex, off, size);
                System.out.println(typeList.get(data));
                break;
            }
            case VALUE_FIELD,VALUE_ENUM -> {
                int data = parseDataUtil.getData(mDexHex, off, size);
                System.out.println(fieldList.get(data));
                break;
            }
            case VALUE_METHOD -> {
                int data = parseDataUtil.getData(mDexHex, off, size);
                System.out.println(methodList.get(data));
                break;
            }
            case VALUE_ARRAY -> {
                System.out.println("array");
                break;
            }
            case VALUE_ANNOTATION -> {
                System.out.println("annotation");
                break;
            }
            case VALUE_NULL -> {
                System.out.println("null");
                break;
            }
            case VALUE_BOOLEAN -> {
                System.out.println(size==1);
                break;
            }
            default -> {
                throw new RuntimeException("未找到传入的value类型");
            }
        }
        return size;
    }


    class AnnotationVisibility{
        public final static int VISIBILITY_BUILD = 0x00;
        public final static int VISIBILITY_RUNTIME = 0x01;
        public final static int VISIBILITY_SYSTEM = 0x02;
    }

    class ValueType{
        public final static int VALUE_BYTE = 0x00;
        public final static int VALUE_SHORT = 0x02;
        public final static int VALUE_CHAR = 0x03;
        public final static int VALUE_INT = 0x04;
        public final static int VALUE_LONG = 0x06;
        public final static int VALUE_FLOAT = 0x10;
        public final static int VALUE_DOUBLE = 0x11;
        public final static int VALUE_METHOD_TYPE = 0x15;
        public final static int VALUE_METHOD_HANDLE = 0x16;
        public final static int VALUE_STRING = 0x17;
        public final static int VALUE_TYPE = 0x18;
        public final static int VALUE_FIELD = 0x19;
        public final static int VALUE_METHOD = 0x1a;
        public final static int VALUE_ENUM = 0x1b;
        public final static int VALUE_ARRAY = 0x1c;
        public final static int VALUE_ANNOTATION = 0x1d;
        public final static int VALUE_NULL = 0x1e;
        public final static int VALUE_BOOLEAN = 0x1f;
    }
}
