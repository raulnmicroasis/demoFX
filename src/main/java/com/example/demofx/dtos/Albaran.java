package com.example.demofx.dtos;

import java.time.LocalDate;

public class Albaran {

    private String nombre_producto;

    private String cod_producto;

    private Long idfactura;

    private LocalDate fecha;

    private Integer tipoiva;

    private Integer iva;

    private Double canttempamb;

    private String numero;

    private Double precioUnidad;

    private Double descuento;

    public Albaran() {

    }

    public String getNombre_producto() {
        return nombre_producto;
    }

    public void setNombre_producto(String nombre_producto) {
        this.nombre_producto = nombre_producto;
    }

    public String getCod_producto() {
        return cod_producto;
    }

    public void setCod_producto(String producto) {
        this.cod_producto = producto;
    }

    public Long getIdfactura() {
        return idfactura;
    }

    public void setIdfactura(Long idfactura) {
        this.idfactura = idfactura;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getTipoiva() {
        return tipoiva;
    }

    public void setTipoiva(Integer tipoiva) {
        this.tipoiva = tipoiva;
    }

    public Double getCanttempamb() {
        return canttempamb;
    }

    public void setCanttempamb(Double canttempamb) {
        this.canttempamb = canttempamb;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getNumero() {
        return numero;
    }

    public void setPrecioUnidad(Double precioUnidad) {
        this.precioUnidad = precioUnidad;
    }

    public Double getPrecioUnidad() {
        return precioUnidad;
    }

    public Double getDescuento() {
        return descuento;
    }

    public void setDescuento(Double descuento) {
        this.descuento = descuento;
    }

    public Integer getIva() {
        return iva;
    }

    public void setIva(Integer iva) {
        this.iva = iva;
    }

    public Double getImporte() {
        return canttempamb * precioUnidad;
    }

    public Double getImporte_iva(){
        return getImporte() * iva;
    }
}
