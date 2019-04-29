package osp.leobert.android.pandoradoc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.internal.Util;

/**
 * <p><b>Package:</b> osp.leobert.android.pandoradoc </p>
 * <p><b>Project:</b> PandoraDoc </p>
 * <p><b>Classname:</b> Md5 </p>
 * <p><b>Description:</b> TODO </p>
 * Created by leobert on 2019/4/4.
 */
public class Md5 {
    private static final String MD5 = "MD5";

    public static String md5(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(value.getBytes());
            byte[] hash = digest.digest();
            return byte2HexString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String multiMd5(String value, int times) {
        if (times < 1)
            throw new IllegalArgumentException(String.format("multiMd5 %s must > 1", times));
        String result = md5(value);
        for (int i = 0; i < times - 1; i++) {
            result = md5(result);
        }
        return result;
    }

    public static String md5(File file) {
        if (file == null) return null;
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            FileChannel channel = fin.getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(buffer);
            byte[] hash = digest.digest();
            return byte2HexString(hash);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            Util.closeQuietly(fin);
        }
        return null;
    }

    public static String byte2HexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            int val = ((int) bytes[i]) & 0xff;
            if (val < 16) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(val));
        }
        return sb.toString();
    }

    /**
     * 加盐写法
     *
     * @param value
     * @param salt  value.hashCode()
     * @return
     */
    public static String md5(String value, String salt) {
        return md5(value + salt);
    }

}
