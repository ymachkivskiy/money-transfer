package utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ListUtils {

    @SafeVarargs
    public static <E, T extends Collection<? extends E>> List<E> merge(T... lists) {
        return Stream.of(lists)
                .flatMap(l -> l.stream())
                .collect(toList());
    }

}
