package com.github.nagyesta.lowkeyvault.model.json.util;

import java.util.Base64;

public class Base64CertDeserializer extends Base64Deserializer {
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    public Base64CertDeserializer() {
        super(DECODER);
    }
}
