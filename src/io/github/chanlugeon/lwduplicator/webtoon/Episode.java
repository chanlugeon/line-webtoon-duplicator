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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.chanlugeon.util.Util;

public class Episode {
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^https://www[.]webtoons[.]com/en/\\w+/[\\w-]+/(?<epcodename>[\\w-]+)/");
    private static final Pattern CURRENT_LIKE = Pattern.compile(
            ", 'currentCount' : (?<like>\\d+)$", Pattern.MULTILINE);

    private final ArrayList<String> imageUrl = new ArrayList<>();
    private final String codeName;
    private final int likes;
    // full title.
    private final String title;
    private final int episodeNo;
    private final String thumbnail;

    private final String referer;
    private final String path;

    public Episode(EpisodeList epList, int episodeNo) throws IOException {
        referer = EpisodeList.ORIGIN_EN + "/" + epList.genre() + "/" + epList.codeName() + "/recent-episode/viewer?title_no=" +
                epList.titleNo() + "&episode_no=" + episodeNo;

        final Response res = Jsoup.connect(referer)
                .timeout(Util.WAIT_TIME)
                .userAgent(Util.USER_AGENT)
                .method(Method.GET)
                .execute();
        final String body = res.body();
        final Document doc = res.parse();

        title = doc.selectFirst("meta[property=og:title]").attr("content");
        /*
        thumbnail = doc.select(
                "div#topEpisodeList > div.episode_lst > div.episode_cont > ul > li[data-episode-no=" +
                episodeNo + "] > a > span.thmb > img").attr("data-url");
        */
        thumbnail = doc.select("meta[name=twitter:image]").attr("content");

        final String currentUrl = doc.selectFirst("meta[property=og:url]").attr("content");
        final Matcher urlMatcher = URL_PATTERN.matcher(currentUrl);
        urlMatcher.find();
        codeName = urlMatcher.group("epcodename");

        // It is not displayed over 100000.
        // likes = Integer.parseInt(doc.selectFirst("a#likeItButton > span._likeCount").text().replace(",", ""));

        final Matcher likeCount = CURRENT_LIKE.matcher(body);
        likeCount.find();
        likes = Integer.parseInt(likeCount.group("like"));

        this.episodeNo = episodeNo;
        path = epList.path() + "/" + episodeNo;

        Element imgList = doc.selectFirst("#_imageList");
        Elements images = imgList.select("img");

        int last = images.size();
        imageUrl.ensureCapacity(images.size());
        int a = 0;
        for (Element i : images) {
            // If the image is Line logo, it is not downloaded.
            if (++a == last && i.attr("height").compareTo("300.0") == 0) break;
            imageUrl.add(i.attr("data-url"));
        }

        // Comments
        // Elements cmts = doc.select("div#cbox_module > div > div.u_cbox_content_wrap > ul.u_cbox_list");
        /*for (int i = 0; i < 3; i++) { // .select("li")
            Element cmt = cmts.get(i);
            System.out.println(cmt.text());
        }*/
    }

    public Episode download() throws IOException {
        File file = new File(path);
        file.mkdirs();

        // Download content.
        int count = 1;
        for (String i : imageUrl) {
            InputStream in = Jsoup.connect(i)
                    .timeout(Util.WAIT_TIME)
                    .header("DNT", "1")
                    .referrer(referer)
                    .userAgent(Util.USER_AGENT)
                    .ignoreContentType(true)
                    .method(Method.GET)
                    .execute()
                    .bodyStream();

            OutputStream out = new BufferedOutputStream(new FileOutputStream(path + "/" + count++ + ".jpg"));
            for (int b; (b = in.read()) != -1; ) {
                out.write(b);
            }

            out.close();
            in.close();
        }

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

    public Episode saveEpisodeInfo() throws IOException {
        EpisodeInfo info = new EpisodeInfo()
                .episodeNo(episodeNo)
                .likes(likes)
                .title(title);


        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        String json = gson.toJson(info);
        try (
                BufferedWriter bw = new BufferedWriter(Files.newBufferedWriter(Paths.get(path +
                        "/episode-info.json"), StandardCharsets.UTF_8))
        ) {
            bw.write(json);
        }

        return this;
    }

    /*public Episode saveCommentsInfo() throws IOException {
        CommentInfo info = new CommentInfo()
                .episodeNo(episodeNo)
                .likes(likes)
                .title(title);

        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        String json = gson.toJson(info);
        try (
                BufferedWriter bw = new BufferedWriter(Files.newBufferedWriter(Paths.get(path +
                        "/episode-info.json"), StandardCharsets.UTF_8))
        ) {
            bw.write(json);
        }

        return this;
    }*/

    public String title() {
        return title;
    }

    public int episodeNo() {
        return episodeNo;
    }

    @Override
    public String toString() {
        return Arrays.asList(title, codeName, imageUrl).toString();
    }

    private static class EpisodeInfo {
        // private String date;
        private int episodeNo;
        private int likes;
        private String title;

        public EpisodeInfo episodeNo(int epNo) {
            episodeNo = epNo;
            return this;
        }

        public EpisodeInfo likes(int likes) {
            this.likes = likes;
            return this;
        }

        public EpisodeInfo title(String title) {
            this.title = title;
            return this;
        }
    }

    /*
    private static class CommentsInfo {
        ArrayList<CommentInfo> commentsInfo = new ArrayList<>();

        private static class CommentInfo {
            private String content;
            private String date;
            private int recommended;
            private int unrecommended;
            private RepliesInfo repliesInfo;

            private static class RepliesInfo {
                private static class ReplyInfo {
                    private String content;
                    private String date;
                    private int recommended;
                    private int unrecommended;
                }
            }
        }
    }
    */
}
