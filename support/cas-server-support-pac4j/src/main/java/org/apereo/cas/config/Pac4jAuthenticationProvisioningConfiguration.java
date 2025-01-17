package org.apereo.cas.config;

import org.apereo.cas.api.PrincipalProvisioner;
import org.apereo.cas.authentication.principal.provision.DelegatedClientUserProfileProvisioner;
import org.apereo.cas.authentication.principal.provision.GroovyDelegatedClientUserProfileProvisioner;
import org.apereo.cas.authentication.principal.provision.RestfulDelegatedClientUserProfileProvisioner;
import org.apereo.cas.authentication.principal.provision.ScimDelegatedClientUserProfileProvisioner;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.function.Supplier;

/**
 * This is {@link Pac4jAuthenticationProvisioningConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication)
@Configuration(value = "Pac4jAuthenticationProvisioningConfiguration", proxyBeanMethods = false)
public class Pac4jAuthenticationProvisioningConfiguration {

    @Configuration(value = "Pac4jAuthenticationScimProvisioningConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnClass(PrincipalProvisioner.class)
    public static class Pac4jAuthenticationScimProvisioningConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "pac4jScimDelegatedClientUserProfileProvisioner")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Supplier<DelegatedClientUserProfileProvisioner> pac4jScimDelegatedClientUserProfileProvisioner(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(PrincipalProvisioner.BEAN_NAME)
            final PrincipalProvisioner scimProvisioner) {
            return BeanSupplier.of(Supplier.class)
                .when(BeanCondition.on("cas.authn.pac4j.provisioning.scim.enabled").isTrue().given(applicationContext.getEnvironment()))
                .supply(() -> () -> new ScimDelegatedClientUserProfileProvisioner(scimProvisioner))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "Pac4jAuthenticationEventExecutionPlanProvisionerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class Pac4jAuthenticationEventExecutionPlanProvisionerConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "groovyDelegatedClientUserProfileProvisioner")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Supplier<DelegatedClientUserProfileProvisioner> groovyDelegatedClientUserProfileProvisioner(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(Supplier.class)
                .when(BeanCondition.on("cas.authn.pac4j.provisioning.groovy.location").exists()
                    .given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val provisioning = casProperties.getAuthn().getPac4j().getProvisioning();
                    val script = provisioning.getGroovy().getLocation();
                    return () -> new GroovyDelegatedClientUserProfileProvisioner(script);
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "restDelegatedClientUserProfileProvisioner")
        public Supplier<DelegatedClientUserProfileProvisioner> restDelegatedClientUserProfileProvisioner(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) throws Exception {
            return BeanSupplier.of(Supplier.class)
                .when(BeanCondition.on("cas.authn.pac4j.provisioning.rest.url").isUrl().given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val provisioning = casProperties.getAuthn().getPac4j().getProvisioning();
                    return () -> new RestfulDelegatedClientUserProfileProvisioner(provisioning.getRest());
                })
                .otherwiseProxy()
                .get();
        }
    }
}
