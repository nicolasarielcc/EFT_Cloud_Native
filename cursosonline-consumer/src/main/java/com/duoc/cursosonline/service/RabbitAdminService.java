package com.duoc.cursosonline.service;

public interface  RabbitAdminService {

    void crearCola(String nombreCola);

    void eliminarCola(String nombreCola);

    void crearExchange(String nombreExchange);

    void eliminarExchange(String nombreExchange);

    void crearBinding(String nombreCola,
                      String nombreExchange,
                      String routingKey);
}
