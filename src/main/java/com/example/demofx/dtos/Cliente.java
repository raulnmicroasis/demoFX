package com.example.demofx.dtos;

import java.util.Date;

public class Cliente {

    private Long id;

    private Long idempresa;

    private Boolean activo;

    private Boolean bloqueado;

    private String codigo;

    private String nombre_comercial;

    private String razonsocial;

    private String nombre_completo;

    private String nif;

    private String tlf1;

    private String tlf2;

    private String tlf3;

    private Long formapago;

    private Long ivaTipo;

    private Long ivaRegimen;

    private String status;

    private Integer tipoPersona;

    private Integer nif_pais;

    private String cuentacontable;

    private Direccion direccion;

    private Long estadoautorizargpd;

    private Boolean consientergpd;

    private String comarca;

    private Date fechaAlta;

    private Date fecha_modificacion;

    private Banco banco;

    private String observacion;

    private int vencimiento;

    private int numero_vencimientos;

    private int tipo_formapago;

    private String observacionPuntoSuministro;

    private Integer diapago1;

    private Integer diapago2;

    public Cliente(){
        direccion = new Direccion();
        banco = new Banco();
        observacion = "Cliente proveniente de la migración. ";
    }

    public Integer getDiapago1() {
        return diapago1;
    }

    public void setDiapago1(Integer diapago1) {
        this.diapago1 = diapago1;
    }

    public Integer getDiapago2() {
        return diapago2;
    }

    public void setDiapago2(Integer diapago2) {
        this.diapago2 = diapago2;
    }

    public String getObservacionPuntoSuministro() {
        return observacionPuntoSuministro;
    }

    public void setObservacionPuntoSuministro(String observacionPuntoSuministro) {
        this.observacionPuntoSuministro = observacionPuntoSuministro;
    }

    public int getTipo_formapago() {
        return tipo_formapago;
    }

    public void setTipo_formapago(int tipo_formapago) {
        this.tipo_formapago = tipo_formapago;
    }

    public int getVencimiento() {
        return vencimiento;
    }

    public void setVencimiento(int vencimiento) {
        this.vencimiento = vencimiento;
    }

    public int getNumero_vencimientos() {
        return numero_vencimientos;
    }

    public void setNumero_vencimientos(int numero_vencimientos) {
        this.numero_vencimientos = numero_vencimientos;
    }

    public Integer getNif_pais() {
        return nif_pais;
    }

    public void setNif_pais(Integer nif_pais) {
        this.nif_pais = nif_pais;
    }

    public String getCuentacontable() {
        return cuentacontable;
    }

    public void setCuentacontable(String cuentacontable) {
        this.cuentacontable = cuentacontable;
    }

    public Boolean getConsientergpd() {
        return consientergpd;
    }

    public String getNombre_comercial() {
        return nombre_comercial;
    }

    public void setNombre_comercial(String nombre_comercial) {
        this.nombre_comercial = nombre_comercial;
    }

    public String getNombre_completo() {
        return nombre_completo;
    }

    public void setNombre_completo(String nombre_completo) {
        this.nombre_completo = nombre_completo;
    }

    public void setConsientergpd(Boolean consientergpd) {
        this.consientergpd = consientergpd;
    }

    public Date getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public Long getIvaTipo() {
        return ivaTipo;
    }

    public void setIvaTipo(Long ivaTipo) {
        this.ivaTipo = ivaTipo;
    }

    public Long getIvaRegimen() {
        return ivaRegimen;
    }

    public void setIvaRegimen(Long ivaRegimen) {
        this.ivaRegimen = ivaRegimen;
    }

    public String getComarca() {
        return comarca;
    }

    public void setComarca(String comarca) {
        this.comarca = comarca;
    }

    public Long getEstadoautorizargpd() {
        return estadoautorizargpd;
    }

    public void setEstadoautorizargpd(Long estadoautorizargpd) {
        this.estadoautorizargpd = estadoautorizargpd;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }

    public Integer getTipoPersona() {
        return tipoPersona;
    }

    public void setTipoPersona(Integer tipoPersona) {
        this.tipoPersona = tipoPersona;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getFormapago() {
        return formapago;
    }

    public void setFormapago(Long formapago) {
        this.formapago = formapago;
    }

    public String getTlf1() {
        return tlf1;
    }

    public void setTlf1(String tlf1) {
        this.tlf1 = tlf1;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getRazonsocial() {
        return razonsocial;
    }

    public void setRazonsocial(String razonsocial) {
        this.razonsocial = razonsocial;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Boolean getBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(Boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Long getIdempresa() {
        return idempresa;
    }

    public void setIdempresa(Long idempresa) {
        this.idempresa = idempresa;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getFecha_modificacion() {
        return fecha_modificacion;
    }

    public void setFecha_modificacion(Date fecha_modificacion) {
        this.fecha_modificacion = fecha_modificacion;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Banco getBanco() {
        return banco;
    }

    public void setBanco(Banco banco) {
        this.banco = banco;
    }

    public String getTlf2() {
        return tlf2;
    }

    public void setTlf2(String tlf2) {
        this.tlf2 = tlf2;
    }

    public String getTlf3() {
        return tlf3;
    }

    public void setTlf3(String tlf3) {
        this.tlf3 = tlf3;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", idempresa=" + idempresa +
                ", activo=" + activo +
                ", bloqueado=" + bloqueado +
                ", codigo='" + codigo + '\'' +
                ", nombre_comercial='" + nombre_comercial + '\'' +
                ", razonsocial='" + razonsocial + '\'' +
                ", nombre_completo='" + nombre_completo + '\'' +
                ", nif='" + nif + '\'' +
                ", tlf1='" + tlf1 + '\'' +
                ", tlf2='" + tlf2 + '\'' +
                ", tlf3='" + tlf3 + '\'' +
                ", formapago=" + formapago +
                ", ivaTipo=" + ivaTipo +
                ", ivaRegimen=" + ivaRegimen +
                ", status='" + status + '\'' +
                ", tipoPersona=" + tipoPersona +
                ", nif_pais=" + nif_pais +
                ", cuentacontable='" + cuentacontable + '\'' +
                ", direccion=" + direccion +
                ", estadoautorizargpd=" + estadoautorizargpd +
                ", consientergpd=" + consientergpd +
                ", comarca='" + comarca + '\'' +
                ", fechaAlta=" + fechaAlta +
                ", fecha_modificacion=" + fecha_modificacion +
                '}';
    }


}