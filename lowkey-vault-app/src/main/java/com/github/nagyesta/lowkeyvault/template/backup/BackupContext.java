package com.github.nagyesta.lowkeyvault.template.backup;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BackupContext {

    private String host;

    private int port;
}
