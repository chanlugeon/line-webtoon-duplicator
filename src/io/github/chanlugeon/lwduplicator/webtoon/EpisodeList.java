package io.github.chanlugeon.lwduplicator.webtoon;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.chanlugeon.util.Util;


public class EpisodeList {
    private static final String LANGUAGE_TAG = "en";

    private static final String DOMAIN = "www.webtoons.com";
    private static final String ORIGIN = "https://" + DOMAIN;
    public static final String ORIGIN_EN = ORIGIN + "/" + LANGUAGE_TAG;
    public static final String EPLIST = ORIGIN + "/episodeList";

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^https://www[.]webtoons[.]com/en/(?<genre>\\w+)/(?<codename>[\\w-]+)/");

    private DayOfWeek dayOfWeek;
    private final String title;
    private final int titleNo;
    private final String thumbnail;
    private final String imageUrl;
    private final float rating;
    private final String summary;
    private final String[] artists;
    private final String genre;
    private final String codeName;
    private final int lastEpisodeNo;

    private final String referer;
    private final String path;

    public EpisodeList(int titleNo) throws IOException {
        referer = EPLIST + "?titleNo=" + titleNo;
        final Document doc = Jsoup.connect(referer)
                .timeout(Util.WAIT_TIME)
                .userAgent(Util.USER_AGENT)
                .method(Method.GET)
                .execute()
                .parse();
        title = doc.selectFirst("meta[property=og:title]").attr("content");
        thumbnail = doc.selectFirst("meta[property=og:image]").attr("content");
        imageUrl = doc.selectFirst("meta[property=og:image]").attr("content");
        summary = doc.selectFirst("meta[property=og:description]").attr("content");
        artists = doc.selectFirst("meta[property=com-linewebtoon:webtoon:author]").attr("content").split(" / ");
        lastEpisodeNo = Integer.parseInt(doc.selectFirst("#_listUl > li").attr("data-episode-no"));

        String dayInfo = doc.selectFirst("#_asideDetail > p.day_info").textNodes().get(0).toString();
        int index = dayInfo.lastIndexOf("EVERY ") == 0 ? 6 : 0;
        dayOfWeek = DayOfWeek.valueOf(dayInfo.substring(index));

        rating = Float.parseFloat(doc.selectFirst("em#_starScoreAverage").text());
        System.out.println(rating);

        final String currentUrl = doc.selectFirst("meta[property=og:url]").attr("content");
        final Matcher urlMatcher = URL_PATTERN.matcher(currentUrl);
        urlMatcher.find();
        genre = urlMatcher.group("genre");
        codeName = urlMatcher.group("codename");

        this.titleNo = titleNo;
        path = new File("").getAbsolutePath() + "/" + title;
    }

    public static EpisodeList newInstance(int titleNo) throws IOException {
        return new EpisodeList(titleNo);
    }

    public String episodeUrl(int episodeNo) {
        return ORIGIN_EN + "/" + genre + "/" + codeName + "/recent-episode/viewer?title_no=" +
                titleNo + "&episode_no=" + episodeNo;
    }

    public Optional<Episode> newEpisode(int episodeNo) {
        try {
            return Optional.of(new Episode(this, episodeNo));
        } catch (IOException e) {
            // This webtoon has been deleted or is currently unavailable.
            return Optional.empty();
        }
    }

    public EpisodeList downloadThumbnail() throws IOException {
        new File(path).mkdirs();

        // Download thumbnail.
        String thumbPath = path + "/thumbnail.jpg";
        File f = new File(thumbPath);

        if (!f.exists()) {
            InputStream in = Jsoup.connect(thumbnail)
                    .timeout(Util.WAIT_TIME)
                    .header("DNT", "1")
                    .referrer(referer)
                    .userAgent(Util.USER_AGENT)
                    .ignoreContentType(true)
                    .method(Method.GET)
                    .execute()
                    .bodyStream();

            OutputStream out = new BufferedOutputStream(new FileOutputStream(thumbPath));
            for (int b; (b = in.read()) != -1; ) {
                out.write(b);
            }

            out.close();
            in.close();
        }
        return this;
    }

    public EpisodeList saveWebtoonInfo() throws IOException {
        WebtoonInfo info = new WebtoonInfo()
                .addArtists(artists)
                .setCodeName(codeName)
                .setDayOfWeek(dayOfWeek)
                .setGenre(genre)
                .setRating(rating)
                .setSummary(summary)
                .setTitle(title)
                .setTitleNo(titleNo);

        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        String json = gson.toJson(info);
        try (
                BufferedWriter bw = new BufferedWriter(Files.newBufferedWriter(Paths.get(path +
                        "/webtoon-info.json"), StandardCharsets.UTF_8))
        ) {
            bw.write(json);
        }

        /*Writer writer2 = new FileWriter(path + "webtoon-info.json");

        try(Writer writer = new OutputStreamWriter(new FileOutputStream(path + "/webtoon-info.json") , "UTF-8")){
            Gson gson = new GsonBuilder().create();
            gson.toJson("Hello", writer);
            gson.toJson(123, writer);
        }*/

        return this;
    }

    public int titleNo() {
        return titleNo;
    }

    public String genre() {
        return genre;
    }

    public String codeName() {
        return codeName;
    }

    public int lastEpisodeNo() {
        return lastEpisodeNo;
    }

    public String path() {
        return path;
    }

    @Override
    public String toString() {
        return title + "\n" + imageUrl + "\n" + summary + "\n" + Arrays.toString(artists) +
                "\n" + genre + "\n" + codeName + "\n" + lastEpisodeNo;
    }

    public enum DayOfWeek {
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        SUNDAY,
        COMPLETED;

        @Override
        public String toString() {
            if (this.compareTo(COMPLETED) == 0) return "end";

            return name().toLowerCase();
        }
    }

    private static class WebtoonInfo {
        private ArrayList<String> artists = new ArrayList<>();
        private String codeName;
        private String dayOfWeek;
        private String genre;
        private float rating;
        private String summary;
        private String title;
        private int titleNo;

        public WebtoonInfo addArtists(String[] artists) {
            for (String artist : artists) this.artists.add(artist);
            return this;
        }

        public WebtoonInfo setCodeName(String codeName) {
            this.codeName = codeName;
            return this;
        }

        public WebtoonInfo setDayOfWeek(DayOfWeek dayOfWeek) {
            this.dayOfWeek = dayOfWeek.toString();
            return this;
        }

        public WebtoonInfo setGenre(String genre) {
            this.genre = genre;
            return this;
        }

        public WebtoonInfo setRating(float rating) {
            this.rating = rating;
            return this;
        }

        public WebtoonInfo setSummary(String summary) {
            this.summary = summary;
            return this;
        }

        public WebtoonInfo setTitle(String title) {
            this.title = title;
            return this;
        }

        public WebtoonInfo setTitleNo(int titleNo) {
            this.titleNo = titleNo;
            return this;
        }
    }
}
