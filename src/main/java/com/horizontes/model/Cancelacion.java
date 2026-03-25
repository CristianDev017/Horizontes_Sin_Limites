
package com.horizontes.model;

import java.math.BigDecimal;
import java.util.Date;

public class Cancelacion {
    private int id;
    private int reservacionId;
    private Date fecha;
    private BigDecimal montoReembolso;
    private BigDecimal perdidaAgencia;

    public Cancelacion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReservacionId() { return reservacionId; }
    public void setReservacionId(int reservacionId) { this.reservacionId = reservacionId; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public BigDecimal getMontoReembolso() { return montoReembolso; }
    public void setMontoReembolso(BigDecimal montoReembolso) { this.montoReembolso = montoReembolso; }

    public BigDecimal getPerdidaAgencia() { return perdidaAgencia; }
    public void setPerdidaAgencia(BigDecimal perdidaAgencia) { this.perdidaAgencia = perdidaAgencia; }
}