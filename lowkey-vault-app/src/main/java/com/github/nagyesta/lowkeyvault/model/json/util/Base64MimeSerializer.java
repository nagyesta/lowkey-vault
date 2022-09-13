package com.github.nagyesta.lowkeyvault.model.json.util;

import java.util.Base64;

public class Base64MimeSerializer extends Base64Serializer {

    private static final Base64.Encoder ENCODER = Base64.getMimeEncoder();

    public Base64MimeSerializer() {
        super(ENCODER);
    }
}
