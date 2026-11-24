package com.archive.backend.remote.infra;

import java.net.InetAddress;

/** Which addresses a request to another site may go to. */
public interface AddressPolicy {

    boolean allows(InetAddress address);
}
