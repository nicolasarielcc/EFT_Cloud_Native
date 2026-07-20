package com.duoc.transportmanagement.service;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Service;

@Service
public class RabbitAdminServiceImpl implements RabbitAdminService {

    private final AmqpAdmin amqpAdmin;

    public RabbitAdminServiceImpl(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    @Override
    public void crearCola(String nombreCola) {

        Queue queue = new Queue(nombreCola, true);

        amqpAdmin.declareQueue(queue);
    }

    @Override
    public void eliminarCola(String nombreCola) {

        amqpAdmin.deleteQueue(nombreCola);
    }

    @Override
    public void crearExchange(String nombreExchange) {

        DirectExchange exchange =
                new DirectExchange(
                        nombreExchange,
                        true,
                        false
                );

        amqpAdmin.declareExchange(exchange);
    }

    @Override
    public void eliminarExchange(String nombreExchange) {

        amqpAdmin.deleteExchange(nombreExchange);
    }

    @Override
    public void crearBinding(String nombreCola,
                             String nombreExchange,
                             String routingKey) {

        Queue queue = new Queue(nombreCola);

        DirectExchange exchange =
                new DirectExchange(nombreExchange);

        Binding binding =
                BindingBuilder
                        .bind(queue)
                        .to(exchange)
                        .with(routingKey);

        amqpAdmin.declareBinding(binding);
    }

}
