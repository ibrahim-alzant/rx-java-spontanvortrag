import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Created by alzant on 11/11/17.
 */
@Slf4j
public class Comparison {

    @Test
    public void javaStreamSucks(){
        Stream<String> stupidStream = Stream.of("a","b","c");
        Stream<String> anotherStupidStream = stupidStream.map(String::toUpperCase);
        anotherStupidStream.forEach(System.out::println);
    }

    @Test
    public void downloadBlocking(){
        Map<URL,String> downloadedSites = new HashMap<>();

        List<URL> blockingList = Urls.all().toList().blockingGet();
        for (URL url : blockingList){
            downloadedSites.put(url, UriDownloader.downloadBlocking(url));
        }
    }

    @Test
    public void downloadStream(){
        Urls
                .all()
                .flatMap(UriDownloader::download)
                .subscribe(s -> {
                    System.out.println(s);
                });

    }

    @Test
    public void downloadAsyncLazy(){
        Urls
                .all()
                .flatMap(url -> UriDownloader
                        .download(url)
                        .subscribeOn(Schedulers.io()))
                .doOnNext(System.out::println)
                .blockingLast();
    }

    @Test
    public void downloadAsync(){
        Urls
                .all()
                .flatMap(url -> UriDownloader
                        .download(url)
                        .subscribeOn(Schedulers.io()))
                .doOnNext(s -> System.out.println(s))
                .toList()
                .blockingGet();
    }

    @Test
    public void downloadAsyncWithZip(){
        Flowable<URL> urls = Urls.all();

        Flowable.zip(
                urls,
                urls.flatMap(url -> UriDownloader.download(url).subscribeOn(Schedulers.io())),
                (url, s) -> s
        ).doOnNext(System.out::println)
                .toList()
                .blockingGet();
    }

    @Test
    public void concat() throws Exception {
        Flowable<Integer> first = slowStreamInteger(1, 2, 3).subscribeOn(Schedulers.io());
        Flowable<Integer> second = slowStreamInteger( 4, 5, 6).subscribeOn(Schedulers.io());

        //first   || second
        //1, 2, 3 || 4, 5, 6

        Flowable.concat(first,second)
                .take(6)
                .map(String::valueOf)
                .doOnNext(System.out::println)
                .blockingLast();
        // a nice example would be to remove onComplete to show that the stream never stops
    }

    @Test
    public void merge() throws Exception {
        Flowable<Integer> first = slowStreamInteger(1, 2, 3).subscribeOn(Schedulers.io());
        Flowable<Integer> second = slowStreamInteger( 4, 5, 6);


        //first   || second
        //1, 2, 3 || 4, 5, 6

        Flowable.merge(first,second)
                .take(6)
                .map(String::valueOf)
                .doOnNext(System.out::println)
                .blockingLast();
    }

    @Test
    public void toMap() {
        Flowable<URL> urls = Urls.all();
        Map<URL, String> sitesMap = urls
                .flatMap(url -> UriDownloader.download(url)
                        .map(body -> Pair.of(url, body)))
                .toMap(p -> p.getLeft(), p -> p.getRight())
                .blockingGet();
    }

    @Test
    public void toMapWithZip() throws Exception {
        Flowable<URL> urls = Urls.all();

        Map<URL, String> sitesMap = Flowable
                .zip(urls, urls.flatMap(UriDownloader::download).subscribeOn(Schedulers.io()), Pair::of)
                .doOnNext(System.out::println)
                .toMap(pair -> pair.getLeft(), pair -> pair.getRight())
                .blockingGet();

        System.out.println(sitesMap);
    }

    private Flowable<Integer> slowStreamInteger(int ... list){
        return Flowable.unsafeCreate(subscriber -> {
            Arrays.stream(list).forEach(value -> {
                subscriber.onNext(value);
                sleep(Duration.ofMillis(1000));
            });
            subscriber.onComplete();
        });

    }

    private static void sleep(Duration duration) {
        try {
            TimeUnit.MILLISECONDS.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            log.warn("Sleep interrupted", e);
        }
    }


}
