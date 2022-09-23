package com.example.demofx.dtos;

import java.time.LocalDate;

public class Factura {

    private Long id;

    private Cliente cliente;

    private PuntoSuministro puntoSuministro;

    private String numfactura;

    private Integer anocontable;

    private LocalDate fechaemision;

    private Double total;

    private Albaran albaran;

    private int tipofactura;

    public Factura() {
        albaran = new Albaran();
        cliente = new Cliente();
        puntoSuministro = new PuntoSuministro();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumfactura() {
        return numfactura;
    }

    public void setNumfactura(String numfactura) {
        this.numfactura = numfactura;
    }

    public Integer getAnocontable() {
        return anocontable;
    }

    public void setAnocontable(Integer anocontable) {
        this.anocontable = anocontable;
    }

    public LocalDate getFechaemision() {
        return fechaemision;
    }

    public void setFechaemision(LocalDate fechaemision) {
        this.fechaemision = fechaemision;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Albaran getAlbaran() {
        return albaran;
    }

    public void setAlbaran(Albaran albaran) {
        this.albaran = albaran;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public PuntoSuministro getPuntoSuministro() {
        return puntoSuministro;
    }

    public void setPuntoSuministro(PuntoSuministro puntoSuministro) {
        this.puntoSuministro = puntoSuministro;
    }

    public int getTipofactura() {
        return tipofactura;
    }

    public void setTipofactura(int tipofactura) {
        this.tipofactura = tipofactura;
    }

    @Override
    public String toString() {
        return "Factura{" +
                "id=" + id +
                ", cliente=" + cliente +
                ", puntoSuministro=" + puntoSuministro +
                ", numfactura='" + numfactura + '\'' +
                ", anocontable=" + anocontable +
                ", fechaemision=" + fechaemision +
                ", total=" + total +
                ", albaran=" + albaran.getFecha() +
                '}';
    }
}
