package com.example.demofx.dtos;

import java.time.LocalDate;

public class Recibo {
    private LocalDate fecha;

    private int anocontable;

    private String numrecibo;

    private String codCliente;

    private String numfactura;

    private Double importe;

    public Recibo() {
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public int getAnocontable() {
        return anocontable;
    }

    public void setAnocontable(int anocontable) {
        this.anocontable = anocontable;
    }

    public String getNumrecibo() {
        return numrecibo;
    }

    public void setNumrecibo(String numrecibo) {
        this.numrecibo = numrecibo;
    }

    public String getCodCliente() {
        return codCliente;
    }

    public void setCodCliente(String codCliente) {
        this.codCliente = codCliente;
    }

    public String getNumfactura() {
        return numfactura;
    }

    public void setNumfactura(String numfactura) {
        this.numfactura = numfactura;
    }

    public Double getImporte() {
        return importe;
    }

    public void setImporte(Double importe) {
        this.importe = importe;
    }
}
