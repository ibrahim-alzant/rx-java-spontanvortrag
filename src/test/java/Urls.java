import io.reactivex.Flowable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * Created by alzant on 11/11/17.
 */
public class Urls {

    public static Flowable<URL> all() {
        return all("urls.txt");
    }

    public static Flowable<URL> all(String fileName) {
        return Flowable.defer(() -> load(fileName));
    }

    private static Flowable<URL> load(String fileName) {
        try (Stream<String> lines = classpathReaderOf(fileName).lines()) {
            final List<URL> all = lines
                    .map(url -> {
                        try {
                            return new URL(url);
                        } catch (MalformedURLException e) {
                            throw new IllegalArgumentException(e);
                        }
                    })
                    .collect(toList());
            return Flowable.fromIterable(all);
        } catch (Exception e) {
            return Flowable.error(e);
        }
    }

    private static BufferedReader classpathReaderOf(String fileName) throws IOException {
        URL input = Urls.class.getResource(fileName);
        if (input == null) {
            throw new FileNotFoundException(fileName);
        }
        InputStream is = input.openStream();
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

}
