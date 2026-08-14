package com.gly091020.netMusicListNeoforge.media.netease;

import com.gly091020.netMusicListNeoforge.util.NetMusicBetterLoginUtil;
import com.gly091020.netMusicListNeoforge.util.NetMusicListUtil;
import com.google.gson.Gson;
import com.gly091020.netMusicListNeoforge.NetMusicList;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class NeteaseSearch {
    private static final String PUB_KEY = "010001";
    private static final String MODULUS = "00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7";
    private static final String NONCE = "0CoJUm6Qyw8W8jud";
    private static final String IV = "0102030405060708";

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public static String aesEncrypt(String text, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(
                Cipher.ENCRYPT_MODE,
                new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES"),
                new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8))
        );
        return Base64.getEncoder().encodeToString(
                cipher.doFinal(text.getBytes(StandardCharsets.UTF_8))
        );
    }

    public static String rsaEncrypt(String text) {
        String reversed = new StringBuilder(text).reverse().toString();
        BigInteger bigText = new BigInteger(1, reversed.getBytes(StandardCharsets.UTF_8));
        BigInteger result = bigText.modPow(
                new BigInteger(PUB_KEY, 16),
                new BigInteger(MODULUS, 16)
        );
        return String.format("%0256x", result);
    }

    public static String randomKey(int len) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static Map<String, String> encrypt(Map<String, Object> data) throws Exception {
        String key = randomKey(16);
        String encText = aesEncrypt(aesEncrypt(GSON.toJson(data), NONCE), key);

        Map<String, String> result = new HashMap<>();
        result.put("params", encText);
        result.put("encSecKey", rsaEncrypt(key));
        return result;
    }

    public static String search(String keyword) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("s", keyword);
        data.put("type", 1);
        data.put("offset", 0);
        data.put("limit", 30);
        data.put("total", true);
        data.put("csrf_token", "");

        Map<String, String> enc = encrypt(data);

        String body = "params=" + URLEncoder.encode(enc.get("params"), StandardCharsets.UTF_8)
                + "&encSecKey=" + URLEncoder.encode(enc.get("encSecKey"), StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://music.163.com/weapi/cloudsearch/get/web?csrf_token="))
                .header("User-Agent", "Mozilla/5.0")
                .header("Referer", "https://music.163.com/")
                .header("Origin", "https://music.163.com")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "*/*")
                .header("Cookie", NetMusicListUtil.hasBetterLogin() ?
                        NetMusicBetterLoginUtil.getCookie() :
                        NetMusicList.CONFIG.neteaseCookie)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
}
