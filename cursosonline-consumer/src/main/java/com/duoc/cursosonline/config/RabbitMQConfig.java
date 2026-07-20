package com.duoc.cursosonline.config;

import com.duoc.cursosonline.util.RabbitConstants;
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
    // CURSOS
    // ===========================

    @Bean
    public DirectExchange cursoExchange() {
        return new DirectExchange(RabbitConstants.CURSO_EXCHANGE);
    }

    @Bean
    public DirectExchange cursoDlxExchange() {
        return new DirectExchange(RabbitConstants.CURSO_DLX_EXCHANGE);
    }

    @Bean
    public Queue cursoQueue() {

        return new Queue(
                RabbitConstants.CURSO_QUEUE,
                true,
                false,
                false,
                Map.of(
                        "x-dead-letter-exchange", RabbitConstants.CURSO_DLX_EXCHANGE,
                        "x-dead-letter-routing-key", RabbitConstants.CURSO_DLQ
                )
        );
    }

    @Bean
    public Queue cursoDlq() {
        return new Queue(RabbitConstants.CURSO_DLQ, true);
    }

    @Bean
    public Binding cursoBinding() {

        return BindingBuilder
                .bind(cursoQueue())
                .to(cursoExchange())
                .with(RabbitConstants.CURSO_QUEUE);
    }

    @Bean
    public Binding cursoDlqBinding() {

        return BindingBuilder
                .bind(cursoDlq())
                .to(cursoDlxExchange())
                .with(RabbitConstants.CURSO_DLQ);
    }

    // ===========================
    // INSCRIPCIONES
    // ===========================

    @Bean
    public DirectExchange inscripcionExchange() {
        return new DirectExchange(RabbitConstants.INSCRIPCION_EXCHANGE);
    }

    @Bean
    public DirectExchange inscripcionDlxExchange() {
        return new DirectExchange(RabbitConstants.INSCRIPCION_DLX_EXCHANGE);
    }

    @Bean
    public Queue inscripcionQueue() {

        return new Queue(
                RabbitConstants.INSCRIPCION_QUEUE,
                true,
                false,
                false,
                Map.of(
                        "x-dead-letter-exchange", RabbitConstants.INSCRIPCION_DLX_EXCHANGE,
                        "x-dead-letter-routing-key", RabbitConstants.INSCRIPCION_DLQ
                )
        );
    }

    @Bean
    public Queue inscripcionDlq() {
        return new Queue(RabbitConstants.INSCRIPCION_DLQ, true);
    }

    @Bean
    public Binding inscripcionBinding() {

        return BindingBuilder
                .bind(inscripcionQueue())
                .to(inscripcionExchange())
                .with(RabbitConstants.INSCRIPCION_QUEUE);
    }

    @Bean
    public Binding inscripcionDlqBinding() {

        return BindingBuilder
                .bind(inscripcionDlq())
                .to(inscripcionDlxExchange())
                .with(RabbitConstants.INSCRIPCION_DLQ);
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
