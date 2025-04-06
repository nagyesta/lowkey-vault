package com.github.nagyesta.lowkeyvault.http.management;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.net.URI;

@Getter
@EqualsAndHashCode
public final class TimeShiftContext {

    private final int seconds;

    private final boolean regenerateCertificates;

    private final URI vaultBaseUri;

    private TimeShiftContext(
            final int seconds,
            final URI vaultBaseUri,
            final boolean regenerateCertificates) {
        this.seconds = seconds;
        this.vaultBaseUri = vaultBaseUri;
        this.regenerateCertificates = regenerateCertificates;
    }

    public static TimeShiftContextBuilder builder() {
        return new TimeShiftContextBuilder();
    }

    public static final class TimeShiftContextBuilder {
        private static final int SECONDS_PER_MINUTE = 60;
        private static final int MINUTES_PER_HOUR = 60;
        private static final int HOURS_PER_DAY = 24;
        private int seconds;
        private URI vaultBaseUri;

        private boolean regenerateCertificates;

        TimeShiftContextBuilder() {
        }

        public TimeShiftContextBuilder addSeconds(final int seconds) {
            if (seconds <= 0) {
                throw new IllegalArgumentException("Time shift amount must be positive.");
            }
            this.seconds += seconds;
            return this;
        }

        public TimeShiftContextBuilder addMinutes(final int minutes) {
            return addSeconds(SECONDS_PER_MINUTE * minutes);
        }

        public TimeShiftContextBuilder addHours(final int hours) {
            return addMinutes(MINUTES_PER_HOUR * hours);
        }

        public TimeShiftContextBuilder addDays(final int days) {
            return addHours(HOURS_PER_DAY * days);
        }

        public TimeShiftContextBuilder regenerateCertificates() {
            this.regenerateCertificates = true;
            return this;
        }

        public TimeShiftContextBuilder vaultBaseUri(@NonNull final URI vaultBaseUri) {
            this.vaultBaseUri = vaultBaseUri;
            return this;
        }

        public TimeShiftContext build() {
            return new TimeShiftContext(seconds, vaultBaseUri, regenerateCertificates);
        }
    }
}
