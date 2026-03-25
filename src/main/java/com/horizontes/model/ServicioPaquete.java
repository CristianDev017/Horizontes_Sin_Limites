
package com.horizontes.model;

import java.math.BigDecimal;

public class ServicioPaquete {
    private int id;
    private int paqueteId;
    private int proveedorId;
    private String proveedorNombre;
    private String descripcion;
    private BigDecimal costo;

    public ServicioPaquete() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPaqueteId() { return paqueteId; }
    public void setPaqueteId(int paqueteId) { this.paqueteId = paqueteId; }

    public int getProveedorId() { return proveedorId; }
    public void setProveedorId(int proveedorId) { this.proveedorId = proveedorId; }

    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }
}