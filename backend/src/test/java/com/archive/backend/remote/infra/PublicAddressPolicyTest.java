package com.archive.backend.remote.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Which addresses a request to another site may go to: only those of the public internet. */
class PublicAddressPolicyTest {

    private final PublicAddressPolicy policy = new PublicAddressPolicy();

    @ParameterizedTest
    @ValueSource(strings = {"8.8.8.8", "93.184.216.34", "172.32.0.1", "2001:4860:4860::8888"})
    void allowsTheInternet(String address) throws UnknownHostException {
        assertThat(policy.allows(InetAddress.getByName(address))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "127.0.0.1",
                "127.1.2.3",
                "::1",
                "0.0.0.0",
                "::",
                "10.1.2.3",
                "172.16.0.1",
                "172.31.255.255",
                "192.168.1.1",
                "169.254.169.254",
                "100.64.0.1",
                "fe80::1",
                "fc00::1",
                "fd12:3456::1",
                "224.0.0.1",
                "ff02::1",
                "255.255.255.255",
                "::ffff:127.0.0.1",
                "::ffff:10.0.0.1"
            })
    void refusesTheMachineItselfAndEveryInnerNetwork(String address) throws UnknownHostException {
        assertThat(policy.allows(InetAddress.getByName(address))).isFalse();
    }
}
