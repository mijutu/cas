package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.MongoDbTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.MongoDbTicketRegistryFacilitator;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.net.ssl.SSLContext;

/**
 * This is {@link MongoDbTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "mongoTicketRegistryConfiguration", proxyBeanMethods = false)
public class MongoDbTicketRegistryConfiguration {

    @RefreshScope
    @Bean
    @Autowired
    public TicketRegistry ticketRegistry(
        @Qualifier("ticketCatalog")
        final TicketCatalog ticketCatalog, final CasConfigurationProperties casProperties,
        @Qualifier("mongoDbTicketRegistryTemplate")
        final MongoTemplate mongoDbTicketRegistryTemplate,
        @Qualifier("ticketSerializationManager")
        final TicketSerializationManager ticketSerializationManager) {
        val mongo = casProperties.getTicket().getRegistry().getMongo();
        val mongoTemplate = mongoDbTicketRegistryTemplate;
        val registry = new MongoDbTicketRegistry(ticketCatalog, mongoTemplate, ticketSerializationManager);
        registry.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(mongo.getCrypto(), "mongo"));
        new MongoDbTicketRegistryFacilitator(ticketCatalog, mongoTemplate, mongo.isDropCollection()).createTicketCollections();
        return registry;
    }

    @ConditionalOnMissingBean(name = "mongoDbTicketRegistryTemplate")
    @Bean
    @RefreshScope
    @Autowired
    public MongoTemplate mongoDbTicketRegistryTemplate(final CasConfigurationProperties casProperties,
                                                       @Qualifier("sslContext")
                                                       final SSLContext sslContext) {
        val factory = new MongoDbConnectionFactory(sslContext);
        val mongo = casProperties.getTicket().getRegistry().getMongo();
        return factory.buildMongoTemplate(mongo);
    }
}
