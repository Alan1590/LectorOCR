/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BDSqlite;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author alan
 */
public class BDSqlite {

    private Connection c = null;
    private PreparedStatement st = null;
    private static int ID;
    private static int campoBuscado;
    private static String palabraBuscada="";
    private  ArrayList<String> resultadosPalabrasClaves = new ArrayList<>();
    private  ArrayList<String> resultadosUrl = new ArrayList<>();
    private  ArrayList<String> resultadosPorBusqueda = new ArrayList<>();

    public ArrayList<String> getResultadosPorBusqueda() {
        return resultadosPorBusqueda;
    }
    
    public static void setCampoBuscado(int campoBuscado) {
        BDSqlite.campoBuscado = campoBuscado;

    }

    public static void setPalabraBuscada(String palabraBuscada) {
        BDSqlite.palabraBuscada = palabraBuscada;
    }

    public ArrayList<String> getResultadosPalabrasClaves() {
        return resultadosPalabrasClaves;
    }

    public ArrayList<String> getResultadosUrl() {
        return resultadosUrl;
    }
    
    
    
    
    public static int getID() {
        return ID;
    }

    public BDSqlite() {
        String sistemaOperativo = System.getProperties().getProperty("os.name");
        try {
            if (sistemaOperativo.equals("Linux")) {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:./src/BDSqlite/lector_ocr.sqlite");
            } else {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:.\\lector_ocr.sqlite");
            }

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public void insertarDatosLogin(String user, String pass) throws SQLException {
        st = c.prepareStatement("insert into login (id, name,user, pass) values(?,?,?,?)");
        st.setString(1, user);
        st.setString(2, pass);
        st.execute();
    }

    public void insertarDatosImgagen(String fecha_creacion, String nombre, int ancho_imagen,
            int alto_imagen, String url_imagen_original, String url_imagen_destino, String palabraclave, int idusuario) throws SQLException {
        st = c.prepareStatement("insert into tbl_imagenes (dfecha_creacion, cnombre, iancho, ialto, turl_original, turl_destino, tpalabras_claves, iid_usuario) "
                + "values(?,?,?,?,?,?,?,?)");
        st.setString(1, fecha_creacion);
        st.setString(2, nombre);
        st.setInt(3, ancho_imagen);
        st.setInt(4, alto_imagen);
        st.setString(5, url_imagen_original);
        st.setString(6, url_imagen_destino);
        st.setString(7, palabraclave);
        st.setInt(8, idusuario);
        st.execute();
    }

    public void insertarDatosImgHija(String name, String urlImageHija, int id_urlImagenMadre, String palabrasClaves) throws SQLException {
        st = c.prepareStatement("insert into tbl_ocrimagesh (name, urlimgh, id_urlimgm, id_palabrasclaves) values(?,?,?,?)");
        st.setString(1, name);
        st.setString(2, urlImageHija);
        st.setInt(3, id_urlImagenMadre);
        st.setString(4, palabrasClaves);
        st.execute();

    }

    public void insertarDatosPalabraC(int idImgMadre, String palabrasClave) throws SQLException {
        st = c.prepareStatement("insert into palabrasc (id,id_imgmadre, palabrasclaves) values(?,?,?)");
        st.setString(1, palabrasClave);
        st.setInt(2, idImgMadre);
        st.execute();
    }

    public int getUltimoIdImgMadre() throws SQLException {
        Statement stmt = c.createStatement();
        ResultSet result = stmt.executeQuery("Select * from tbl_ocrimagesm");
        int ultimoID = 0;
        while (result.next()) {
            ultimoID = result.getInt("id");
        }
        return ultimoID;

    }

    public Multimap<Integer, String> getDatosBD() throws SQLException {
        Multimap resultados = ArrayListMultimap.create();
        PreparedStatement prst = c.prepareStatement("Select * from tbl_imagenes;");
        ResultSet result = prst.executeQuery();
        int id;
        String palabraClave;
        while (result.next()) {
            id = result.getInt("iid");
            palabraClave = result.getString("cnombre");
            resultados.put(id, palabraClave);

        }

        return resultados;

    }

    public void ejecutarConsulta() throws SQLException {
        resultadosPorBusqueda.clear();
        resultadosPalabrasClaves.clear();
        resultadosUrl.clear();

        PreparedStatement prst;
        ResultSet result;
        switch (campoBuscado) {

            //Buscar por palabra global

            case (0):
                prst = c.prepareStatement("Select * from tbl_imagenes where cnombre like ?;");
                prst.setString(1, "%" + palabraBuscada + "%");
                result = prst.executeQuery();
                while (result.next()) {
                    int x = 0;
                    resultadosPorBusqueda.add(result.getString("cnombre"));
                    String[] splitResultado = result.getString("tpalabras_claves").split(",");
                    while (x < splitResultado.length) {
                        resultadosPalabrasClaves.add(splitResultado[x].replace("[", "").replace("]", ""));
                        resultadosUrl.add(result.getString("turl_destino"));
                        x++;
                    }
                }

                break;

            case (1):
                prst = c.prepareStatement("Select * from tbl_imagenes where tpalabras_claves like ?;");
                prst.setString(1, "%" + palabraBuscada + "%");
                result = prst.executeQuery();
                while (result.next()) {
                    int x = 0;
                    String[] splitResultado = result.getString("tpalabras_claves").split(",");
                    while (x < splitResultado.length) {
                        resultadosPorBusqueda.add(splitResultado[x].replace("[", "").replace("]", ""));

                        resultadosPalabrasClaves.add(splitResultado[x].replace("[", "").replace("]", ""));
                        resultadosUrl.add(result.getString("turl_destino"));
                        x++;
                    }
                }
                System.err.println("1");
                break;

            case (2):
                prst = c.prepareStatement("Select * from tbl_imagenes where dfecha_creacion like ?;");
                prst.setString(1, "%" + palabraBuscada + "%");
                result = prst.executeQuery();
                while (result.next()) {
                    int x = 0;
                    resultadosPorBusqueda.add(result.getString("dfecha_creacion"));

                    String[] splitResultado = result.getString("tpalabras_claves").split(",");
                    while (x < splitResultado.length) {
                        resultadosPalabrasClaves.add(splitResultado[x].replace("[", "").replace("]", ""));
                        resultadosUrl.add(result.getString("turl_destino"));
                        x++;
                    }
                }
                System.err.println("2");

                break;

        }
    }

    public ArrayList<String> getPalabraClaves(int seleccion, String palabraSeleccionado) throws SQLException {
        ArrayList<String> resultados = new ArrayList<>();
        PreparedStatement prst;
        ResultSet result;

        switch (seleccion) {

            //Buscar por palabra global
            case (0):
                prst = c.prepareStatement("Select * from tbl_imagenes where tpalabras_claves like ?;");
                prst.setString(1, "%" + palabraSeleccionado + "%");
                result = prst.executeQuery();

                while (result.next()) {
                    int x = 0;

                    result = prst.executeQuery();

                    String[] splitResultado = result.getString("tpalabras_claves").split(",");
                    while (x < splitResultado.length) {
                        resultados.add(splitResultado[x].replace("[", "").replace("]", ""));
                        x++;
                    }
                }

            case (1):
                prst = c.prepareStatement("Select * from tbl_imagenes where cnombre like ?;");
                prst.setString(1, "%" + palabraSeleccionado + "%");
                result = prst.executeQuery();

                while (result.next()) {
                    int x = 0;

                    String[] splitResultado = result.getString("tpalabras_claves").split(",");
                    while (x < splitResultado.length) {
                        resultados.add(splitResultado[x].replace("[", "").replace("]", ""));
                        x++;
                    }

                }
            case (2):
                prst = c.prepareStatement("Select * from tbl_imagenes where dfecha_creacion like ?;");
                prst.setString(1, "%" + palabraSeleccionado + "%");
                result = prst.executeQuery();

                while (result.next()) {
                    int x = 0;

                    String[] splitResultado = result.getString("tpalabras_claves").split(",");
                    while (x < splitResultado.length) {
                        resultados.add(splitResultado[x].replace("[", "").replace("]", ""));
                        x++;
                    }

                }

        }
        return resultados;
    }

    public String getUrlImg() throws SQLException {
        PreparedStatement prst = c.prepareStatement("Select * from tbl_ocrimagesh where id = 1;");
        ResultSet result = prst.executeQuery();
        String urlIMG = "";
        while (result.next()) {
            urlIMG = result.getString("id_urlimgm");
        }
        return urlIMG;
    }
    
    public void limpiarVariables(){
             Connection c = null;
     PreparedStatement st = null;
     ID=0;
     campoBuscado=0;
     palabraBuscada="";
    resultadosPalabrasClaves.clear();
    resultadosUrl.clear();
    resultadosPorBusqueda.clear();
    }
}
