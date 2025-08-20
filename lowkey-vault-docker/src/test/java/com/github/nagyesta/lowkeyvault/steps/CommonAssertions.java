package com.github.nagyesta.lowkeyvault.steps;

import org.testng.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class CommonAssertions {

    protected <T> void assertNotNull(final T actual) {
        Assert.assertNotNull(actual);
    }

    protected <T> void assertTrue(final String message, final boolean actual) {
        Assert.assertTrue(actual, message);
    }

    protected <T> void assertTrue(final boolean actual) {
        Assert.assertTrue(actual);
    }

    protected <T> void assertFail(final String message) {
        Assert.fail(message);
    }

    protected <T> void assertNull(final T actual) {
        Assert.assertNull(actual);
    }

    protected <T> void assertEquals(final T expected, final T actual) {
        Assert.assertEquals(actual, expected);
    }

    protected void assertArrayEquals(final byte[] expected, final byte[] actual) {
        Assert.assertEquals(actual, expected);
    }

    protected <K, V> void assertContainsEqualEntries(final Map<K, V> expected, final Map<K, V> actual) {
        assertEquals(expected.size(), actual.size());
        expected.forEach((key, value) -> assertEquals(value, actual.get(key)));
    }

    protected <E> void assertContainsEqualEntries(final Collection<E> expected, final Collection<E> actual) {
        assertEquals(expected, actual);
        assertEquals(expected.size(), actual.size());
    }

    protected <E extends Comparable<E>> void assertContainsEqualEntriesSorted(final Collection<E> expected, final Collection<E> actual) {
        final var expectedSorted = new ArrayList<>(expected);
        Collections.sort(expectedSorted);
        final var actualSorted = new ArrayList<>(actual);
        Collections.sort(actualSorted);
        assertEquals(expectedSorted, actualSorted);
        assertEquals(expectedSorted.size(), actualSorted.size());
    }

    protected void assertByteArrayLength(final int byteArrayLength, final byte[] bytes) {
        assertNotNull(bytes);
        assertTrue("Byte array was " + bytes.length + " long, expected " + byteArrayLength + " (+/-1 tolerance)",
                byteArrayLength - 1 <= bytes.length && byteArrayLength + 1 >= bytes.length);
    }

    protected String readResourceContent(final String resource) throws IOException {
        try (var stream = getClass().getResourceAsStream(resource);
             var reader = new InputStreamReader(Objects.requireNonNull(stream));
             var bufferedReader = new BufferedReader(reader)) {
            return bufferedReader.lines().collect(Collectors.joining(""))
                    .replaceAll(" +", "");
        }
    }
}
