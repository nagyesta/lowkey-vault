package com.github.nagyesta.lowkeyvault.context;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyV73ModelToEntityConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class KeyConverterConfiguration {

    @Bean
    public KeyConverterRegistry keyConverterRegistry() {
        return new KeyConverterRegistry();
    }

    @Bean
    public KeyEntityToV72PropertiesModelConverter keyPropertiesConverter() {
        return new KeyEntityToV72PropertiesModelConverter(keyConverterRegistry());
    }

    @Bean
    @DependsOn("keyPropertiesConverter")
    public KeyEntityToV72ModelConverter keyModelConverter() {
        return new KeyEntityToV72ModelConverter(keyConverterRegistry());
    }

    @Bean
    @DependsOn("keyPropertiesConverter")
    public KeyEntityToV72KeyItemModelConverter keyItemConverter() {
        return new KeyEntityToV72KeyItemModelConverter(keyConverterRegistry());
    }

    @Bean
    @DependsOn("keyPropertiesConverter")
    public KeyEntityToV72KeyVersionItemModelConverter keyVersionedItemConverter() {
        return new KeyEntityToV72KeyVersionItemModelConverter(keyConverterRegistry());
    }

    @Bean
    @DependsOn("keyPropertiesConverter")
    public KeyEntityToV72BackupConverter keyBackupConverter() {
        return new KeyEntityToV72BackupConverter(keyConverterRegistry());
    }

    @Bean
    public KeyRotationPolicyToV73ModelConverter keyRotationPolicyModelConverter() {
        return new KeyRotationPolicyToV73ModelConverter(keyConverterRegistry());
    }

    @Bean
    public KeyRotationPolicyV73ModelToEntityConverter keyRotationPolicyEntityConverter() {
        return new KeyRotationPolicyV73ModelToEntityConverter(keyConverterRegistry());
    }
}
