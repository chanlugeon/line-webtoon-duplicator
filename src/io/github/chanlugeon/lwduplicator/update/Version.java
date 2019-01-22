package io.github.chanlugeon.lwduplicator.update;

import java.io.IOException;

import org.jsoup.Jsoup;

import com.google.gson.Gson;


public class Version {
    private static final String LATEST_VERSION_URL =
            "https://raw.githubusercontent.com/chanlugeon/line-webtoon-duplicator/master/version.json";

    public static final int CURRENT_VERSION = 1000;

    public static VersionJson readLatestVersionJson() {
        String jsonStr = "{\"version\":" + CURRENT_VERSION + ",\"whats_new\":\"Error.\"";
        try {
            jsonStr = Jsoup.connect(LATEST_VERSION_URL).execute().body();
        } catch (IOException e) {}

        return new Gson().fromJson(jsonStr, VersionJson.class);
    }

    public class VersionJson {
        int version;
        String whats_new;

        public int version() {
            return version;
        }

        public boolean isNewVersion() {
            return version > CURRENT_VERSION;
        }

        public String updateLog() {
            return whats_new;
        }
    }
}
