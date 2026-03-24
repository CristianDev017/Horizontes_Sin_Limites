
package com.horizontes.model;

import java.math.BigDecimal;
import java.util.List;

public class Paquete {
    private int id;
    private String nombre;
    private int destinoId;
    private String destinoNombre;
    private int duracionDias;
    private String descripcion;
    private BigDecimal precioVenta;
    private int capacidad;
    private boolean activo;
    private List<ServicioPaquete> servicios;

    public Paquete() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getDestinoId() { return destinoId; }
    public void setDestinoId(int destinoId) { this.destinoId = destinoId; }

    public String getDestinoNombre() { return destinoNombre; }
    public void setDestinoNombre(String destinoNombre) { this.destinoNombre = destinoNombre; }

    public int getDuracionDias() { return duracionDias; }
    public void setDuracionDias(int duracionDias) { this.duracionDias = duracionDias; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public List<ServicioPaquete> getServicios() { return servicios; }
    public void setServicios(List<ServicioPaquete> servicios) { this.servicios = servicios; }
}