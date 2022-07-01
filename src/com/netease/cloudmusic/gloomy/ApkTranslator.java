package com.netease.cloudmusic.gloomy;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @author gloomy
 * 用于解析apk文件
 * */

public class ApkTranslator {
    private File mApk;      //Apk文件

    ApkTranslator(File apk){
        this.mApk = apk;
    }

    public Translator analyseApk(){
        //根据zip文件的解析规则解析apk
        Map<String, InputStream> cache = new HashMap<>();
        try {
            ZipInputStream in = new ZipInputStream(new FileInputStream(mApk));
            ZipEntry zipFile;
            while ((zipFile=in.getNextEntry())!=null) {
                //如果是目录，不处理
                if (zipFile.isDirectory()){
                    System.out.println("当前路径为目录："+zipFile.getName());
                }
                String fileName = zipFile.getName();
                cache.put(fileName, new ZipFile(mApk).getInputStream(zipFile));
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Translator(cache);
    }

    class Translator{
        private Map<String, InputStream> mCache;    // 存储了各个文件
        Translator(Map<String, InputStream> cache){
            this.mCache = cache;
        }

        InputStream getFile(String fileName){
            if(!mCache.containsKey(fileName)){
                System.out.println("没有找到该文件");
                return null;
            }
            return mCache.get(fileName);
        }

        public List<String> getCacheFileList(){
            List<String> fileList = new ArrayList<>(mCache.size());
            for(String fileName : mCache.keySet()){
                fileList.add(fileName);
            }
            return fileList;
        }

        public void printHexCode(String fileName){
            if(!mCache.containsKey(fileName)){
                System.out.println("没有找到该文件");
                return;
            }

            InputStream file = getFile("classes.dex");
            byte[] buf = new byte[1];
            int hex;
            int count = 0;
            while(true){
                try {
                    if ((hex = file.read()) == -1) break;
                    System.out.print(Integer.toHexString(hex) + " ");
                    if ((count + 1) % 16 == 0 ){
                        System.out.println();
                    }
                    count++;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public List<String> getHexCode(String fileName){
            List<String> list = new ArrayList<>();
            if(!mCache.containsKey(fileName)){
                System.out.println("没有找到该文件");
                return null;
            }

            InputStream file = getFile("classes.dex");
            byte[] buf = new byte[1];
            int hex;
            while(true){
                try {
                    if ((hex = file.read()) == -1) break;
                    list.add(Integer.toHexString(hex));
                    //System.out.print(Integer.toHexString(hex) + " ");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return list;
        }
    }
}
