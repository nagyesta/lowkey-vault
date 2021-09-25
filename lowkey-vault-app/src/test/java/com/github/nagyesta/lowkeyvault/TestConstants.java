package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("checkstyle:JavadocVariable")
public final class TestConstants {

    private TestConstants() {
        throw new IllegalCallerException("Utility.");
    }

    //<editor-fold defaultstate="collapsed" desc="Time">
    public static final OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    public static final OffsetDateTime TIME_10_MINUTES_AGO = NOW.minusMinutes(10);
    public static final OffsetDateTime TIME_IN_10_MINUTES = NOW.plusMinutes(10);
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Common">
    public static final int INT_10 = 10;
    public static final int INT_20 = 20;
    public static final int INT_30 = 30;
    public static final int INT_40 = 40;
    public static final int INT_50 = 50;
    public static final String HEADER_VALUE = "header";
    public static final String EMPTY = "";
    public static final String BLANK = " ";
    public static final String KEY_URI_FORMAT = "%s/keys/%s/%s";
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="HTTP/HTTPS">
    public static final String HTTPS = "https://";
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Domains">
    public static final String LOOP_BACK_IP = "127.0.0.1";
    public static final String LOCALHOST = "localhost";
    public static final String LOWKEY_VAULT = "lowkey-vault";
    public static final String DEFAULT_SUB = "default.";
    public static final String DEFAULT_LOWKEY_VAULT = DEFAULT_SUB + LOWKEY_VAULT;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Port">
    public static final int HTTPS_PORT = 443;
    public static final int TOMCAT_SECURE_PORT = 8443;
    public static final int HTTP_PORT = 80;
    public static final String PORT_80 = ":80";
    public static final String PORT_8443 = ":8443";
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Tags">
    public static final String KEY_1 = "key1";
    public static final String KEY_2 = "key2";
    public static final String KEY_3 = "key3";
    public static final String VALUE_1 = "value1";
    public static final String VALUE_2 = "value2";
    public static final String VALUE_3 = "value3";
    public static final Map<String, String> TAGS_EMPTY = Collections.emptyMap();
    public static final Map<String, String> TAGS_ONE_KEY = Collections.singletonMap(KEY_1, VALUE_1);
    public static final Map<String, String> TAGS_TWO_KEYS = new TreeMap<>(Map.of(KEY_1, VALUE_1, KEY_2, VALUE_2));
    public static final Map<String, String> TAGS_THREE_KEYS = new TreeMap<>(Map.of(KEY_1, VALUE_1, KEY_2, VALUE_2, KEY_3, VALUE_3));
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Vault">
    public static final String DEFAULT_VAULT = "default";
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Properties">
    public static final KeyPropertiesModel PROPERTIES_MODEL = new KeyPropertiesModel();
    //</editor-fold>
}
