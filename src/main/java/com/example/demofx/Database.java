package com.example.demofx;

import com.example.demofx.dtos.*;
import org.postgresql.util.PSQLException;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Database {

    public Connection connection;
    public Long idGrupo;
    public Long idEmpresa;
    public Long idLocal;
    public String grupo, empresa, local, codlocal;

    public HashMap<String, Integer> asientosNumeros;

    public HashMap<String, Integer> asientosOrden;

    // Posibilidad de crear otro método de conexión que reciba los datos de conexión
    public Connection conexionDB() {

        String url = "jdbc:postgresql://192.168.0.254:5432/test_maestros?user=postgres&password=Micro2019";
//        String url = "jdbc:postgresql://192.168.0.254:5432/test_maestros_demo?user=postgres&password=Micro2019";
//        String url = "jdbc:postgresql://192.168.0.200:5432/test_maestros?user=postgres&password=Micro2019";
//        String url = "jdbc:postgresql://192.168.0.200:5432/gasocentro_backup?user=postgres&password=Micro2019";
//        String url = "jdbc:postgresql://192.168.0.254:5433/gasocentro?user=postgres&password=Micro2019";

        try {
            // Conexión con la bbdd
            connection = DriverManager.getConnection(url);
            connection.setAutoCommit(false);
            // Comprobación de la conexión
            boolean conectado = connection.isValid(50000);
            System.out.println(conectado ? "TEST OK" : "TEST FAIL");

        } catch (PSQLException e) {
            e.printStackTrace();
            System.out.println("Error SQL " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public void cerrarConexionDb() {
        try {
            System.out.println("CIERRE CONEXIÓN");
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*public Direccion consultaCodigoPostalConObjeto(Direccion dir) {
        String codigoPostal = dir.getCod_postal();
        try {
            String query = "SELECT municipio, provincia, ccaa, pais FROM gasocentro.busca_direccion_vw WHERE codigo_postal = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, codigoPostal);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                dir.setMunicipio(resultSet.getString(1));
                dir.setProvincia(resultSet.getString(2));
                dir.setCcaa(resultSet.getString(3));
                dir.setPais(resultSet.getString(4));
            } else {
                dir.setMunicipio("Sin municipio");
                dir.setProvincia("Sin provincia");
                dir.setCcaa("Sin ccaa");
                dir.setPais("Sin país");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            dir.setMunicipio("Sin municipio");
            dir.setProvincia("Sin provincia");
            dir.setCcaa("Sin ccaa");
            dir.setPais("Sin pais");
        }
        return dir;
    }*/

    public boolean modificarCuentas(HashMap<String, HashMap<Integer, ArrayList<Object>>> hmFilas) throws SQLException {
        String queryProv = "SELECT id from gasocentro.proveedor WHERE nombrecomercial = ? and idempresa = ?";
        String updateProv = "UPDATE gasocentro.proveedor SET cuentacontable = ? WHERE id = ?";
        String queryArt = "SELECT id from gasocentro.articulo WHERE nombre = ? and idgrupo = ?";
        String updateArtVenta = "UPDATE gasocentro.articulo SET cta_venta = ? WHERE id = ?";
        String updateArtCompra = "UPDATE gasocentro.articulo SET cta_compra = ? WHERE id = ?";
        String key;

        for (Map.Entry<String, HashMap<Integer, ArrayList<Object>>> entry : hmFilas.entrySet()) {
            key = entry.getKey();
            switch (key) {
                case "40":
                    if (modifCuentaCliProv(queryProv, updateProv, entry)) return false;
                    break;
                case "70":
                    if (modifCuentaArt(queryArt, updateArtVenta, entry)) return false;
                    break;
                case "60":
                    if (modifCuentaArt(queryArt, updateArtCompra, entry)) return false;
                    break;
            }
        }
        return true;
    }

    private boolean modifCuentaArt(String queryArt, String updateArt, Map.Entry<String, HashMap<Integer, ArrayList<Object>>> entry) throws SQLException {
        ArrayList<Object> datos;
        String cuenta;
        String nombre;
        PreparedStatement statement;
        ResultSet resultSet;
        int idArt;
        int operacion;
        for (Map.Entry<Integer, ArrayList<Object>> entryTabla : entry.getValue().entrySet()) {
            datos = entryTabla.getValue();
            cuenta = String.valueOf(datos.get(0));
            nombre = String.valueOf(datos.get(1));
            nombre = nombre.replace("VENTAS ", "");
            nombre = nombre.replace("COMPRAS ", "");
            statement = connection.prepareStatement(queryArt);
            statement.setObject(1, nombre);
            statement.setObject(2, idGrupo);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                idArt = resultSet.getInt(1);
                statement = connection.prepareStatement(updateArt);
                statement.setString(1, cuenta);
                statement.setInt(2, idArt);
                operacion = statement.executeUpdate();
                statement.close();
                if (operacion == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean modifCuentaCliProv(String query, String update, Map.Entry<String, HashMap<Integer, ArrayList<Object>>> entry) throws SQLException {
        ArrayList<Object> datos;
        String cuenta;
        String nombre;
        PreparedStatement statement;
        ResultSet resultSet;
        int idCliente;
        int operacion;
        for (Map.Entry<Integer, ArrayList<Object>> entryTabla : entry.getValue().entrySet()) {
            datos = entryTabla.getValue();
            cuenta = String.valueOf(datos.get(0));
            nombre = String.valueOf(datos.get(1));
            statement = connection.prepareStatement(query);
            statement.setObject(1, nombre);
            statement.setLong(2, idEmpresa);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                idCliente = resultSet.getInt(1);
                statement = connection.prepareStatement(update);
                statement.setString(1, cuenta);
                statement.setInt(2, idCliente);
                operacion = statement.executeUpdate();
                statement.close();
                if (operacion == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public Direccion controlMunicipioCodigoPostalConObjeto(Direccion dir, String reformatCell) throws SQLException {

        String codigoPostal = "SinCodPostal", localidad = "Sin localidad";

        // Remplaza todos los guiones con espacios o no por un único espacio
        reformatCell = reformatCell.replaceAll("(\\s?-\\s?)", " ");

        // Comprobación si la celda viene vacía
        // Encuentra un código de 5 dígitos entre toda la cadena
        if (reformatCell.matches(".*\\d{5}.*")) {
            // Valida si está en decimal o no y lo sustituye
            if (reformatCell.matches("^\\d{5}\\.0")) {
                reformatCell = reformatCell.replace(".0", "");
            } else {
                // Si no está en decimal -> existe cadena con el municipio
                // Elimina el código para quedarse con el municipio
                localidad = reformatCell.replaceAll("(\\d{5}\\s?)", "");
            }
            // Elimina los caracteres no digitos para quedarse con el codigo
            codigoPostal = reformatCell.replaceAll("\\D", "");

        } else {
            // No encuentra código
            localidad = reformatCell;
        }

        if (codigoPostal.matches("\\d{5}")) {
            dir.setCod_postal(codigoPostal);

            String consultaCaPais = "SELECT municipio, municipio_codigo, ccaa, pais, provincia, pais_codigo FROM gasocentro.busca_direccion_vw WHERE codigo_postal = ?";
            PreparedStatement statement = connection.prepareStatement(consultaCaPais);
            statement.setString(1, codigoPostal);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                dir.setMunicipio(resultSet.getString(2) + " - " + resultSet.getString(1));
                dir.setCcaa(resultSet.getString(3));
                dir.setPais(resultSet.getString(6) + " - " + resultSet.getString(4));
                dir.setProvincia(resultSet.getString(5));
            } else {
                dir.setMunicipio("Sin municipio");
                dir.setCcaa("Sin comunidad");
                dir.setPais("Sin país");
                dir.setProvincia("Sin provincia");
            }

        } else {
            dir.setCod_postal("SinCodPostal");
            dir.setMunicipio("Sin municipio");
            dir.setCcaa("Sin comunidad");
            dir.setPais("Sin país");
        }

        dir.setLocalidad(localidad);

        return dir;
    }

    public Long query(String tabla, String campos, ArrayList<Object> datos) {
        StringBuilder valuesQuery = new StringBuilder("?");
        long id;
        valuesQuery.append(", ?".repeat(Math.max(0, datos.size() - 1)));
        try {
            String query = "INSERT INTO gasocentro." + tabla + " (" + campos + ") VALUES (" + valuesQuery + ")";
            System.out.println(query);
            PreparedStatement statement = connection.prepareStatement(query);
            for (int j = 0; j < datos.size(); j++) {
                statement.setObject(j + 1, datos.get(j));
            }
            statement.addBatch();
            statement.executeBatch();
            Statement statement1 = connection.createStatement();
            ResultSet resultSet = statement1.executeQuery("SELECT MAX(id) as id FROM gasocentro." + tabla);
            if (resultSet.next()) {
                id = resultSet.getLong("id");
            } else {
                id = -1L;
            }
            statement.close();
            statement1.close();
            resultSet.close();
            return id;
        } catch (SQLException e) {
            System.out.println("Error sql " + e.getMessage() + " " + e.getNextException());
            return -1L;
        }
    }

    public Camion consultaCamiones(Object matricula) {
        Camion camion = new Camion();
        try {
            String queryCamion = "SELECT fve.id, fve.codigo, fve.prov_transporte, fve.tipovehiculo, fve.marca, fve.modelo, fve.idproveedor, fve.matricula " +
                    "from gasocentro.flota_vehiculo fve where fve.matricula = ? and fve.idempresa = ?";
            PreparedStatement statement = connection.prepareStatement(queryCamion);
            statement.setObject(1, matricula);
            statement.setLong(2, idEmpresa);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                camion.setId(resultSet.getLong(1));
                camion.setCodigo(resultSet.getString(2));
                camion.setProveedor_trans(resultSet.getString(3));
                camion.setTipo(resultSet.getInt(4));
                camion.setMarca(resultSet.getString(5));
                camion.setModelo(resultSet.getString(6));
                camion.setIdProve(resultSet.getLong(7));
                camion.setMatricula(resultSet.getObject(8));
            }
            statement.close();
            resultSet.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            cerrarConexionDb();
        }
        return camion;
    }

    public boolean consultaAmbito(String grupo, String empresa, String local) {
        try {
            this.idGrupo = Long.parseLong(grupo);
            this.idEmpresa = Long.parseLong(empresa);
            this.idLocal = Long.parseLong(local);
            String queryGrupo = "SELECT descripcion FROM gasocentro.grupo WHERE id = ?";
            String queryEmpresa = "SELECT descripcion_comercial FROM gasocentro.empresa WHERE id = ? AND idgrupo = ?";
            String queryLocal = "SELECT nombre_local, codigo FROM gasocentro.local WHERE id = ? AND idempresa = ?";
            PreparedStatement statement = null;
            statement = connection.prepareStatement(queryGrupo);
            statement.setLong(1, idGrupo);
            int ids = 0;
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                this.grupo = resultSet.getString(1);
                statement = connection.prepareStatement(queryEmpresa);
                statement.setLong(1, idEmpresa);
                statement.setLong(2, idGrupo);
                resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    this.empresa = resultSet.getString(1);
                    statement = connection.prepareStatement(queryLocal);
                    statement.setLong(1, idLocal);
                    statement.setLong(2, idEmpresa);
                    resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        this.local = resultSet.getString(1);
                        this.codlocal = resultSet.getString(2);
                        return true;
                    }
                }
            }
            statement.close();
            resultSet.close();
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*public String generarCodigoCliente() {
        String query = "SELECT max(codigo) as cod FROM gasocentro.cliente WHERE idempresa = ?";
        int codigo = generarCodigoComun(query);
        if (codigo == -1) {
            return "000001";
        } else {
            return String.valueOf(codigo);
        }
    }*/

    /*public String generarCodigoProv(int cod) {
        String query = "SELECT max(codigo) as cod FROM gasocentro.proveedor WHERE idempresa = ?";
        int codigo = generarCodigoComun(query);
        return String.valueOf(codigo + cod);
    }*/

    private String generarCodigoSuperfamilia() {
        String query = "SELECT max(codigo) as cod FROM gasocentro.articulo_superfamilia WHERE idgrupo = ?";
        return getCodigo(idGrupo, query);
    }

    private String generarCodigoFamilia(Long idSuperfamilia) {
        String query = "SELECT max(codigo) as cod FROM gasocentro.articulo_superfamilia_familia WHERE idsuperfamilia = ?";
        return getCodigo(idSuperfamilia, query);
    }

    private String getCodigo(Long parametro, String query) {
        int codigo;
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, parametro);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            if (resultSet.getString(1) != null) {
                codigo = resultSet.getInt(1) + 1;
                return String.format("%4s", codigo).replace(" ", "0");
            } else {
                return "0001";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "0001";
        }
    }

    /*public int generarCodigoComun(String query) {
        int codigo;
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, idEmpresa);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            if (resultSet.getString(1) != null) {
                codigo = resultSet.getInt(1) + 1;
                return codigo;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }*/

    /*public void resetIds() {
        String resetCliente = "ALTER SEQUENCE gasocentro.\"Cliente_id_seq\" RESTART WITH ";
        String resetProveedor = "ALTER SEQUENCE gasocentro.proveedor_id_seq RESTART WITH ";
        String resetBanco = "ALTER SEQUENCE gasocentro.cliente_banco_id_seq RESTART WITH ";
        PreparedStatement statement;
        long id;
        try {
            id = selectMaxId("cliente");
            resetCliente += id;
            statement = connection.prepareStatement(resetCliente);
            statement.execute();
            id = selectMaxId("proveedor");
            resetProveedor += id;
            statement = connection.prepareStatement(resetProveedor);
            statement.execute();
            id = selectMaxId("cliente_banco");
            resetBanco += id;
            statement = connection.prepareStatement(resetBanco);
            statement.execute();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/

    public int consultaMaximoDigitosCuenta() throws Exception {
        String queryMaxDigitos = "SELECT maxdigitocuenta FROM gasocentro.empresa WHERE id = ?";
        int idMaxDigitos = 10;

            PreparedStatement statement = connection.prepareStatement(queryMaxDigitos);
            statement.setLong(1, idEmpresa);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                idMaxDigitos = resultSet.getInt(1);
            }
        return idMaxDigitos;
    }

    public ArrayList<Long> consultaFamilias() {
        String querySuperfamilia = "SELECT id FROM gasocentro.articulo_superfamilia WHERE descripcion = 'HIDROCARBUROS' AND idgrupo = ?";
        String queryFamilia = "SELECT id FROM gasocentro.articulo_superfamilia_familia WHERE descripcion = 'Liquidos inflamables' and idsuperfamilia = ?";
        String camposSuperfamilia = "idgrupo, activa, hidrocarburo, codigo, descripcion";
        String camposFamilia = "idsuperfamilia, activa, codigo, descripcion";

        PreparedStatement statement;
        ResultSet resultSet;
        Long idSuperfamilia = null, idFamilia = null;
        String codigoFamilia, codigoSuperfamilia;
        ArrayList<Object> datosFamilia, datosSuperfamilia;

        try {
            statement = connection.prepareStatement(querySuperfamilia);
            statement.setLong(1, idGrupo);
            resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                datosSuperfamilia = new ArrayList<>();
                datosSuperfamilia.add(idGrupo);
                datosSuperfamilia.add(true);
                datosSuperfamilia.add(true);
                codigoSuperfamilia = generarCodigoSuperfamilia();
                datosSuperfamilia.add(codigoSuperfamilia);
                datosSuperfamilia.add("HIDROCARBUROS");
                idSuperfamilia = query("articulo_superfamilia", camposSuperfamilia, datosSuperfamilia);
            } else {
                idSuperfamilia = resultSet.getLong(1);
            }

            statement = connection.prepareStatement(queryFamilia);
            statement.setLong(1, idSuperfamilia);
            resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                datosFamilia = new ArrayList<>();
                datosFamilia.add(idSuperfamilia);
                datosFamilia.add(true);
                codigoFamilia = generarCodigoFamilia(idSuperfamilia);
                datosFamilia.add(codigoFamilia);
                datosFamilia.add("Liquidos inflamables");
                idFamilia = query("articulo_superfamilia_familia", camposFamilia, datosFamilia);
            } else {
                idFamilia = resultSet.getLong(1);
            }
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<Long> ids = new ArrayList<>();
        ids.add(idSuperfamilia);
        ids.add(idFamilia);
        return ids;
    }

    public void dropTriggers(){
        String dropTriggers = "DROP TRIGGER IF EXISTS update_client_func ON gasocentro.cliente_banco;" +
                "DROP TRIGGER IF EXISTS update_client_func ON gasocentro.cliente_facturacion;" +
                "DROP TRIGGER IF EXISTS update_psuministro_func ON gasocentro.cliente_puntosuministro_banco;" +
                "DROP TRIGGER IF EXISTS update_psuministro_func ON gasocentro.cliente_puntosuministro_facturacion;" +
                "DROP TRIGGER IF EXISTS update_psuministro_func ON gasocentro.cliente_puntosuministro_finalidad;";

        try {
            connection.createStatement().execute(dropTriggers);
        } catch (SQLException e) {
            createTriggers();
            e.printStackTrace();
        }
    }

    public void createTriggers(){
        String createTriggers = "CREATE TRIGGER update_psuministro_func " +
                "AFTER INSERT OR DELETE OR UPDATE  " +
                "ON gasocentro.cliente_puntosuministro_banco " +
                "FOR EACH ROW " +
                "EXECUTE FUNCTION gasocentro.update_psuministro_func(); " +
                "CREATE TRIGGER update_client_func " +
                "AFTER INSERT OR DELETE OR UPDATE  " +
                "ON gasocentro.cliente_banco " +
                "FOR EACH ROW " +
                "EXECUTE FUNCTION gasocentro.update_client_func(); " +
                "" +
                "CREATE TRIGGER update_psuministro_func " +
                "AFTER INSERT OR DELETE OR UPDATE  " +
                "ON gasocentro.cliente_puntosuministro_facturacion " +
                "FOR EACH ROW " +
                "EXECUTE FUNCTION gasocentro.update_psuministro_func(); " +
                "CREATE TRIGGER update_client_func " +
                "AFTER INSERT OR DELETE OR UPDATE  " +
                "ON gasocentro.cliente_facturacion " +
                "FOR EACH ROW " +
                "EXECUTE FUNCTION gasocentro.update_client_func();" +
                "" +
                "CREATE TRIGGER update_psuministro_func " +
                "AFTER INSERT OR DELETE OR UPDATE " +
                "ON gasocentro.cliente_puntosuministro_finalidad " +
                "FOR EACH ROW " +
                "EXECUTE FUNCTION gasocentro.update_psuministro_func();";
        try {
            connection.createStatement().execute(createTriggers);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ArrayList<String>> queryDiarioAplifisa() {
        conexionDB();
        asientosNumeros = new HashMap<String, Integer>();

        String query = "SELECT c.* from ( " +
                "SELECT  " +
                "asi.numasiento as numasiento,  " +
                "asi.apunte as orden,  " +
                "fact.fechaalta as fecha,  " +
                "case when asi.ctadebe is null then asi.ctahaber " +
                "when asi.ctahaber is null then asi.ctadebe end as subcuenta,  " +
                "asi.concepto,  " +
                "case when asi.importedebe is null then 0 " +
                "when asi.importedebe is not null then asi.importedebe end as debe,  " +
                "case when asi.importehaber is null then 0 " +
                "when asi.importehaber is not null then asi.importehaber end as haber  " +
                "from gasocentro.tesoreria_asiento asi inner join gasocentro.tesoreria_factura fact on asi.idfactura = fact.id " +
                "where fact.idlocal = ? " +
                " " +
                "UNION " +
                " " +
                "SELECT  " +
                "asi.numasiento as numasiento,  " +
                "asi.apunte as orden,  " +
                "remesa.fechaalta as fecha,  " +
                "case when asi.ctadebe is null then asi.ctahaber " +
                "when asi.ctahaber is null then asi.ctadebe end as subcuenta,  " +
                "asi.concepto,  " +
                "case when asi.importedebe is null then 0 " +
                "when asi.importedebe is not null then asi.importedebe end as debe,  " +
                "case when asi.importehaber is null then 0 " +
                "when asi.importehaber is not null then asi.importehaber end as haber  " +
                "from gasocentro.tesoreria_asiento asi inner join gasocentro.tesoreria_remesa remesa on asi.idremesa = remesa.id " +
                "where remesa.idlocal = ? " +
                " " +
                "UNION " +
                " " +
                "SELECT  " +
                "asi.numasiento as numasiento,  " +
                "asi.apunte as orden,  " +
                "recibo.fechacreacion as fecha,  " +
                "case when asi.ctadebe is null then asi.ctahaber " +
                "when asi.ctahaber is null then asi.ctadebe end as subcuenta,  " +
                "asi.concepto,  " +
                "case when asi.importedebe is null then 0 " +
                "when asi.importedebe is not null then asi.importedebe end as debe,  " +
                "case when asi.importehaber is null then 0 " +
                "when asi.importehaber is not null then asi.importehaber end as haber  " +
                "from gasocentro.tesoreria_asiento asi inner join gasocentro.tesoreria_factura_recibo recibo on asi.idrecibo = recibo.id " +
                "where recibo.idlocal = ?) as c where numasiento is not null and numasiento != '' order by numasiento, orden ";

        ArrayList<ArrayList<String>> datos = new ArrayList<>();


        try {
            String numasiento = "";
            int asiento = 0;

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, idLocal);
            statement.setLong(2, idLocal);
            statement.setLong(3, idLocal);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ArrayList<String> datosAsiento = new ArrayList<>();
                if (resultSet.getObject(1) == null) continue;
                if (!resultSet.getString(1).equals(numasiento)){
                    asiento++;
                    numasiento = resultSet.getString(1);
                    asientosNumeros.put(numasiento, asiento);
                }

                datosAsiento.add(String.valueOf(asiento));
                for (int i = 1; i < 8; i++) {
                    datosAsiento.add(String.valueOf(resultSet.getObject(i)));
                }
                datos.add(datosAsiento);
            }

            statement.close();
            resultSet.close();
            cerrarConexionDb();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return datos;
    }

    public ArrayList<ArrayList<String>> queryIVAAplifisa() {
        conexionDB();

        String query = "SELECT   " +
                "   asi.numasiento as asiento,   " +
                "   asi.apunte as orden,   " +
                "   fact.fechaalta as fecha,   " +
                "   asi.ctahaber as subcuenta,   " +
                "   fact.ctafacturacion as contrapartida,      " +
                "   case when fact.tipofactura = 1 then 'V'     " +
                "       when fact.tipofactura = 2 then 'C' end as ven_com,      " +
                "   fact.numfactura as documento,      " +
                "   fact.numfactura as factura,      " +
                "   iva.baseimponible as baseimp,      " +
                "   iva.importeiva as cuota,      " +
                "   replace(iva.tipoiva, '%', '') as iva,     " +
                "   /*baseret, cuotaret, */     " +
                "   fact.retencion as retencion,     " +
                "   case when fact.regiva = 1 then 'R'      " +
                "       when fact.regiva = 2 then 'O'     " +
                "       when fact.regiva = 3 or fact.regiva = 4 or fact.regiva = 5 then 'X'     " +
                "       when fact.regiva = 6  then 'E' end as tipofac,     " +
                "   case when fact.claveoperacion = 1 then 'N'     " +
                "       when fact.claveoperacion = 2 then 'S' end as rectificativa,     " +
                "   case when fact.regiva = 5 then 'S'      " +
                "       else 'N' end as fra_igic,      " +
                "   case when iva.idtipoiva = 4 then 'S'      " +
                "       else 'N' end as exen,   " +
                "   asi.concepto as descripcion_sii,   " +
                "   case when fact.total >= 100000000 then 'S'     " +
                "       else 'N' end as framacrodato_sii     " +
                "   from gasocentro.tesoreria_factura_iva iva  " +
                "       inner join gasocentro.tesoreria_factura fact on iva.idfactura = fact.id      " +
                "       inner join gasocentro.tesoreria_asiento asi on asi.idfactura = fact.id and asi.tipoasiento = 3 and asi.concepto <> 'Importe bonificado' " +
                "       inner join gasocentro.articulo art on art.cta_iva_repercutido = asi.ctahaber and art.status='ACTIVE' and art.idgrupo = ? and art.iva_tipo = iva.idtipoiva " +
                "   where fact.idlocal = ? and numasiento is not null and numasiento != ''" +
                "   order by asiento, orden";

        ArrayList<ArrayList<String>> datos = new ArrayList<>();

        try {

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, idGrupo);
            statement.setLong(2, idLocal);
            ResultSet resultSet = statement.executeQuery();
            String asiento;

            while (resultSet.next()) {
                ArrayList<String> datosAsiento = new ArrayList<>();
                asiento = String.valueOf(asientosNumeros.get(resultSet.getString(1)));
                datosAsiento.add(asiento);

                for (int i = 2; i < 19; i++) {
                    datosAsiento.add(String.valueOf(resultSet.getObject(i)));
                }
                datos.add(datosAsiento);
            }

            statement.close();
            resultSet.close();
            cerrarConexionDb();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return datos;
    }

    public ArrayList<ArrayList<String>> queryRetencionAplifisa() {
        conexionDB();

        String query = "SELECT " +
                "iva.idfactura as idfactura,  " +
                "fact.fechaalta as fecha, " +
                "asi.ctahaber as subcuenta,  " +
                "fact.ctacontablecliente as contrapartida,  " +
                "case when fact.tipofactura = 1 then 'V' " +
                "when fact.tipofactura = 2 then 'C' end as ven_com,  " +
                "fact.numfactura as documento,  " +
                "fact.numfactura as factura,  " +
                "iva.baseimponible as baseimp,  " +
                "iva.importeiva as cuota,  " +
                "replace(iva.tipoiva, '%', '') as iva, " +
                "/*baseret, cuotaret, */ " +
                "fact.retencion as retencion, " +
                "case when fact.regiva = 1 then 'R'  " +
                "when fact.regiva = 2 then 'O' " +
                "when fact.regiva = 3 or fact.regiva = 4 or fact.regiva = 5 then 'X' " +
                "when fact.regiva = 6  then 'E' end as tipofac, " +
                "case when claveoperacion = 1 then 'N' " +
                "when claveoperacion = 2 then 'S' end as rectificativa, " +
                "case when fact.regiva = 5 then 'S'  " +
                "else 'N' end as fra_igic,  " +
                "case when iva.idtipoiva = 4 then 'S'  " +
                "else 'N' end as exen,  " +
                "asi.concepto as descripcion_sii,  " +
                "case when fact.total >= 100000000 then 'S' " +
                "else 'N' end as framacrodato_sii " +
                "from gasocentro.tesoreria_factura_iva iva inner join gasocentro.tesoreria_factura fact on iva.idfactura = fact.id " +
                "inner join gasocentro.tesoreria_asiento asi on asi.idfactura = fact.id " +
                "where (substring (asi.ctahaber from 1 for 3) = '477' or substring (asi.ctahaber from 1 for 3) = '472') and  " +
                "fact.idlocal = ? order by idfactura;";

        ArrayList<ArrayList<String>> datos = new ArrayList<>();


        try {
            long idfactura = 0;
            int asiento = 0;
            int orden = 0;

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, idLocal);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ArrayList<String> datosAsiento = new ArrayList<>();
                if (resultSet.getLong(1) != idfactura){
                    asiento++;
                    orden = 1;
                    idfactura = resultSet.getLong(1);
                } else {
                    orden++;
                }
                datosAsiento.add(String.valueOf(asiento));
                datosAsiento.add(String.valueOf(orden));
                for (int i = 2; i < 18; i++) {
                    datosAsiento.add(String.valueOf(resultSet.getObject(i)));
                }
                datos.add(datosAsiento);
            }

            statement.close();
            resultSet.close();
            cerrarConexionDb();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return datos;
    }

    public ArrayList<ArrayList<String>> querySubcuentasAplifisa() {
        conexionDB();

        String query = "select distinct a.* from (   " +
                "                select case when asi.ctadebe is null then asi.ctahaber when asi.ctahaber is null then asi.ctadebe end as subcuenta,   " +
                "                    case when substring(asi.ctahaber from 1 for 2) = '33' or substring(asi.ctadebe from 1 for 2) = '33' then 'PRODUCTOS EN CURSO (VTOS.)'   " +
                "                        when substring(asi.ctahaber from 1 for 2) = '22' or substring(asi.ctadebe from 1 for 2) = '22' then 'INMOVILIZACIONES MATERIALES (VTOS.)'   " +
                "                        when substring(asi.ctahaber from 1 for 3) = '572' or substring(asi.ctadebe from 1 for 3) = '572' " +
                "       or substring(asi.ctahaber from 1 for 3) = '577' or substring(asi.ctadebe from 1 for 3) = '577' then 'BANCO (REMESA)' " +
                "      when substring(asi.ctahaber from 1 for 3) = '555' or substring(asi.ctadebe from 1 for 3) = '555' then 'CUENTAS NO BANCARIAS (REMESA)' " +
                "      when substring(asi.ctahaber from 1 for 3) = '520' or substring(asi.ctadebe from 1 for 3) = '520' then 'CAJA (REMESA)' " +
                "      when substring(asi.ctahaber from 1 for 2) = '11' or substring(asi.ctadebe from 1 for 2) = '11' then 'RESERVAS (VTOS.)' " +
                "      when substring(asi.ctahaber from 1 for 3) = '431' or substring(asi.ctadebe from 1 for 3) = '431' then 'DEUDORES (VTOS.)' " +
                "     else asi.concepto end as conceptoo,   " +
                "                    '' as porciva,   " +
                "                    '' as tipoiva,   " +
                "                    '' as porcrec,   " +
                "                    '' as cuenrec,   " +
                "                    '' as cif,   " +
                "                    '' as dir,   " +
                "                    '' as cod_pos,   " +
                "                    '' as prov,   " +
                "                    '' as loc,   " +
                "                    '' as telefono,   " +
                "                    '' as fax,   " +
                "                    '' as web,   " +
                "                    '' as correo,   " +
                "                    1 as clave_id,   " +
                "                    '' as nif_intra1,   " +
                "                    '' as nif_intra2,   " +
                "                    '' as nif_intra3,   " +
                "                    'N' as fradeduparcial,   " +
                "                    '' as fradeduporc   " +
                "                    from gasocentro.tesoreria_asiento asi left join gasocentro.tesoreria_factura fact on fact.id = asi.idfactura   " +
                "                        left join gasocentro.tesoreria_factura_recibo recibo on recibo.id = asi.idrecibo   " +
                "                        left join gasocentro.tesoreria_remesa remesa on remesa.id = asi.idremesa   " +
                "                    where case when asi.ctadebe is null then (substring (asi.ctahaber from 1 for 3) <> '430' and substring (asi.ctahaber from 1 for 3) <> '472'   " +
                "                            and substring (asi.ctahaber from 1 for 3) <> '400' and substring (asi.ctahaber from 1 for 3) <> '477')   " +
                "                        when asi.ctahaber is null then (substring (asi.ctadebe from 1 for 3) <> '430' and substring (asi.ctadebe from 1 for 3) <> '472'   " +
                "                            and substring (asi.ctadebe from 1 for 3) <> '400' and substring (asi.ctadebe from 1 for 3) <> '477') end   " +
                "                        and (fact.idempresa = ? or recibo.idempresa = ? or remesa.idempresa = ?) and remesa.estado = 4   " +
                "                    group by subcuenta, conceptoo   " +
                "                       " +
                "                union   " +
                "                   " +
                "                select asi.ctahaber as subcuenta,   " +
                "                    asi.concepto as concepto,   " +
                "                    case when (select iva_tipo from gasocentro.articulo where cta_iva_repercutido = asi.ctahaber and idgrupo = ? and status = 'ACTIVE' limit 1) = 1 then '21'   " +
                "                        when (select iva_tipo from gasocentro.articulo where cta_iva_repercutido = asi.ctahaber and idgrupo = ? and status = 'ACTIVE' limit 1) = 2 then '10'   " +
                "                        when (select iva_tipo from gasocentro.articulo where cta_iva_repercutido = asi.ctahaber and idgrupo = ? and status = 'ACTIVE' limit 1) = 3 then '4'    " +
                "                        when (select iva_tipo from gasocentro.articulo where cta_iva_repercutido = asi.ctahaber and idgrupo = ? and status = 'ACTIVE' limit 1) = 4 then 'EXENTO' end as porciva,   " +
                "                    case when fact.regiva = 1 then 'R'   " +
                "                        when fact.regiva = 2 then 'O'   " +
                "                        when fact.regiva = 3 or fact.regiva = 4 or fact.regiva = 5 then 'X'   " +
                "                        when fact.regiva = 6 then 'E' end as tipoiva,   " +
                "                    '' as porcrec,   " +
                "                    '' as cuenrec,   " +
                "                    '' as cif,   " +
                "                    '' as dir,   " +
                "                    '' as cod_pos,   " +
                "                    '' as prov,   " +
                "                    '' as loc,   " +
                "                    '' as telefono,   " +
                "                    '' as fax,   " +
                "                    '' as web,   " +
                "                    '' as correo,   " +
                "                    1 as clave_id,   " +
                "                    '' as nif_intra1,   " +
                "                    '' as nif_intra2,   " +
                "                    '' as nif_intra3,   " +
                "                    'N' as fradeduparcial,   " +
                "                    '' as fradeduporc   " +
                "                    from gasocentro.tesoreria_asiento asi left join gasocentro.tesoreria_factura fact on fact.id = asi.idfactura   " +
                "                        left join gasocentro.tesoreria_factura_recibo recibo on recibo.id = asi.idrecibo   " +
                "                        left join gasocentro.tesoreria_remesa remesa on remesa.id = asi.idremesa   " +
                "                    where case when asi.ctadebe is null then (substring (asi.ctahaber from 1 for 3) = '472' or substring (asi.ctahaber from 1 for 3) = '477')   " +
                "                        when asi.ctahaber is null then (substring (asi.ctadebe from 1 for 3) = '472' or substring (asi.ctadebe from 1 for 3) = '477') end   " +
                "                        and (fact.idempresa = ? or recibo.idempresa = ? or remesa.idempresa = ?)   " +
                "                    group by asi.ctahaber, asi.concepto, fact.regiva   " +
                "                       " +
                "                union   " +
                "                       " +
                "                select fact.ctafacturacion as subcuenta,   " +
                "                    case when fact.facturacion_destino = 2 then fact.puntosuministro   " +
                "                        else fact.cliente end as concepto_subcuenta,   " +
                "                    '' as porciva,   " +
                "                    '' as tipoiva,   " +
                "                    '' as porcrec,   " +
                "                    '' as cuenrec,   " +
                "                    case when fact.facturacion_destino = 2 then fact.nifpuntosuministro   " +
                "                        else fact.nifcliente end as cif,   " +
                "                    case when fact.facturacion_destino = 2 then psum.direccion   " +
                "                        else cli.direccion end as dir,   " +
                "                    case when fact.facturacion_destino = 2 then psum.cod_postal   " +
                "                        else cli.cod_postal end as cod_pos,   " +
                "                    case when fact.facturacion_destino = 2 then psum.provincia   " +
                "                        else cli.provincia end as prov,   " +
                "                    case when fact.facturacion_destino = 2 then psum.localidad   " +
                "                        else cli.localidad end as loc,   " +
                "                    case when fact.facturacion_destino = 2 then psum.tlf1   " +
                "                        else cli.tlf1 end as telefono,   " +
                "                    '' as fax,   " +
                "                    cli.web as web,   " +
                "                    cli.email as correo,   " +
                "                    case when cli.nif_pais = 6 then 1   " +
                "                        else 2 end as clave_id,   " +
                "                    case when fact.facturacion_destino = 2 then   " +
                "                            case when cli.nif_pais <> 6 then   " +
                "                                case when cli.nif_pais is null then ''   " +
                "                                    when cli.nif_pais = 0 then ''   " +
                "                                    when cli.nif_pais = 1 then 'AT'   " +
                "                                    when cli.nif_pais = 2 then 'BE'   " +
                "                                    when cli.nif_pais = 3 then 'BG'   " +
                "                                    when cli.nif_pais = 4 then 'CY'   " +
                "                                    when cli.nif_pais = 5 then 'DK'   " +
                "                                    when cli.nif_pais = 7 then 'FR'   " +
                "                                    when cli.nif_pais = 8 then 'GR'   " +
                "                                    when cli.nif_pais = 9 then 'HU'   " +
                "                                    when cli.nif_pais = 10 then 'IT'   " +
                "                                    when cli.nif_pais = 11 then 'LU'   " +
                "                                    when cli.nif_pais = 12 then 'MT'   " +
                "                                    when cli.nif_pais = 13 then 'NL'   " +
                "                                    when cli.nif_pais = 14 then 'PL'   " +
                "                                    when cli.nif_pais = 15 then 'PT'   " +
                "                                    when cli.nif_pais = 17 then 'DE'   " +
                "                                    when cli.nif_pais = 18 then 'RO'   " +
                "                                    when cli.nif_pais = 19 then 'SE'   " +
                "                                    when cli.nif_pais = 20 then 'LV'   " +
                "                                    when cli.nif_pais = 21 then 'EE'   " +
                "                                    when cli.nif_pais = 22 then 'LT'   " +
                "                                    when cli.nif_pais = 23 then 'CZ'   " +
                "                                    when cli.nif_pais = 24 then 'SK'   " +
                "                                    when cli.nif_pais = 25 then 'HR'   " +
                "                                    when cli.nif_pais = 26 then 'SI'   " +
                "                                    when cli.nif_pais = 27 then 'FI'   " +
                "                                    when cli.nif_pais = 28 then 'IE' end   " +
                "                                else '' end   " +
                "                        else   " +
                "                            case when psum.nif_pais <> 6 then   " +
                "                                case when psum.nif_pais is null then ''   " +
                "                                    when psum.nif_pais = 0 then ''    " +
                "                                    when psum.nif_pais = 1 then 'AT'   " +
                "                                    when psum.nif_pais = 2 then 'BE'   " +
                "                                    when psum.nif_pais = 3 then 'BG'   " +
                "                                    when psum.nif_pais = 4 then 'CY'   " +
                "                                    when psum.nif_pais = 5 then 'DK'   " +
                "                                    when psum.nif_pais = 7 then 'FR'   " +
                "                                    when psum.nif_pais = 8 then 'GR'   " +
                "                                    when psum.nif_pais = 9 then 'HU'   " +
                "                                    when psum.nif_pais = 10 then 'IT'   " +
                "                                    when psum.nif_pais = 11 then 'LU'   " +
                "                                    when psum.nif_pais = 12 then 'MT'   " +
                "                                    when psum.nif_pais = 13 then 'NL'   " +
                "                                    when psum.nif_pais = 14 then 'PL'   " +
                "                                    when psum.nif_pais = 15 then 'PT'   " +
                "                                    when psum.nif_pais = 17 then 'DE'   " +
                "                                    when psum.nif_pais = 18 then 'RO'   " +
                "                                    when psum.nif_pais = 19 then 'SE'   " +
                "                                    when psum.nif_pais = 20 then 'LV'   " +
                "                                    when psum.nif_pais = 21 then 'EE'   " +
                "                                    when psum.nif_pais = 22 then 'LT'   " +
                "                                    when psum.nif_pais = 23 then 'CZ'   " +
                "                                    when psum.nif_pais = 24 then 'SK'   " +
                "                                    when psum.nif_pais = 25 then 'HR'   " +
                "                                    when psum.nif_pais = 26 then 'SI'   " +
                "                                    when psum.nif_pais = 27 then 'FI'   " +
                "                                    when psum.nif_pais = 28 then 'IE' end   " +
                "                                else '' end   " +
                "                    end as nif_intra1,   " +
                "                    case when fact.facturacion_destino = 2 then   " +
                "                            case when cli.nif_pais <> 6 then substring(cli.nif, 2)   " +
                "                                else '' end                " +
                "                        else   " +
                "                            case when psum.nif_pais <> 6 then substring(psum.nif, 2)   " +
                "                                else '' end   " +
                "                    end as nif_intra2,   " +
                "                    '' as nif_intra3,   " +
                "                    'N' as fradeduparcial,   " +
                "                    '' as fradeduporc   " +
                "                    from gasocentro.tesoreria_factura fact    " +
                "                        left join gasocentro.tesoreria_factura_recibo recibo on recibo.idfactura = fact.id   " +
                "                        inner join gasocentro.tesoreria_asiento asi on (asi.idfactura = fact.id or asi.idrecibo = recibo.id)   " +
                "                        inner join gasocentro.cliente cli on (fact.idcliente = cli.id or recibo.idcliente = cli.id)   " +
                "                        inner join gasocentro.cliente_puntosuministro psum on (fact.idpuntosuministro = psum.id)    " +
                "                    where case when asi.ctadebe is null then (substring (asi.ctahaber from 1 for 3) = '430')   " +
                "                        when asi.ctahaber is null then (substring (asi.ctadebe from 1 for 3) = '430') end   " +
                "                        and (fact.idempresa = ? or recibo.idempresa = ?)    " +
                "                    group by subcuenta, concepto_subcuenta, cif, dir, cod_pos, prov, loc, telefono, fax, web, correo, clave_id, nif_intra1, nif_intra2 " +
                " " +
                " " +
                "   union " +
                "  " +
                "   select case when asi.ctadebe is null then asi.ctahaber when asi.ctahaber is null then asi.ctadebe end as subcuenta,   " +
                "                    case when substring(asi.ctahaber from 1 for 2) = '33' or substring(asi.ctadebe from 1 for 2) = '33' then 'PRODUCTOS EN CURSO (VTOS.)'   " +
                "                        when substring(asi.ctahaber from 1 for 2) = '22' or substring(asi.ctadebe from 1 for 2) = '22' then 'INMOVILIZACIONES MATERIALES (VTOS.)'   " +
                "                        when substring(asi.ctahaber from 1 for 3) = '572' or substring(asi.ctadebe from 1 for 3) = '572' " +
                "       or substring(asi.ctahaber from 1 for 3) = '577' or substring(asi.ctadebe from 1 for 3) = '577' then 'BANCO (REMESA)' " +
                "      when substring(asi.ctahaber from 1 for 3) = '555' or substring(asi.ctadebe from 1 for 3) = '555' then 'CUENTAS NO BANCARIAS (REMESA)' " +
                "      when substring(asi.ctahaber from 1 for 3) = '520' or substring(asi.ctadebe from 1 for 3) = '520' then 'CAJA (REMESA)' " +
                "      when substring(asi.ctahaber from 1 for 2) = '11' or substring(asi.ctadebe from 1 for 2) = '11' then 'RESERVAS (VTOS.)' " +
                "      when substring(asi.ctahaber from 1 for 3) = '431' or substring(asi.ctadebe from 1 for 3) = '431' then 'DEUDORES (VTOS.)' " +
                "     else asi.concepto end as conceptoo,   " +
                "                    '' as porciva,   " +
                "                    '' as tipoiva,   " +
                "                    '' as porcrec,   " +
                "                    '' as cuenrec,   " +
                "                    '' as cif,   " +
                "                    '' as dir,   " +
                "                    '' as cod_pos,   " +
                "                    '' as prov,   " +
                "                    '' as loc,   " +
                "                    '' as telefono,   " +
                "                    '' as fax,   " +
                "                    '' as web,   " +
                "                    '' as correo,   " +
                "                    1 as clave_id,   " +
                "                    '' as nif_intra1,   " +
                "                    '' as nif_intra2,   " +
                "                    '' as nif_intra3,   " +
                "                    'N' as fradeduparcial,   " +
                "                    '' as fradeduporc   " +
                "                    from gasocentro.tesoreria_asiento_pago asi left join gasocentro.tesoreria_factura_pago fact on fact.id = asi.idfactura   " +
                "                        left join gasocentro.tesoreria_recibo_pago recibo on recibo.id = asi.idrecibo  " +
                "                    where case when asi.ctadebe is null then (substring (asi.ctahaber from 1 for 3) <> '430' and substring (asi.ctahaber from 1 for 3) <> '472'   " +
                "                            and substring (asi.ctahaber from 1 for 3) <> '400' and substring (asi.ctahaber from 1 for 3) <> '477')   " +
                "                        when asi.ctahaber is null then (substring (asi.ctadebe from 1 for 3) <> '430' and substring (asi.ctadebe from 1 for 3) <> '472'   " +
                "                            and substring (asi.ctadebe from 1 for 3) <> '400' and substring (asi.ctadebe from 1 for 3) <> '477') end   " +
                "                        and (fact.idempresa = ? or recibo.idempresa = ?)   " +
                "                    group by subcuenta, conceptoo   " +
                "                       " +
                "                union   " +
                "                   " +
                "                select asi.ctadebe as subcuenta,   " +
                "                    asi.concepto as concepto,   " +
                "                    case when (select iva_tipo from gasocentro.articulo where cta_iva_soportado = asi.ctadebe and idgrupo = ? and status = 'ACTIVE' limit 1) = 1 then '21'   " +
                "                        when (select iva_tipo from gasocentro.articulo where cta_iva_soportado = asi.ctadebe and idgrupo = ? and status = 'ACTIVE' limit 1) = 2 then '10'   " +
                "                        when (select iva_tipo from gasocentro.articulo where cta_iva_soportado = asi.ctadebe and idgrupo = ? and status = 'ACTIVE' limit 1) = 3 then '4'    " +
                "                        when (select iva_tipo from gasocentro.articulo where cta_iva_soportado = asi.ctadebe and idgrupo = ? and status = 'ACTIVE' limit 1) = 4 then 'EXENTO' end as porciva,   " +
                "                    case when fact.regiva = 1 then 'R'   " +
                "                        when fact.regiva = 2 then 'O'   " +
                "                        when fact.regiva = 3 or fact.regiva = 4 or fact.regiva = 5 then 'X'   " +
                "                        when fact.regiva = 6 then 'E' end as tipoiva,   " +
                "                    '' as porcrec,   " +
                "                    '' as cuenrec,   " +
                "                    '' as cif,   " +
                "                    '' as dir,   " +
                "                    '' as cod_pos,   " +
                "                    '' as prov,   " +
                "                    '' as loc,   " +
                "                    '' as telefono,   " +
                "                    '' as fax,   " +
                "                    '' as web,   " +
                "                    '' as correo,   " +
                "                    1 as clave_id,   " +
                "                    '' as nif_intra1,   " +
                "                    '' as nif_intra2,   " +
                "                    '' as nif_intra3,   " +
                "                    'N' as fradeduparcial,   " +
                "                    '' as fradeduporc   " +
                "                    from gasocentro.tesoreria_asiento_pago asi left join gasocentro.tesoreria_factura_pago fact on fact.id = asi.idfactura   " +
                "                        left join gasocentro.tesoreria_recibo_pago recibo on recibo.id = asi.idrecibo    " +
                "                    where case when asi.ctadebe is null then (substring (asi.ctahaber from 1 for 3) = '472' or substring (asi.ctahaber from 1 for 3) = '477')   " +
                "                        when asi.ctahaber is null then (substring (asi.ctadebe from 1 for 3) = '472' or substring (asi.ctadebe from 1 for 3) = '477') end   " +
                "                        and (fact.idempresa = ? or recibo.idempresa = ?)   " +
                "                    group by asi.ctadebe, asi.concepto, fact.regiva   " +
                "                       " +
                "                union   " +
                "                       " +
                "                select fact.ctacontableproveedorfac as subcuenta,   " +
                "     fact.proveedorfac concepto_subcuenta,   " +
                "                    '' as porciva,   " +
                "                    '' as tipoiva,   " +
                "                    '' as porcrec,   " +
                "                    '' as cuenrec, " +
                "     fact.nifproveedorfac as cif, " +
                "     fact.proveedorfacdireccion as dir, " +
                "     fact.proveedorfaccodpostal as cod_pos, " +
                "     fact.proveedorfacprovincia as prov, " +
                "     fact.proveedorfaclocalidad as loc, " +
                "     prov.tlf1 as telefono, " +
                "     '' as fax, " +
                "     prov.web as web, " +
                "     '' as correo,   " +
                "                    case when prov.nif_pais = 6 then 1   " +
                "                        else 2 end as clave_id,   " +
                "     case when prov.nif_pais <> 6 then   " +
                "      case when prov.nif_pais is null then ''   " +
                "       when prov.nif_pais = 0 then ''   " +
                "       when prov.nif_pais = 1 then 'AT'   " +
                "       when prov.nif_pais = 2 then 'BE'   " +
                "       when prov.nif_pais = 3 then 'BG'   " +
                "       when prov.nif_pais = 4 then 'CY'   " +
                "       when prov.nif_pais = 5 then 'DK'   " +
                "       when prov.nif_pais = 7 then 'FR'   " +
                "       when prov.nif_pais = 8 then 'GR'   " +
                "       when prov.nif_pais = 9 then 'HU'   " +
                "       when prov.nif_pais = 10 then 'IT'   " +
                "       when prov.nif_pais = 11 then 'LU'   " +
                "       when prov.nif_pais = 12 then 'MT'   " +
                "       when prov.nif_pais = 13 then 'NL'   " +
                "       when prov.nif_pais = 14 then 'PL'   " +
                "       when prov.nif_pais = 15 then 'PT'   " +
                "       when prov.nif_pais = 17 then 'DE'   " +
                "       when prov.nif_pais = 18 then 'RO'   " +
                "       when prov.nif_pais = 19 then 'SE'   " +
                "       when prov.nif_pais = 20 then 'LV'   " +
                "       when prov.nif_pais = 21 then 'EE'   " +
                "       when prov.nif_pais = 22 then 'LT'   " +
                "       when prov.nif_pais = 23 then 'CZ'   " +
                "       when prov.nif_pais = 24 then 'SK'   " +
                "       when prov.nif_pais = 25 then 'HR'   " +
                "       when prov.nif_pais = 26 then 'SI'   " +
                "       when prov.nif_pais = 27 then 'FI'   " +
                "       when prov.nif_pais = 28 then 'IE' end   " +
                "     else '' end as nif_intra1,   " +
                "     case when prov.nif_pais <> 6 then substring(prov.nif, 2)   " +
                "      else '' end  as nif_intra2,   " +
                "                    '' as nif_intra3,   " +
                "                    'N' as fradeduparcial,   " +
                "                    '' as fradeduporc   " +
                "                    from gasocentro.tesoreria_factura_pago fact    " +
                "                        left join gasocentro.tesoreria_recibo_pago recibo on recibo.idfactura = fact.id   " +
                "                        inner join gasocentro.tesoreria_asiento_pago asi on (asi.idfactura = fact.id or asi.idrecibo = recibo.id) " +
                "                     left join gasocentro.proveedor prov on prov.id = fact.idproveedor " +
                "     where case when asi.ctadebe is null then (substring (asi.ctahaber from 1 for 3) = '400')   " +
                "                        when asi.ctahaber is null then (substring (asi.ctadebe from 1 for 3) = '400') end   " +
                "                        and (fact.idempresa = ? or recibo.idempresa = ?)    " +
                "                    group by subcuenta, concepto_subcuenta, cif, dir, cod_pos, prov, loc, telefono, fax, web, correo, clave_id, nif_intra1, nif_intra2) as a     " +
                "                       " +
                "                    order by a.subcuenta";

        ArrayList<ArrayList<String>> datos = new ArrayList<>();


        try {

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, idEmpresa);
            statement.setLong(2, idEmpresa);
            statement.setLong(3, idEmpresa);
            statement.setLong(4, idGrupo);
            statement.setLong(5, idGrupo);
            statement.setLong(6, idGrupo);
            statement.setLong(7, idGrupo);
            statement.setLong(8, idEmpresa);
            statement.setLong(9, idEmpresa);
            statement.setLong(10, idEmpresa);
            statement.setLong(11, idEmpresa);
            statement.setLong(12, idEmpresa);
            statement.setLong(13, idEmpresa);
            statement.setLong(14, idEmpresa);
            statement.setLong(15, idGrupo);
            statement.setLong(16, idGrupo);
            statement.setLong(17, idGrupo);
            statement.setLong(18, idGrupo);
            statement.setLong(19, idEmpresa);
            statement.setLong(20, idEmpresa);
            statement.setLong(21, idEmpresa);
            statement.setLong(22, idEmpresa);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ArrayList<String> datosAsiento = new ArrayList<>();
                for (int i = 1; i < 13; i++) {
                    datosAsiento.add(String.valueOf(resultSet.getObject(i)));
                }
                datos.add(datosAsiento);
            }

            statement.close();
            resultSet.close();
            cerrarConexionDb();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return datos;
    }


}
