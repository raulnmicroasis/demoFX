package com.example.demofx.dtos;

import java.time.LocalDate;

public class VentaRuta {

    private long id;

    private String partida;

    private String reintro;

    private double litros_dev;

    private double litros15_dev;

    private String numalbaran;

    private String nota_entrega;

    private String matricula;

    private String cod_articulo;

    private String cre;

    private int compartimento;

    private boolean bonificacion;

    private double litros_carga;

    private double litros15_carga;

    private LocalDate fecha;

    private int secuencia;

    public VentaRuta() {
    }

    public double getLitros_dev() {
        return litros_dev;
    }

    public void setLitros_dev(double litros_dev) {
        this.litros_dev = litros_dev;
    }

    public double getLitros15_dev() {
        return litros15_dev;
    }

    public void setLitros15_dev(double litros15_dev) {
        this.litros15_dev = litros15_dev;
    }

    public int getCompartimento() {
        return compartimento;
    }

    public void setCompartimento(int numcompartimento) {
        this.compartimento = numcompartimento;
    }

    public VentaRuta(String reintro) {
        this.reintro = reintro;
    }

    public int getSecuencia() {
        return secuencia;
    }

    public void setSecuencia(int secuencia) {
        this.secuencia = secuencia;
    }

    public String getCre() {
        return cre;
    }

    public void setCre(String cre) {
        this.cre = cre;
    }

    public double getLitros_carga() {
        return litros_carga;
    }

    public void setLitros_carga(double litros_carga) {
        this.litros_carga = litros_carga;
    }

    public double getLitros15_carga() {
        return litros15_carga;
    }

    public void setLitros15_carga(double litros15_carga) {
        this.litros15_carga = litros15_carga;
    }

    public String getCod_articulo() {
        return cod_articulo;
    }

    public void setCod_articulo(String cod_articulo) {
        this.cod_articulo = cod_articulo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isBonificacion() {
        return bonificacion;
    }

    public void setBonificacion(boolean bonificacion) {
        this.bonificacion = bonificacion;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getPartida() {
        return partida;
    }

    public void setPartida(String partida) {
        this.partida = partida;
    }

    public String getReintro() {
        return reintro;
    }

    public void setReintro(String reintro) {
        this.reintro = reintro;
    }

    public String getNumalbaran() {
        return numalbaran;
    }

    public void setNumalbaran(String numalbaran) {
        this.numalbaran = numalbaran;
    }

    public String getNota_entrega() {
        return nota_entrega;
    }

    public void setNota_entrega(String nota_entrega) {
        this.nota_entrega = nota_entrega;
    }
}
