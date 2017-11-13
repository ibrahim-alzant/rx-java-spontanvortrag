import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.concurrent.Semaphore;

/**
 * Created by alzant on 11/11/17.
 */
@Slf4j
public class UriDownloader {

    public static final int TOTAL = 10;
    private static final Semaphore counter = new Semaphore(TOTAL);

    public static Flowable<String> downloadAsync(URL url, Scheduler scheduler) {
        return Flowable
                .fromCallable(() -> downloadBlocking(url))
                .subscribeOn(scheduler);
    }

    public static Flowable<String> download(URL url) {
        return Flowable.fromCallable(() -> downloadBlocking(url));
    }

    public static String downloadBlocking(URL url) {
        log.info("Downloading: {}", url);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("Done: {}", url);
        return "<html>" + url.getHost() + "</html>";
    }
}
