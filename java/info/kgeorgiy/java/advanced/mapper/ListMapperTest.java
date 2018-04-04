package info.kgeorgiy.java.advanced.mapper;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.concurrent.ListIPTest;
import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.util.Arrays;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ListMapperTest extends ListIPTest {
    public ListMapperTest() {
        factors = Arrays.asList(1, 2, 5, 10);
    }

    @Override
    protected ListIP createInstance(final int threads) {
        return (ListIP) ScalarMapperTest.instance(threads);
    }

    @Override
    protected int getSubtasks(final int threads, final int totalThreads) {
        return ScalarMapperTest.subtasks(totalThreads);
    }

    @AfterClass
    public static void close() {
        ScalarMapperTest.close();
    }
}