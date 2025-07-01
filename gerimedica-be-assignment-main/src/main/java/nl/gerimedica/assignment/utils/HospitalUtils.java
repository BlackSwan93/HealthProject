package nl.gerimedica.assignment.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;


// Utility methods for general-purpose hospital logic.
// For now, just keeps track of how often it's used — might expand later if needed.

@Slf4j
public final class HospitalUtils {

    private static final AtomicInteger usageCounter = new AtomicInteger(0);

    private HospitalUtils() {
        throw new IllegalStateException("This is just a utility class.");
    }

    public static void recordUsage(String context) {
        int count = usageCounter.incrementAndGet();
        log.info("Used at {} | Count: {} | Context: {}", LocalDateTime.now(), count, context);
    }

    public static boolean isValidDate(java.time.LocalDate date) {
        return date != null && !date.isAfter(java.time.LocalDate.now());
    }

    public static int getUsageCount() {
        return usageCounter.get();
    }
}