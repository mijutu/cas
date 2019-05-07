package org.apereo.cas.configuration.model.core.authentication.passwordsync;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link PasswordSynchronizationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
public class PasswordSynchronizationProperties implements Serializable {
    private static final long serialVersionUID = -3878237508646993100L;

    /**
     * Options for password sync via LDAP.
     */
    private List<LdapPasswordSynchronizationProperties> ldap = new ArrayList<>();

}