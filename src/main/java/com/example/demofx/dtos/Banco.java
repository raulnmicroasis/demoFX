package com.example.demofx.dtos;

import java.time.LocalDate;

public class Banco {

    private String denominacion;

    private String iban;

    private String cod_entidad;

    private String cod_sucursal;

    private String dc;

    private String numero_cuenta;

    private String bic;

    private String cuenta_contable;

    private LocalDate fechaautoriza_core;

    private String refmandato_core;

    private String refmandato_b2b;

    private String observacion;

    private int status;

    private Direccion direccion;

    private String cuentacontable;

    private String cuentariesgo;

    public Banco() {
        status = 1;
        observacion = "";
        direccion = new Direccion();
    }

    public String getCuentariesgo() {
        return cuentariesgo;
    }

    public void setCuentariesgo(String cuentariesgo) {
        this.cuentariesgo = cuentariesgo;
    }

    public String getCuentacontable() {
        return cuentacontable;
    }

    public void setCuentacontable(String cuentacontable) {
        this.cuentacontable = cuentacontable;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getCod_entidad() {
        return cod_entidad;
    }

    public void setCod_entidad(String cod_entidad) {
        this.cod_entidad = cod_entidad;
    }

    public String getCod_sucursal() {
        return cod_sucursal;
    }

    public void setCod_sucursal(String cod_sucursal) {
        this.cod_sucursal = cod_sucursal;
    }

    public String getDc() {
        return dc;
    }

    public void setDc(String dc) {
        this.dc = dc;
    }

    public String getNumero_cuenta() {
        return numero_cuenta;
    }

    public void setNumero_cuenta(String numero_cuenta) {
        this.numero_cuenta = numero_cuenta;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getCuenta_contable() {
        return cuenta_contable;
    }

    public void setCuenta_contable(String cuenta_contable) {
        this.cuenta_contable = cuenta_contable;
    }

    public String getRefmandato_core() {
        return refmandato_core;
    }

    public void setRefmandato_core(String refmandato_core) {
        this.refmandato_core = refmandato_core;
    }

    public String getRefmandato_b2b() {
        return refmandato_b2b;
    }

    public void setRefmandato_b2b(String refmandato_b2b) {
        this.refmandato_b2b = refmandato_b2b;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDate getFechaautoriza_core() {
        return fechaautoriza_core;
    }

    public void setFechaautoriza_core(LocalDate fechaautoriza_core) {
        this.fechaautoriza_core = fechaautoriza_core;
    }
}
