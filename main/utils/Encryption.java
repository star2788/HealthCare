package com.example.main.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encryption {
    public static String SHA256(String str){
        String SHA = "";
        try {
            MessageDigest sh = MessageDigest.getInstance("SHA-256"); // 메세지 다이제스트 : SHA 을 사용하는 암호화 방식 설정
            sh.update(str.getBytes());
            System.out.println("strByte : "+str.getBytes());
            byte byteData[] = sh.digest(); //암호화 실행
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i< byteData.length ; i++){
                sb.append(Integer.toString((byteData[i]&0xff)+0x100,16).substring(1));
                SHA = sb.toString();
                int tmp1 = (byteData[i] & 0xff);
                int tmp2 = ((byteData[i]&0xff) + 0x100);

                System.out.println(byteData +" : "+ tmp1 +" : "+ tmp2
                        +" : "+Integer.toString((byteData[i]&0xff)+0x100, 16)
                        +" : "+(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1)));

            }

        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            SHA = null;
        }
        return SHA;
    }
}
