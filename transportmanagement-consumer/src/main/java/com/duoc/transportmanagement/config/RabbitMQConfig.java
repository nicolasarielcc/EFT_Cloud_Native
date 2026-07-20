package com.duoc.transportmanagement.config;

import com.duoc.transportmanagement.util.RabbitConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // ===========================
    // GUIAS
    // ===========================

    @Bean
    public DirectExchange guiaExchange() {
        return new DirectExchange(RabbitConstants.GUIA_EXCHANGE);
    }

    @Bean
    public DirectExchange guiaDlxExchange() {
        return new DirectExchange(RabbitConstants.GUIA_DLX_EXCHANGE);
    }

    @Bean
    public Queue guiaQueue() {

        return new Queue(
                RabbitConstants.GUIA_QUEUE,
                true,
                false,
                false,
                Map.of(
                        "x-dead-letter-exchange", RabbitConstants.GUIA_DLX_EXCHANGE,
                        "x-dead-letter-routing-key", RabbitConstants.GUIA_DLQ
                )
        );
    }

    @Bean
    public Queue guiaDlq() {
        return new Queue(RabbitConstants.GUIA_DLQ, true);
    }

    @Bean
    public Binding guiaBinding() {

        return BindingBuilder
                .bind(guiaQueue())
                .to(guiaExchange())
                .with(RabbitConstants.GUIA_QUEUE);
    }

    @Bean
    public Binding guiaDlqBinding() {

        return BindingBuilder
                .bind(guiaDlq())
                .to(guiaDlxExchange())
                .with(RabbitConstants.GUIA_DLQ);
    }

    // ===========================
    // TRANSPORTISTAS
    // ===========================

    @Bean
    public DirectExchange transportistaExchange() {
        return new DirectExchange(RabbitConstants.TRANSPORTISTA_EXCHANGE);
    }

    @Bean
    public DirectExchange transportistaDlxExchange() {
        return new DirectExchange(RabbitConstants.TRANSPORTISTA_DLX_EXCHANGE);
    }

    @Bean
    public Queue transportistaQueue() {

        return new Queue(
                RabbitConstants.TRANSPORTISTA_QUEUE,
                true,
                false,
                false,
                Map.of(
                        "x-dead-letter-exchange", RabbitConstants.TRANSPORTISTA_DLX_EXCHANGE,
                        "x-dead-letter-routing-key", RabbitConstants.TRANSPORTISTA_DLQ
                )
        );
    }

    @Bean
    public Queue transportistaDlq() {
        return new Queue(RabbitConstants.TRANSPORTISTA_DLQ, true);
    }

    @Bean
    public Binding transportistaBinding() {

        return BindingBuilder
                .bind(transportistaQueue())
                .to(transportistaExchange())
                .with(RabbitConstants.TRANSPORTISTA_QUEUE);
    }

    @Bean
    public Binding transportistaDlqBinding() {

        return BindingBuilder
                .bind(transportistaDlq())
                .to(transportistaDlxExchange())
                .with(RabbitConstants.TRANSPORTISTA_DLQ);
    }

    // ===========================
    // JSON
    // ===========================

    @Bean
    public MessageConverter jsonMessageConverter() {

        Jackson2JsonMessageConverter converter =
                new Jackson2JsonMessageConverter();

        DefaultJackson2JavaTypeMapper typeMapper =
                new DefaultJackson2JavaTypeMapper();

        typeMapper.setTrustedPackages("*");

        typeMapper.setTypePrecedence(
                Jackson2JavaTypeMapper.TypePrecedence.INFERRED
        );

        converter.setJavaTypeMapper(typeMapper);

        return converter;
    }

    // ===========================
    // TEMPLATE
    // ===========================

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {

        RabbitTemplate template =
                new RabbitTemplate(connectionFactory);

        template.setMessageConverter(messageConverter);

        return template;
    }

    // ===========================
    // ADMIN
    // ===========================

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {

        return new RabbitAdmin(connectionFactory);

    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            RabbitTemplate rabbitTemplate) {

        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);

        factory.setDefaultRequeueRejected(false);

        factory.setAdviceChain(retryInterceptor(rabbitTemplate));

        return factory;
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor(RabbitTemplate rabbitTemplate) {

        return RetryInterceptorBuilder.stateless()
                .maxAttempts(1)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }
}
