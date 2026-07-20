package com.duoc.cursosonline.util;

public class RabbitConstants {
    public static final String CURSO_QUEUE = "curso.queue";
    public static final String CURSO_DLQ = "curso.dlq";
    public static final String CURSO_EXCHANGE = "curso.exchange";
    public static final String CURSO_DLX_EXCHANGE = "curso.dlx.exchange";

    public static final String INSCRIPCION_QUEUE = "inscripcion.queue";
    public static final String INSCRIPCION_DLQ = "inscripcion.dlq";
    public static final String INSCRIPCION_EXCHANGE = "inscripcion.exchange";
    public static final String INSCRIPCION_DLX_EXCHANGE = "inscripcion.dlx.exchange";

    private RabbitConstants() {}
}
