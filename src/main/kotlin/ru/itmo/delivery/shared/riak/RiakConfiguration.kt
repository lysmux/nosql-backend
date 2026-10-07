package ru.itmo.delivery.shared.riak

import com.basho.riak.client.api.RiakClient
import com.basho.riak.client.core.RiakNode
import com.basho.riak.client.core.util.HostAndPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RiakConfiguration {
    @Bean(destroyMethod = "shutdown")
    fun riakClient(config: RiakConfig): RiakClient = RiakClient.newClient(
        config.nodes.map { HostAndPort.fromString(it, RiakNode.Builder.DEFAULT_REMOTE_PORT) },
        RiakClient.createDefaultNodeBuilder().withConnectionTimeout(config.timeout.toMillis().toInt()),
    )
}
