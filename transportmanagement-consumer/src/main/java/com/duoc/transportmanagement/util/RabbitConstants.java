package com.duoc.transportmanagement.util;

public class RabbitConstants {
    public static final String GUIA_QUEUE = "guia.queue";
    public static final String GUIA_DLQ = "guia.dlq";
    public static final String GUIA_EXCHANGE = "guia.exchange";
    public static final String GUIA_DLX_EXCHANGE = "guia.dlx.exchange";

    public static final String TRANSPORTISTA_QUEUE = "transportista.queue";
    public static final String TRANSPORTISTA_DLQ = "transportista.dlq";
    public static final String TRANSPORTISTA_EXCHANGE = "transportista.exchange";
    public static final String TRANSPORTISTA_DLX_EXCHANGE = "transportista.dlx.exchange";

    private RabbitConstants() {}
}
