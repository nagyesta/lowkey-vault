package com.github.nagyesta.lowkeyvault.context;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class SecretConverterConfiguration {

    @Bean
    public SecretConverterRegistry secretConverterRegistry() {
        return new SecretConverterRegistry();
    }

    @Bean
    public SecretEntityToV72PropertiesModelConverter secretPropertiesConverter() {
        return new SecretEntityToV72PropertiesModelConverter(secretConverterRegistry());
    }

    @Bean
    @DependsOn("secretPropertiesConverter")
    public SecretEntityToV72ModelConverter secretModelConverter() {
        return new SecretEntityToV72ModelConverter(secretConverterRegistry());
    }

    @Bean
    @DependsOn("secretPropertiesConverter")
    public SecretEntityToV72SecretItemModelConverter secretItemConverter() {
        return new SecretEntityToV72SecretItemModelConverter(secretConverterRegistry());
    }

    @Bean
    @DependsOn("secretPropertiesConverter")
    public SecretEntityToV72SecretVersionItemModelConverter secretVersionedItemConverter() {
        return new SecretEntityToV72SecretVersionItemModelConverter(secretConverterRegistry());
    }

    @Bean
    @DependsOn("secretPropertiesConverter")
    public SecretEntityToV72BackupConverter secretBackupConverter() {
        return new SecretEntityToV72BackupConverter(secretConverterRegistry());
    }
}
