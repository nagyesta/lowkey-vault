package com.github.nagyesta.lowkeyvault.template.backup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class BackupContext {

    @NonNull
    private String host;

    private int port;
}
