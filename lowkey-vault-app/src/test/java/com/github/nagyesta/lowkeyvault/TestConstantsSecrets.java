package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;

import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;

@SuppressWarnings("checkstyle:JavadocVariable")
public final class TestConstantsSecrets {

    private TestConstantsSecrets() {
        throw new IllegalCallerException("Utility.");
    }

    //<editor-fold defaultstate="collapsed" desc="Secrets">
    public static final String SECRET_NAME_1 = "secret-name-01";
    public static final String SECRET_NAME_2 = "secret-name-02";
    public static final String SECRET_NAME_3 = "secret-name-03";
    public static final String SECRET_VERSION_1 = "00000000000000000000000000000001";
    public static final String SECRET_VERSION_2 = "00000000000000000000000000000002";
    public static final String SECRET_VERSION_3 = "00000000000000000000000000000003";
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Secrets - Ids">
    public static final SecretEntityId UNVERSIONED_SECRET_ENTITY_ID_1
            = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1);
    public static final SecretEntityId UNVERSIONED_SECRET_ENTITY_ID_2
            = new SecretEntityId(HTTPS_LOWKEY_VAULT, SECRET_NAME_2);
    public static final SecretEntityId UNVERSIONED_SECRET_ENTITY_ID_3
            = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_3);

    public static final VersionedSecretEntityId VERSIONED_SECRET_ENTITY_ID_1_VERSION_1
            = new VersionedSecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, SECRET_VERSION_1);
    public static final VersionedSecretEntityId VERSIONED_SECRET_ENTITY_ID_1_VERSION_2
            = new VersionedSecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, SECRET_VERSION_2);
    public static final VersionedSecretEntityId VERSIONED_SECRET_ENTITY_ID_1_VERSION_3
            = new VersionedSecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, SECRET_VERSION_3);
    public static final VersionedSecretEntityId VERSIONED_SECRET_ENTITY_ID_2_VERSION_1
            = new VersionedSecretEntityId(HTTPS_LOWKEY_VAULT, SECRET_NAME_2, SECRET_VERSION_1);
    public static final VersionedSecretEntityId VERSIONED_SECRET_ENTITY_ID_2_VERSION_2
            = new VersionedSecretEntityId(HTTPS_LOWKEY_VAULT, SECRET_NAME_2, SECRET_VERSION_2);
    public static final VersionedSecretEntityId VERSIONED_SECRET_ENTITY_ID_2_VERSION_3
            = new VersionedSecretEntityId(HTTPS_LOWKEY_VAULT, SECRET_NAME_2, SECRET_VERSION_3);
    public static final VersionedSecretEntityId VERSIONED_SECRET_ENTITY_ID_3_VERSION_1
            = new VersionedSecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_3, SECRET_VERSION_1);
    public static final VersionedSecretEntityId VERSIONED_SECRET_ENTITY_ID_3_VERSION_2
            = new VersionedSecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_3, SECRET_VERSION_2);
    public static final VersionedSecretEntityId VERSIONED_SECRET_ENTITY_ID_3_VERSION_3
            = new VersionedSecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_3, SECRET_VERSION_3);
    //</editor-fold>
}
