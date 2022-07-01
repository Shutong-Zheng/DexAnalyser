package com.netease.cloudmusic.gloomy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gloomy
 * */
public class DexTranslator {
    List<String> mDexHex;
    private int stringIdsSize;  //字符串大小
    private int stringIdsOff;   //字符串偏移量
    private int typeIdsSize;
    private int typeIdsOff;
    private int fieldIdsSize;
    private int fieldIdsOff;
    private int methodIdsSize;
    private int methodIdsOff;
    private int classDefsSize;
    private int classDefsOff;
    private int dataSize;
    private int dataOff;
    private int protoIdsSize;
    private int protoIdsOff;
    private List<String> stringList;
    private List<String> typeList;
    private List<String> protoList;
    private List<String> fieldList;
    private List<String> methodList;
    private List<String> classList;

    ParseDataUtil parseDataUtil;

    AnnotationUtil annotationUtil;
    DexTranslator(List<String> dexHex){
        mDexHex = dexHex;
        parseDataUtil = ParseDataUtil.getInstance();
        annotationUtil = AnnotationUtil.getInstance();
        stringIdsSize = parseDataUtil.getData(mDexHex, 14 * 4, 4);
        stringIdsOff = parseDataUtil.getData(mDexHex,15 * 4, 4);
        typeIdsSize = parseDataUtil.getData(mDexHex,16 * 4, 4);
        typeIdsOff = parseDataUtil.getData(mDexHex,17 * 4, 4);
        protoIdsSize = parseDataUtil.getData(mDexHex,18 * 4, 4);
        protoIdsOff = parseDataUtil.getData(mDexHex,19 * 4, 4);
        fieldIdsSize = parseDataUtil.getData(mDexHex,20 * 4, 4);
        fieldIdsOff = parseDataUtil.getData(mDexHex,21 * 4, 4);
        methodIdsSize = parseDataUtil.getData(mDexHex,22 * 4, 4);
        methodIdsOff = parseDataUtil.getData(mDexHex,23 * 4, 4);
        classDefsSize = parseDataUtil.getData(mDexHex,24 * 4, 4);
        classDefsOff = parseDataUtil.getData(mDexHex,25 * 4, 4);
        dataSize = parseDataUtil.getData(mDexHex,26 * 4, 4);
        dataOff = parseDataUtil.getData(mDexHex,27 * 4, 4);
        System.out.println("dataOff: " + dataOff);
        //根据偏移量和基址整理一下用到了哪些字符串
        initStringList();
        initTypeList();
        initProtoList();
        initFieldList();
        initMethodList();
        annotationUtil.init(stringList, typeList, protoList, fieldList, methodList);
        initClassList();

    }


    public void initStringList(){
        stringList = new ArrayList<>(stringIdsSize);
        //字符串的起始地址作为key
        int size = stringIdsSize;
        int pointer = stringIdsOff;
        while(size != 0){
             //先拿起始地址
            StringBuilder sb = new StringBuilder();
            int index = parseDataUtil.getData(mDexHex, pointer, 4);
            int len = Integer.parseInt(mDexHex.get(index), 16);     //这个是字符串的大小
            //System.out.println(len);
            for(int i = 0;i < len;i++){
                sb.append((char)Integer.parseInt(mDexHex.get(index + i + 1), 16));
                //System.out.println(mDexHex.get(index + i + 1));
            }
            //System.out.println(sb);
            stringList.add(sb.toString());
            pointer += 4;
            size--;
        }
    }

    public void initTypeList(){
        typeList = new ArrayList<>(typeIdsSize);
        int size = typeIdsSize;     //总共有多少种类型
        int pointer = typeIdsOff;   //指针，指向存放数据地址的地址
        while(size != 0){
            //先拿起始地址
            int index = parseDataUtil.getData(mDexHex, pointer, 4);
            //System.out.println(stringList.get(index));
            typeList.add(stringList.get(index));
            pointer += 4;
            size--;
        }
    }

    public void initProtoList(){
        protoList = new ArrayList<>(protoIdsSize);
        int size = protoIdsSize;
        int pointer = protoIdsOff;
        while(size != 0){
            StringBuilder sb = new StringBuilder();
            //连续12个16进制数
            int shortlyIdx = parseDataUtil.getData(mDexHex, pointer, 4);
            //System.out.println(stringList.get(shortlyIdx));
            int returnTypeIdx = parseDataUtil.getData(mDexHex, pointer + 4, 4);
            //System.out.println(typeList.get(returnTypeIdx));
            sb.append(typeList.get(returnTypeIdx) + "(");
            int dexTypeListIdx = parseDataUtil.getData(mDexHex, pointer + 8, 4);     //参数存储地址
            //如果指向了0x0000表示没有参数
            if(dexTypeListIdx == 0){
                sb.append(")");
                //System.out.println(sb);
                protoList.add(sb.toString());
                pointer += 12;
                size--;
                continue;
            }
            int typeSize = parseDataUtil.getData(mDexHex, dexTypeListIdx, 4);  //参数个数
            dexTypeListIdx += 4;
            while(typeSize != 0){
                int typeIdx = parseDataUtil.getData(mDexHex, dexTypeListIdx, 2);
                sb.append(typeList.get(typeIdx));
                if (typeSize != 1){
                    sb.append(',');
                }
                dexTypeListIdx += 2;
                typeSize--;
            }
            sb.append(")");
            protoList.add(sb.toString());
            //System.out.println(sb);
            pointer += 12;
            size--;
        }
    }

    public void initFieldList(){
        fieldList = new ArrayList<>();
        int size = fieldIdsSize;
        int pointer = fieldIdsOff;
        while(size != 0){
            StringBuilder sb = new StringBuilder();
            int classIdx = parseDataUtil.getData(mDexHex, pointer,2);
            int typeIdx = parseDataUtil.getData(mDexHex, pointer + 2, 2);
            int nameIdx = parseDataUtil.getData(mDexHex, pointer + 4, 4);
            sb.append(typeList.get(typeIdx));
            sb.append(" ");
            sb.append(typeList.get(classIdx));
            sb.append(".");
            sb.append(stringList.get(nameIdx));
            fieldList.add(sb.toString());
            //System.out.println(sb);
            pointer += 8;
            size--;
        }
    }

    public void initMethodList() {
        methodList = new ArrayList<>();
        int size = methodIdsSize;
        int pointer = methodIdsOff;
        while(size != 0){
            StringBuilder sb = new StringBuilder();
            int classIdx = parseDataUtil.getData(mDexHex, pointer, 2);
            int protoIdx = parseDataUtil.getData(mDexHex, pointer + 2, 2);
            int nameIdx = parseDataUtil.getData(mDexHex, pointer + 4, 4);
            sb.append(protoList.get(protoIdx));
            sb.insert(sb.indexOf("("), " " + typeList.get(classIdx) + "." + stringList.get(nameIdx));
            //System.out.println(sb);
            methodList.add(sb.toString());
            pointer += 8;
            size--;
        }
    }

    public void initClassList(){
        classList = new ArrayList<>();
        int size = classDefsSize;
        int pointer = classDefsOff;

        while(size != 0){
            System.out.println("=================================");
            System.out.println(pointer);
            StringBuilder sb = new StringBuilder();
            int classIdx = parseDataUtil.getData(mDexHex, pointer, 4);
            //System.out.println("classIdx : "+classIdx+"");
            String className = typeList.get(classIdx);
            int accessFlags = parseDataUtil.getData(mDexHex, pointer + 4, 4);
            //System.out.println("accessFlags : "+accessFlags+"");
            int superclassIdx = parseDataUtil.getData(mDexHex, pointer + 8, 4);    //父类的类型
            //System.out.println("superclassIdx : "+superclassIdx+"");
            String superClassName = typeList.get(superclassIdx);
            int interfacesOff = parseDataUtil.getData(mDexHex, pointer + 12, 4);
            //System.out.println("interfacesOff : "+interfacesOff+"");
            if(interfacesOff != 0){
                int interfaceSize = parseDataUtil.getData(mDexHex, interfacesOff, 4);
                interfacesOff += 4;
                for (int i = 0; i < interfaceSize; i++) {
                    int interfaceIdx = parseDataUtil.getData(mDexHex, interfacesOff, 2);
                    String interfaceName = typeList.get(interfaceIdx);     //是否实现了接口
                    interfacesOff += 2;
                    System.out.println("接口为：" + interfaceName);
                }
            }
            int sourceFileIdx = parseDataUtil.getData(mDexHex, pointer + 16, 4);   //文件名
            //System.out.println("sourceFileIdx : "+sourceFileIdx+"");
            String sourceFileName = stringList.get(sourceFileIdx);
            int annotationsOff = parseDataUtil.getData(mDexHex, pointer + 20, 4);
            System.out.println("annotationsOff : "+annotationsOff+"");
            if(annotationsOff != 0){
                System.out.println("存在注解！");
                //这里还没有看！！！
                int classAnnotationsOff = parseDataUtil.getData(mDexHex, annotationsOff, 4);       //起始地址 offset to DexAnnotationSetItem
                System.out.println("classAnnotationsOff："+classAnnotationsOff+"");
                if (classAnnotationsOff != 0){
                    int annotationSize = parseDataUtil.getData(mDexHex, classAnnotationsOff, 4);
                    System.out.println("总共有 "+annotationSize+" 个注解");
                    classAnnotationsOff += 4;
                    for (int i = 0;i < annotationSize;i++){
                        System.out.println("注解 "+i+" 信息如下：");
                        int off = parseDataUtil.getData(mDexHex, classAnnotationsOff, 4);      //AnnotationItem的偏移量
                        System.out.println("off: "+off+"");
                        int visibility = parseDataUtil.getData(mDexHex, off, 1);
                        System.out.println("visibility: " + visibility);            //注解的可见性

                        int[]ret = parseDataUtil.parseUleb128(mDexHex, off + 1);
                        System.out.println("注解名字：" + typeList.get(ret[0]));

                        ret = parseDataUtil.parseUleb128(mDexHex, ret[1]);
                        int ss = ret[0];
                        System.out.println("size: " + ss);      //参数个数
                        for(int j = 0;j < ss;j++){
                            ret = parseDataUtil.parseUleb128(mDexHex, ret[1]);
                            System.out.print("name : " + stringList.get(ret[0]) + "  ->  ");
                            int data = parseDataUtil.getData(mDexHex, ret[1], 1);
                            //System.out.println("data: "+data+"");
                            int ff = annotationUtil.parseEncodedValue(mDexHex, data, ret[1] + 1);
                            ret[1] += ff + 1;
                        }
                        classAnnotationsOff += 4;
                    }
                }


                int fieldsSize = parseDataUtil.getData(mDexHex, annotationsOff + 4, 4);
                System.out.println("fieldsSize："+fieldsSize+"");
                int methodsSize = parseDataUtil.getData(mDexHex, annotationsOff + 8, 4);
                System.out.println("methodsSize："+methodsSize+"");
                int parametersSize = parseDataUtil.getData(mDexHex, annotationsOff + 12, 4);
                System.out.println("parametersSize："+parametersSize+"");
                annotationsOff += 16;
//                for(int i = 0;i < fieldsSize;i++){
//                    int fieldIdx = parseDataUtil.getData(mDexHex, annotationsOff, 4);
//                    System.out.println(fieldList.get(fieldIdx));
//                    int fieldAnnotationsOff = parseDataUtil.getData(mDexHex, annotationsOff + 4, 4);
//                    //classAnnotationsOff += 8;
//                    annotationsOff += 8;
//                }
//
//                for(int i = 0;i < methodsSize;i++){
//                    int methodIdx = parseDataUtil.getData(mDexHex, annotationsOff, 4);
//                    System.out.println(fieldList.get(methodIdx));
//                    int methodAnnotationsOff = parseDataUtil.getData(mDexHex, annotationsOff + 4, 4);
//                    annotationsOff += 8;
//                }
//
//                for(int i = 0;i < parametersSize;i++){
//                    int methodIdx = parseDataUtil.getData(mDexHex, annotationsOff, 4);
//                    System.out.println(fieldList.get(methodIdx));
//                    int paramAnnotationsOff = parseDataUtil.getData(mDexHex, annotationsOff + 4, 4);
//                    annotationsOff += 8;
//                }

            }
            int classDataOff = parseDataUtil.getData(mDexHex, pointer + 24, 4);
            //System.out.println(classDataOff);
            //有可能是空的！！！类里面什么都没有
            if (classDataOff != 0){
                //后面的数据结构全部都是uleb128类型的
                int[] ret;
                ret = parseDataUtil.parseUleb128(mDexHex, classDataOff);
                int staticFieldsSize = ret[0];  //静态变量个数
                ret = parseDataUtil.parseUleb128(mDexHex, ret[1]);
                int instanceFieldsSize = ret[0];    //实例变量个数
                ret = parseDataUtil.parseUleb128(mDexHex, ret[1]);
                int directMethodsSize = ret[0];     //直接方法个数
                ret = parseDataUtil.parseUleb128(mDexHex, ret[1]);
                int virtualMethodsSize = ret[0];    //虚方法个数

                System.out.println("静态变量个数 " + staticFieldsSize);
                System.out.println("实例变量个数 " + instanceFieldsSize);
                System.out.println("直接方法个数 " + directMethodsSize);
                System.out.println("虚方法个数 " + virtualMethodsSize);

                //获取静态变量
                for(int i = 0;i < staticFieldsSize;i++){
                    ret = parseDataUtil.parseUleb128(mDexHex, ret[1]);
                    //System.out.println("ret: "+ret[0]+"  "+ret[1]+"");
                    int fieldIdx = ret[0];
                    System.out.println("静态变量" + fieldList.get(fieldIdx));
                    ret = parseDataUtil.parseUleb128(mDexHex, ret[1]);
                    int fieldFlags = ret[0];
                }

                //获取实例变量
                for(int i = 0;i < instanceFieldsSize;i++){
                    ret = parseDataUtil.parseUleb128(mDexHex, ret[1]);
                    //System.out.println("ret: "+ret[0]+"  "+ret[1]+"");
                    int fieldIdx = ret[0];
                    System.out.println("实例变量" + fieldList.get(fieldIdx));
                    ret = parseDataUtil.parseUleb128(mDexHex, ret[1]);
                    int fieldFlags = ret[0];
                }

                //获取直接方法
                for(int i = 0;i < directMethodsSize;i++){
                    ret = parseDataUtil.parseUleb128(mDexHex, ret[1]);
                    int methodIdx = ret[0];
                    ret = parseDataUtil.parseUleb128(mDexHex, ret[1]);
                    int methodFlags = ret[0];
                    System.out.println("method：" + methodList.get(methodIdx));

                    ret = parseDataUtil.parseUleb128(mDexHex, ret[1]);
                    int codeOff = ret[0];

                    /*struct DexCode {
                        u2 registersSize;   //使用寄存器个数
                        u2 insSize;         //参数个数
                        u2 outsSize;        //调用其他方法时使用的寄存器个数
                        u2 triesSize;       //try/catch个数
                        u4 debugInfoOff;    //指向调试信息的偏移
                        u4 insnsSize;       //指令集个数，以2字节为单位
                        u2 insns[1];        //指令集
                    };*/
                    if (codeOff == 0){
                        continue;
                    }

                    int registersSize = parseDataUtil.getData(mDexHex, codeOff, 2);
                    System.out.println("method使用寄存器个数："+registersSize+"");
                    int insSize = parseDataUtil.getData(mDexHex, codeOff + 2, 2);
                    System.out.println("method参数个数："+insSize+"");
                    int outsSize = parseDataUtil.getData(mDexHex, codeOff + 4, 2);
                    System.out.println("method调用其他方法时使用的寄存器个数："+outsSize+"");
                    int triesSize = parseDataUtil.getData(mDexHex, codeOff + 6, 2);
                    System.out.println("try/catch个数："+triesSize+"");
                    int debugInfoOff = parseDataUtil.getData(mDexHex, codeOff + 8, 4);
                    System.out.println("指向调试信息的偏移："+debugInfoOff+"");
                    int insnsSize = parseDataUtil.getData(mDexHex, codeOff + 12, 4);
                    System.out.println("method指令个数："+insnsSize+"");
                    int[] insns = new int[insnsSize];
                    System.out.println("指令集为：");
                    for (int j = 0;j < insnsSize;j++){
                        insns[j] = parseDataUtil.getData(mDexHex, codeOff + 16 + 2 * j, 2);
                        //System.out.println(Integer.toHexString(insns[j]));
                    }
                    /**
                     * 由于后续需要和Android官方文档对照着阅读，所以这里先不写啦
                     * */

                }
                System.out.println("===============virtual method===============");
                //获取虚方法
                for(int i = 0;i < virtualMethodsSize;i++){
                    ret = parseDataUtil.parseUleb128(mDexHex, ret[1]);
                    int methodIdx = ret[0];
                    ret = parseDataUtil.parseUleb128(mDexHex, ret[1]);
                    int methodFlags = ret[0];
                    System.out.println("method：" + methodList.get(methodIdx));

                    ret = parseDataUtil.parseUleb128(mDexHex, ret[1]);
                    int codeOff = ret[0];
                    if (codeOff == 0){
                        continue;
                    }
                    int registersSize = parseDataUtil.getData(mDexHex, codeOff, 2);
                    System.out.println("method使用寄存器个数："+registersSize+"");
                    int insSize = parseDataUtil.getData(mDexHex, codeOff + 2, 2);
                    System.out.println("method参数个数："+insSize+"");
                    int outsSize = parseDataUtil.getData(mDexHex, codeOff + 4, 2);
                    System.out.println("method调用其他方法时使用的寄存器个数："+outsSize+"");
                    int triesSize = parseDataUtil.getData(mDexHex, codeOff + 6, 2);
                    System.out.println("try/catch个数："+triesSize+"");
                    int debugInfoOff = parseDataUtil.getData(mDexHex, codeOff + 8, 4);
                    System.out.println("指向调试信息的偏移："+debugInfoOff+"");
                    int insnsSize = parseDataUtil.getData(mDexHex, codeOff + 12, 4);
                    System.out.println("method指令个数："+insnsSize+"");
                    int[] insns = new int[insnsSize];
                    System.out.println("指令集为：");
                    for (int j = 0;j < insnsSize;j++){
                        insns[j] = parseDataUtil.getData(mDexHex, codeOff + 16 + 2 * j, 2);
                        //System.out.println(Integer.toHexString(insns[j]));
                    }
                }
            }
            int staticValuesOff = parseDataUtil.getData(mDexHex, pointer + 28, 4);
            pointer += 32;
            size--;
        }
    }





    /*
        struct DexClassData{
            DexClassDataHeader	 header;		    指定字段与方法的个数
            DexField* 			staticFields;		静态字段，DexField结构
            DexField*			instanceFields；	实例字段，DexField结构
            DexMethod*			directMethods;		直接方法，DexMethod结构
            DexMethod*			virtualMethods;		虚方法，DexMethod结构
        }
    */




    public void getMethods(){

    }
}
