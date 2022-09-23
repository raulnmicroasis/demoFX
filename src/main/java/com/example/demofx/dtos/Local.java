package com.example.demofx.dtos;


public class Local {


    private Long id;


    private Boolean activo;


    private Long idempresa;


    private Direccion direccion;


    private String codPais;


    private String codPostal;


    private String nombreLocal;


    private String nombreEmpresa;


    private String nif;


    private String cae;

    private long idOfiGestora;

    private String ofiGestora;


    private String caeOfiGestora;


    private String garantiaOfiGestora;


    private String codigoFirma;

    private String numInstlCcaa;


    private String status;


    private String nifPais;


    private String numgarantia;

    private String codigo;


    public Local() {
        setDireccion(new Direccion());
    }

    public long getIdOfiGestora() {
        return idOfiGestora;
    }

    public void setIdOfiGestora(long idOfiGestora) {
        this.idOfiGestora = idOfiGestora;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNumgarantia() {
        return numgarantia;
    }

    public void setNumgarantia(String numgarantia) {
        this.numgarantia = numgarantia;
    }

    public String getNifPais() {
        return nifPais;
    }

    public void setNifPais(String nifPais) {
        this.nifPais = nifPais;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNumInstlCcaa() {
        return numInstlCcaa;
    }

    public void setNumInstlCcaa(String numInstlCcaa) {
        this.numInstlCcaa = numInstlCcaa;
    }

    public String getCodigoFirma() {
        return codigoFirma;
    }

    public void setCodigoFirma(String codigoFirma) {
        this.codigoFirma = codigoFirma;
    }

    public String getGarantiaOfiGestora() {
        return garantiaOfiGestora;
    }

    public void setGarantiaOfiGestora(String garantiaOfiGestora) {
        this.garantiaOfiGestora = garantiaOfiGestora;
    }

    public String getCaeOfiGestora() {
        return caeOfiGestora;
    }

    public void setCaeOfiGestora(String caeOfiGestora) {
        this.caeOfiGestora = caeOfiGestora;
    }

    public String getOfiGestora() {
        return ofiGestora;
    }

    public void setOfiGestora(String ofiGestora) {
        this.ofiGestora = ofiGestora;
    }

    public String getCae() {
        return cae;
    }

    public void setCae(String cae) {
        this.cae = cae;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getNombreLocal() {
        return nombreLocal;
    }

    public void setNombreLocal(String nombreLocal) {
        this.nombreLocal = nombreLocal;
    }

    public String getCodPostal() {
        return codPostal;
    }

    public void setCodPostal(String codPostal) {
        this.codPostal = codPostal;
    }

    public String getCodPais() {
        return codPais;
    }

    public void setCodPais(String codPais) {
        this.codPais = codPais;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdempresa() {
        return idempresa;
    }

    public void setIdempresa(Long idempresa) {
        this.idempresa = idempresa;
    }

    @Override
    public String toString() {
        return "Local{" +
                "id=" + id +
                ", activo=" + activo +
                ", idempresa=" + idempresa +
                ", direccion='" + direccion + '\'' +
                ", codPais='" + codPais + '\'' +
                ", codPostal='" + codPostal + '\'' +
                ", nombreLocal='" + nombreLocal + '\'' +
                ", nombreEmpresa='" + nombreEmpresa + '\'' +
                ", nif='" + nif + '\'' +
                ", cae='" + cae + '\'' +
                ", ofiGestora='" + ofiGestora + '\'' +
                ", caeOfiGestora='" + caeOfiGestora + '\'' +
                ", garantiaOfiGestora='" + garantiaOfiGestora + '\'' +
                ", codigoFirma='" + codigoFirma + '\'' +
                ", numInstlCcaa='" + numInstlCcaa + '\'' +
                ", status='" + status + '\'' +
                ", nifPais='" + nifPais + '\'' +
                ", numgarantia='" + numgarantia + '\'' +
                ", codigo='" + codigo + '\'' +
                '}';
    }
}