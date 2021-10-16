package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;

import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;

@SuppressWarnings("checkstyle:JavadocVariable")
public final class TestConstantsKeys {

    private TestConstantsKeys() {
        throw new IllegalCallerException("Utility.");
    }

    //<editor-fold defaultstate="collapsed" desc="Crypto">
    public static final int MIN_RSA_KEY_SIZE = KeyType.RSA.validateOrDefault(null, Integer.class);
    public static final int MIN_AES_KEY_SIZE = KeyType.OCT.validateOrDefault(null, Integer.class);
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Keys">
    public static final String KEY_NAME_1 = "key-name-01";
    public static final String KEY_NAME_2 = "key-name-02";
    public static final String KEY_NAME_3 = "key-name-03";
    public static final String KEY_VERSION_1 = "00000000000000000000000000000001";
    public static final String KEY_VERSION_2 = "00000000000000000000000000000002";
    public static final String KEY_VERSION_3 = "00000000000000000000000000000003";
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Keys - Ids">
    public static final KeyEntityId UNVERSIONED_KEY_ENTITY_ID_1
            = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1);
    public static final KeyEntityId UNVERSIONED_KEY_ENTITY_ID_2
            = new KeyEntityId(HTTPS_LOWKEY_VAULT, KEY_NAME_2);
    public static final KeyEntityId UNVERSIONED_KEY_ENTITY_ID_3
            = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_3);

    public static final VersionedKeyEntityId VERSIONED_KEY_ENTITY_ID_1_VERSION_1
            = new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, KEY_VERSION_1);
    public static final VersionedKeyEntityId VERSIONED_KEY_ENTITY_ID_1_VERSION_2
            = new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, KEY_VERSION_2);
    public static final VersionedKeyEntityId VERSIONED_KEY_ENTITY_ID_1_VERSION_3
            = new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, KEY_VERSION_3);
    public static final VersionedKeyEntityId VERSIONED_KEY_ENTITY_ID_2_VERSION_1
            = new VersionedKeyEntityId(HTTPS_LOWKEY_VAULT, KEY_NAME_2, KEY_VERSION_1);
    public static final VersionedKeyEntityId VERSIONED_KEY_ENTITY_ID_2_VERSION_2
            = new VersionedKeyEntityId(HTTPS_LOWKEY_VAULT, KEY_NAME_2, KEY_VERSION_2);
    public static final VersionedKeyEntityId VERSIONED_KEY_ENTITY_ID_2_VERSION_3
            = new VersionedKeyEntityId(HTTPS_LOWKEY_VAULT, KEY_NAME_2, KEY_VERSION_3);
    public static final VersionedKeyEntityId VERSIONED_KEY_ENTITY_ID_3_VERSION_1
            = new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_3, KEY_VERSION_1);
    public static final VersionedKeyEntityId VERSIONED_KEY_ENTITY_ID_3_VERSION_2
            = new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_3, KEY_VERSION_2);
    public static final VersionedKeyEntityId VERSIONED_KEY_ENTITY_ID_3_VERSION_3
            = new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_3, KEY_VERSION_3);
    //</editor-fold>
}
