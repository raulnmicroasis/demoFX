package com.example.demofx.dtos;

import java.time.LocalDate;

public class Compra {

    private LocalDate fecha;

    private String nota_entrega;

    private String cod_cliprov;

    private String cod_articulo;

    private double litros;

    private double litros_15;

    private String documento_compra;

    private String cae;

    private String cod_parque;

    public Compra() {
    }

    public String getCod_parque() {
        return cod_parque;
    }

    public void setCod_parque(String cod_parque) {
        this.cod_parque = cod_parque;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getNota_entrega() {
        return nota_entrega;
    }

    public void setNota_entrega(String nota_entrega) {
        this.nota_entrega = nota_entrega;
    }

    public String getCod_cliprov() {
        return cod_cliprov;
    }

    public void setCod_cliprov(String cod_cliprov) {
        this.cod_cliprov = cod_cliprov;
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

    public double getLitros_15() {
        return litros_15;
    }

    public void setLitros_15(double litros_15) {
        this.litros_15 = litros_15;
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
}
