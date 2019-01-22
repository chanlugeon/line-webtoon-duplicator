package io.github.chanlugeon.lwduplicator.cui;

import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.chanlugeon.lwduplicator.cui.Console.Box.Alignment;
import io.github.chanlugeon.lwduplicator.update.Version;
import io.github.chanlugeon.lwduplicator.update.Version.VersionJson;
import io.github.chanlugeon.lwduplicator.webtoon.Episode;
import io.github.chanlugeon.lwduplicator.webtoon.EpisodeList;

public class CuiHandler {
    private static Scanner scan = new Scanner(System.in);

    public CuiHandler() {
        throw new AssertionError();
    }

    public static void run() {
        new Console.Box(70)
        .line("Line Webtoon Duplicator", Alignment.CENTER)
        .line("Made by Chan", Alignment.RIGHT)
        .newline()
        .line("My github: https://github.com/chanlugeon/line-webtoon-duplicator", Alignment.LEFT)
        .newline()
        .line("All content in Line Webtoon can be downloaded any number of times and stored forever due to Line Webtoon Duplicator.", Alignment.LEFT)
        .print();
        /*new Console.Box(70)
        .line("Hello", Alignment.CENTER)
        .print();*/

        // LATEST VERION CHECK
        VersionJson verJson = Version.readLatestVersionJson();
        if (verJson.isNewVersion()) {
            new Console.Box(70)
            .line("Found New Version", Alignment.CENTER)
            // .newline()
            .line("Latest version: " + verJson.version(), Alignment.LEFT)
            .line("Current version: " + Version.CURRENT_VERSION, Alignment.LEFT)
            .newline()
            .line("Update Log:", Alignment.LEFT)
            .line(verJson.updateLog(), Alignment.CENTER)
            .newline()
            .line("You can download latest version of Line Webton Duplicator from here:", Alignment.LEFT)
            .line("https://github.com/chanlugeon/line-webtoon-duplicator", Alignment.CENTER)
            .print();
        }


        System.out.print("Input title number of the webtoon: ");
        int titleNo = scan.nextInt();
        scan.nextLine();

        try {
            EpisodeList webtoon = EpisodeList.newInstance(titleNo);
            Console.println(webtoon.lastEpisodeNo());
            Console.println("Duplication are saved in " + webtoon.path());
            ExecutorService service = Executors.newCachedThreadPool();

            service.execute(() -> {
                try {
                    webtoon.downloadThumbnail();
                    Console.println("Stored thumbnail.jpg");
                } catch (IOException e) {
                    Console.println("ERROR: " + e.getMessage() + " " + e.getCause());
                }
            });

            for (int i = 1, n = webtoon.lastEpisodeNo(); i <= n; i++) {
                Optional<Episode> oep = webtoon.newEpisode(i);
                if (!oep.isPresent()) continue;
                Episode ep = oep.get();
                service.execute(() -> {
                    try {
                        ep.download();
                        Console.println("Stored " + ep.title());
                        ep.saveEpisodeInfo();
                        Console.println("Generated episode-info.json");
                    } catch (IOException e) {
                        Console.println("ERROR: " + e.getMessage() + " " + e.getCause());
                    }
                });
            }
            service.shutdown();
        } catch (IOException e) {
            // e.printStackTrace();
            Console.println("ERROR: " + e.getMessage() + " " + e.getCause());
        }

    }
}
