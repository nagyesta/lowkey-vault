Feature: Certificate renewal/recreation

    @Certificate @CertificateCreate @CertificateTimeShift @RSA
    Scenario Outline: RSA_CERT_TIME_SHIFT_01 Single versions of RSA certificates can be recreated or renewed with time shift
        Given certificate API version <api> is used
        And a vault is created with name certs-time-shift-rsa-<index>
        And a certificate client is created with the vault named certs-time-shift-rsa-<index>
        And a <type> certificate is prepared with subject <subject>
        And the certificate is set to expire in <expiryMonths> months
        And the lifetime action trigger is set to AutoRenew when 1 days before expiry reached
        And the certificate is set to be enabled
        And the certificate is set to use an RSA key with 2048 and without HSM
        And the certificate is created with name <certName>
        When the time of the vault named certs-time-shift-rsa-<index> is shifted by <shiftDays> days
        Then the certificate is enabled
        And the certificate secret named <certName> is downloaded
        And the downloaded secret contains a <type> certificate
        And the downloaded <type> certificate store was shifted <shiftDays> days, using renewals 1 days before <expiryMonths> months expiry
        And the downloaded <type> certificate store has a certificate with <subject> as subject


        Examples:
            | api | index | certName           | type | subject        | expiryMonths | shiftDays |
            | 7.3 | 1     | 73-recreateRsaCert | PEM  | CN=localhost   | 20           | 100       |
            | 7.3 | 2     | 73-renewRsaCert    | PEM  | CN=example.com | 5            | 360       |
            | 7.4 | 3     | 74-recreateRsaCert | PEM  | CN=localhost   | 20           | 100       |
            | 7.5 | 4     | 75-recreateRsaCert | PEM  | CN=localhost   | 20           | 100       |
            | 7.6 | 5     | 76-recreateRsaCert | PEM  | CN=localhost   | 20           | 100       |

    @Certificate @CertificateCreate @CertificateTimeShift @EC
    Scenario Outline: EC_CERT_TIME_SHIFT_01 Single versions of EC certificates can be recreated or renewed with time shift
        Given certificate API version <api> is used
        And a vault is created with name certs-time-shift-ec-<index>
        And a certificate client is created with the vault named certs-time-shift-ec-<index>
        And a <type> certificate is prepared with subject <subject>
        And the certificate is set to expire in <expiryMonths> months
        And the lifetime action trigger is set to AutoRenew when 1 days before expiry reached
        And the certificate is set to be enabled
        And the certificate is set to use an EC key with P-521 and without HSM
        And the certificate is created with name <certName>
        When the time of the vault named certs-time-shift-ec-<index> is shifted by <shiftDays> days
        Then the certificate is enabled
        And the certificate secret named <certName> is downloaded
        And the downloaded secret contains a <type> certificate
        And the downloaded <type> certificate store was shifted <shiftDays> days, using renewals 1 days before <expiryMonths> months expiry
        And the downloaded <type> certificate store has a certificate with <subject> as subject


        Examples:
            | api | index | certName          | type | subject        | expiryMonths | shiftDays |
            | 7.3 | 1     | 73-recreateEcCert | PEM  | CN=localhost   | 20           | 100       |
            | 7.3 | 2     | 73-renewEcCert    | PEM  | CN=example.com | 5            | 360       |
            | 7.4 | 3     | 74-renewEcCert    | PEM  | CN=example.com | 5            | 360       |
            | 7.5 | 4     | 75-renewEcCert    | PEM  | CN=example.com | 5            | 360       |
            | 7.6 | 5     | 76-renewEcCert    | PEM  | CN=example.com | 5            | 360       |
