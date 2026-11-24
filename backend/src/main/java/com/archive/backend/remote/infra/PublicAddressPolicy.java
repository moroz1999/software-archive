package com.archive.backend.remote.infra;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.stereotype.Component;

/**
 * Only addresses of the public internet: none of the machine itself, of a private, shared or link-local network (the
 * metadata of a cloud among them), multicast, broadcast, reserved or unspecified, in IPv4 or IPv6, an IPv4 address
 * carried in IPv6 judged as itself.
 */
@Component
public class PublicAddressPolicy implements AddressPolicy {

    @Override
    public boolean allows(InetAddress address) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return false;
        }
        byte[] bytes = address.getAddress();
        if (address instanceof Inet4Address) {
            return allowsIpv4(bytes);
        }
        if (address instanceof Inet6Address) {
            return allowsIpv6(bytes);
        }
        return false;
    }

    private static boolean allowsIpv4(byte[] b) {
        int first = b[0] & 0xFF;
        int second = b[1] & 0xFF;
        return first != 0 // this network
                && !(first == 100 && (second & 0xC0) == 64) // shared address space of providers, 100.64.0.0/10
                && !(first == 192 && second == 0 && (b[2] & 0xFF) == 0) // IETF protocol assignments
                && !(first == 198 && (second == 18 || second == 19)) // benchmarking
                && first < 240; // reserved, the broadcast among them
    }

    private boolean allowsIpv6(byte[] b) {
        if ((b[0] & 0xFE) == 0xFC) {
            return false; // unique local, fc00::/7
        }
        boolean zeroHead = true;
        for (int i = 0; i < 10; i++) {
            zeroHead &= b[i] == 0;
        }
        boolean mapped = zeroHead && (b[10] & 0xFF) == 0xFF && (b[11] & 0xFF) == 0xFF;
        boolean compatible = zeroHead && b[10] == 0 && b[11] == 0;
        boolean nat64 =
                (b[0] & 0xFF) == 0x00 && (b[1] & 0xFF) == 0x64 && (b[2] & 0xFF) == 0xFF && (b[3] & 0xFF) == 0x9B;
        if (mapped || compatible || nat64) {
            byte[] v4 = {b[12], b[13], b[14], b[15]};
            try {
                return allows(InetAddress.getByAddress(v4));
            } catch (UnknownHostException e) {
                return false;
            }
        }
        return true;
    }
}
