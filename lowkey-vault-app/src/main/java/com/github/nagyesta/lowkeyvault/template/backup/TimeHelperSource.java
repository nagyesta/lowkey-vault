package com.github.nagyesta.lowkeyvault.template.backup;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Component
@SuppressWarnings("java:S6829") //cannot add autowired to the default constructor
public class TimeHelperSource {

    private final OffsetDateTime now;

    public TimeHelperSource() {
        this(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
    }

    TimeHelperSource(final OffsetDateTime now) {
        this.now = now;
    }

    public CharSequence now(final int offset) {
        return String.valueOf(now.plusSeconds(offset).toEpochSecond());
    }
}
