package com.example.chatappprueba3.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Encrypter {

    public static String encryptMessage(int key, String text){
        char[] messageToEncrypt = text.toCharArray();

        char[] charMessageEncrypted = new char[messageToEncrypt.length];
        for (int i = 0; i < messageToEncrypt.length; i++){
            charMessageEncrypted[i] = (messageToEncrypt[i] += key);
        }

        String messageEncrypted = new String(charMessageEncrypted);

        return messageEncrypted;
    }

    public static String decryptMessage(int key, String text){

        char[] messageToDecrypt = text.toCharArray();

        char[] charMessageDecrypted = new char[messageToDecrypt.length];
        for (int i = 0; i < messageToDecrypt.length; i++){
            charMessageDecrypted[i] = (messageToDecrypt[i] -= key);
        }

        String messageDecrypted = new String(charMessageDecrypted);

        return messageDecrypted;
    }
}
