package io.github.chanlugeon.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class Util {
    public static final int WAIT_TIME = 5000;
    public static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36";

    public Util() {
        throw new AssertionError();
    }

    public static int contentLength(Map<String, String> data) {
        int len = 0;
        try {
            final String enc = URLEncoder.encode(data.toString(), "UTF-8").replaceAll("%0A", "%0D%0A");
            len += enc.length() - 4 - data.size() * 5 + 1;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return len;
    }
}
