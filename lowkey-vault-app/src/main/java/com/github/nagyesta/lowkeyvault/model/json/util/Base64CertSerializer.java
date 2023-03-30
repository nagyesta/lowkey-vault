package com.github.nagyesta.lowkeyvault.model.json.util;

import java.util.Base64;

public class Base64CertSerializer extends Base64Serializer {

    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    public Base64CertSerializer() {
        super(ENCODER);
    }
}
