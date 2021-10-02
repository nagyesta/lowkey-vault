package com.github.nagyesta.lowkeyvault.model.v7_2.key.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAesKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyEcKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRsaKeyVaultKeyEntity;
import org.springframework.util.Assert;

import java.util.Arrays;

/**
 * Defines values for KeyType.
 */
public enum KeyType {

    /**
     * Static value EC for KeyType.
     */
    EC("EC") {
        @Override
        public boolean isEc() {
            return true;
        }
    },

    /**
     * Static value EC-HSM for KeyType.
     */
    EC_HSM("EC-HSM") {
        @Override
        public boolean isEc() {
            return true;
        }

        @Override
        public boolean isHsm() {
            return true;
        }
    },

    /**
     * Static value RSA for KeyType.
     */
    RSA("RSA") {
        @Override
        public boolean isRsa() {
            return true;
        }
    },

    /**
     * Static value RSA-HSM for KeyType.
     */
    RSA_HSM("RSA-HSM") {
        @Override
        public boolean isRsa() {
            return true;
        }

        @Override
        public boolean isHsm() {
            return true;
        }
    },

    /**
     * Static value oct for KeyType.
     */
    OCT("oct") {
        @Override
        public boolean isOct() {
            return true;
        }
    },

    /**
     * Static value oct-HSM for KeyType.
     */
    OCT_HSM("oct-HSM") {
        @Override
        public boolean isOct() {
            return true;
        }

        @Override
        public boolean isHsm() {
            return true;
        }
    };

    private static final int DEFAULT_EC_SIZE = 256;
    private static final int DEFAULT_RSA_SIZE = 1024;
    private static final int DEFAULT_SYMMETRIC_SIZE = 128;
    private final String value;

    KeyType(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static KeyType forValue(final String name) {
        return Arrays.stream(values()).filter(keyType -> keyType.getValue().equals(name)).findFirst().orElse(null);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonIgnore
    public boolean isRsa() {
        return false;
    }

    @JsonIgnore
    public boolean isEc() {
        return false;
    }

    @JsonIgnore
    public boolean isOct() {
        return false;
    }

    @JsonIgnore
    public boolean isHsm() {
        return false;
    }

    @JsonIgnore
    public String getAlgorithmName() {
        if (isEc()) {
            return "EC";
        } else if (isRsa()) {
            return "RSA";
        } else {
            Assert.isTrue(isOct(), "Unknown key type found: " + this);
            return "AES";
        }
    }

    @JsonIgnore
    public int getDefaultKeySize() {
        if (isEc()) {
            return DEFAULT_EC_SIZE;
        } else if (isRsa()) {
            return DEFAULT_RSA_SIZE;
        } else {
            Assert.isTrue(isOct(), "Unknown key type found: " + this);
            return DEFAULT_SYMMETRIC_SIZE;
        }
    }

    @JsonIgnore
    public Class<? extends ReadOnlyKeyVaultKeyEntity> entityClass() {
        if (isRsa()) {
            return ReadOnlyRsaKeyVaultKeyEntity.class;
        } else if (isEc()) {
            return ReadOnlyEcKeyVaultKeyEntity.class;
        } else {
            Assert.isTrue(isOct(), "Unknown key type found: " + this);
            return ReadOnlyAesKeyVaultKeyEntity.class;
        }
    }

}
