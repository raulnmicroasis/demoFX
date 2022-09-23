package com.example.demofx.dtos;

public class ResumenIva {

    private String codigo_factura;

    private Integer anocontable;

    private String nif;

    private String destinatario;

    private Double base_imponible;

    private int iva_tipo;

    private String iva;

    private Double cuota;

    private Double total;

    public ResumenIva() {
    }

    public String getIva() {
        return iva;
    }

    public void setIva(String iva) {
        this.iva = iva;
    }

    public String getCodigo_factura() {
        return codigo_factura;
    }

    public void setCodigo_factura(String codigo_factura) {
        this.codigo_factura = codigo_factura;
    }

    public Integer getAnocontable() {
        return anocontable;
    }

    public void setAnocontable(Integer anocontable) {
        this.anocontable = anocontable;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public Double getBase_imponible() {
        return base_imponible;
    }

    public void setBase_imponible(Double base_imponible) {
        this.base_imponible = base_imponible;
    }

    public int getIva_tipo() {
        return iva_tipo;
    }

    public void setIva_tipo(int iva_tipo) {
        this.iva_tipo = iva_tipo;
    }

    public Double getCuota() {
        return cuota;
    }

    public void setCuota(Double cuota) {
        this.cuota = cuota;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
}
