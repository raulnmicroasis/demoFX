package com.example.demofx.dtos;

import java.time.LocalDate;

public class NotaEntrega {

    private LocalDate fechaentrega;

    private String camion;

    private String codigo;

    private String cod_cliente;

    private String cod_provedor;

    private String cod_articulo;

    private double litros;

    private double litros15;

    private String documento_compra;

    private String cae;

    private String cre;

    private boolean bonificado;

    private String cod_albaran;

    private long id_albaran;

    private String numalbaran_local;

    private String numalbaran_tesoreria;

    private int secuenciaA;

    private int secuenciaB;

    private int secuenciaC;

    public NotaEntrega() {
    }

    public String getNumalbaran_tesoreria() {
        return numalbaran_tesoreria;
    }

    public void setNumalbaran_tesoreria(String numalbaran_tesoreria) {
        this.numalbaran_tesoreria = numalbaran_tesoreria;
    }

    public String getNumalbaran_local() {
        return numalbaran_local;
    }

    public void setNumalbaran_local(String numalbaran_local) {
        this.numalbaran_local = numalbaran_local;
    }

    public long getId_albaran() {
        return id_albaran;
    }

    public void setId_albaran(long id_albaran) {
        this.id_albaran = id_albaran;
    }

    public int getSecuenciaA() {
        return secuenciaA;
    }

    public void setSecuenciaA(int secuenciaA) {
        this.secuenciaA = secuenciaA;
    }

    public int getSecuenciaB() {
        return secuenciaB;
    }

    public void setSecuenciaB(int secuenciaB) {
        this.secuenciaB = secuenciaB;
    }

    public int getSecuenciaC() {
        return secuenciaC;
    }

    public void setSecuenciaC(int secuenciaC) {
        this.secuenciaC = secuenciaC;
    }

    public String getCod_albaran() {
        return cod_albaran;
    }

    public void setCod_albaran(String cod_albaran) {
        this.cod_albaran = cod_albaran;
    }

    public boolean isBonificado() {
        return bonificado;
    }

    public void setBonificado(boolean bonificado) {
        this.bonificado = bonificado;
    }

    public LocalDate getFechaentrega() {
        return fechaentrega;
    }

    public void setFechaentrega(LocalDate fechaentrega) {
        this.fechaentrega = fechaentrega;
    }

    public String getCamion() {
        return camion;
    }

    public void setCamion(String camion) {
        this.camion = camion;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getCod_cliente() {
        return cod_cliente;
    }

    public void setCod_cliente(String cod_cliente) {
        this.cod_cliente = cod_cliente;
    }

    public String getCod_provedor() {
        return cod_provedor;
    }

    public void setCod_provedor(String cod_provedor) {
        this.cod_provedor = cod_provedor;
    }

    public String getCod_articulo() {
        return cod_articulo;
    }

    public void setCod_articulo(String cod_articulo) {
        this.cod_articulo = cod_articulo;
    }

    public double getLitros() {
        return litros;
    }

    public void setLitros(double litros) {
        this.litros = litros;
    }

    public double getLitros15() {
        return litros15;
    }

    public void setLitros15(double litros15) {
        this.litros15 = litros15;
    }

    public String getDocumento_compra() {
        return documento_compra;
    }

    public void setDocumento_compra(String documento_compra) {
        this.documento_compra = documento_compra;
    }

    public String getCae() {
        return cae;
    }

    public void setCae(String cae) {
        this.cae = cae;
    }

    public String getCre() {
        return cre;
    }

    public void setCre(String cre) {
        this.cre = cre;
    }
}
