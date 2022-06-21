package com.github.nagyesta.lowkeyvault.template.backup;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Component
public class TimeHelperSource {

    private final OffsetDateTime now;

    public TimeHelperSource() {
        this(OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
    }

    public TimeHelperSource(final OffsetDateTime now) {
        this.now = now;
    }


    public CharSequence now(final int offset) {
        return String.valueOf(now.plusSeconds(offset).toEpochSecond());
    }
}
