package com.archive.backend.remote;

import com.archive.backend.remote.infra.AddressPolicy;
import java.net.Inet4Address;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Lets the integration tests reach their {@link TestSite} on the IPv4 loopback, which the archive never may. */
@TestConfiguration(proxyBeanMethods = false)
public class RemoteTestConfiguration {

    @Bean
    @Primary
    AddressPolicy loopbackOnly() {
        return address -> address instanceof Inet4Address && address.isLoopbackAddress();
    }
}
