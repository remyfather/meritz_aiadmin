package smart.ai.admin.util;

// TODO: V2 개발 완료 후 기존 AES 유틸리티 복원 필요
// 기존 AES 암호화 유틸리티 - V2 개발 중 임시 주석 처리

/*
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.net.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AESUtil {

    private static final Logger log         = LoggerFactory.getLogger(AESUtil.class);
    private static final String AlgorAES 	= "AES";
	private static final String Algorithm 	= AlgorAES + "/CBC/PKCS5Padding";
	private static final String encCharset 	= "UTF-8";
    private static final String cryptKey    = "a5c2e1f8152cdc1ca5c2e1f8152cdc1c";

    public static String getCryptStr(String inStr, String cryptDivCd, String recId, String stmt) {
            String cryptResult = "";
            try {
                if(!inStr.isEmpty()) {
                    if("E".equals(cryptDivCd)){
                        cryptResult = encryptStr(inStr, cryptKey);
                    } else if("D".equals(cryptDivCd)) {		// decode
                        cryptResult = decryptStr(inStr, cryptKey);
                    }
                }
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("DB EXCEPTION : " + e.getMessage());
                    log.error("recId : " + recId);
                }
            }
            return cryptResult;
       }	

    public static String decryptStr(String inStr, String strKey) throws Exception{
		byte[] base64Decoded = Base64.decodeBase64(inStr.getBytes(encCharset));
		byte[] initVector = Arrays.copyOfRange(base64Decoded, 0, 16);
		byte[] msgByte = Arrays.copyOfRange(base64Decoded, 16, base64Decoded.length);
		SecretKeySpec key = new SecretKeySpec(strKey.getBytes(encCharset), AlgorAES);
		IvParameterSpec ivSpec = new IvParameterSpec(initVector);
	
		Cipher cip = Cipher.getInstance(Algorithm);
		cip.init(Cipher.DECRYPT_MODE, key, ivSpec);
	
		byte[] aesDecode = cip.doFinal(msgByte);
	
		String originStr = new String(aesDecode, encCharset);
	
		return originStr;
	}	
	
    public static String encryptStr(String inStr, String strKey) throws Exception{
		byte initVector[] = new byte[16];
		(new Random()).nextBytes(initVector);
	
		IvParameterSpec ivSpec = new IvParameterSpec(initVector);
	
		SecretKeySpec key = new SecretKeySpec(strKey.getBytes(encCharset), AlgorAES);
	
		Cipher cip = Cipher.getInstance(Algorithm);
		cip.init(Cipher.ENCRYPT_MODE, key, ivSpec);
	
		byte[] cipherbytes = cip.doFinal(inStr.getBytes());
	
		byte[] msgBytes = new byte[initVector.length + cipherbytes.length];
	
		System.arraycopy(initVector, 0, msgBytes, 0, 16);
		System.arraycopy(cipherbytes, 0, msgBytes, 16, cipherbytes.length);
	
		String afterCiphered = new String(Base64.encodeBase64(msgBytes));
	
		return afterCiphered;
	}	
	
}
*/