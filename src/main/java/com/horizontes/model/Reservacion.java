
package com.horizontes.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Reservacion {
    private int id;
    private String numero;
    private Date fechaCreacion;
    private Date fechaViaje;
    private int paqueteId;
    private String paqueteNombre;
    private int agenteId;
    private String agenteNombre;
    private BigDecimal costoTotal;
    private String estado;
    private List<Cliente> pasajeros;

    public Reservacion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Date getFechaViaje() { return fechaViaje; }
    public void setFechaViaje(Date fechaViaje) { this.fechaViaje = fechaViaje; }

    public int getPaqueteId() { return paqueteId; }
    public void setPaqueteId(int paqueteId) { this.paqueteId = paqueteId; }

    public String getPaqueteNombre() { return paqueteNombre; }
    public void setPaqueteNombre(String paqueteNombre) { this.paqueteNombre = paqueteNombre; }

    public int getAgenteId() { return agenteId; }
    public void setAgenteId(int agenteId) { this.agenteId = agenteId; }

    public String getAgenteNombre() { return agenteNombre; }
    public void setAgenteNombre(String agenteNombre) { this.agenteNombre = agenteNombre; }

    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<Cliente> getPasajeros() { return pasajeros; }
    public void setPasajeros(List<Cliente> pasajeros) { this.pasajeros = pasajeros; }
}