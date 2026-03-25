
package com.horizontes.model;

import java.math.BigDecimal;
import java.util.Date;

public class Pago {
    private int id;
    private int reservacionId;
    private BigDecimal monto;
    private int metodo;
    private Date fecha;

    public Pago() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReservacionId() { return reservacionId; }
    public void setReservacionId(int reservacionId) { this.reservacionId = reservacionId; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public int getMetodo() { return metodo; }
    public void setMetodo(int metodo) { this.metodo = metodo; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
}