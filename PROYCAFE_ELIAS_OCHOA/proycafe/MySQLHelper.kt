package com.example.proycafe

import android.os.StrictMode
import com.example.proycafe.MySQLHelper.conectar
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

object MySQLHelper {

    private const val url = "jdbc:mysql://bmdaaj3fmy5ngoi8oexr-mysql.services.clever-cloud.com:3306/bmdaaj3fmy5ngoi8oexr"
    private const val user = "ujpxolgimpmcr6wf"
    private const val password = "nsDozubhMh0AGhEWNZnw"

    fun conectar(): Connection? {
        return try {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            Class.forName("com.mysql.jdbc.Driver") // driver mysql 5.x
            DriverManager.getConnection(url, user, password)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun insertarComanda(mesa: Int, pedido: String?, precio: Double, estado: String): Boolean {
        val conn = conectar() ?: return false
        val sql = "INSERT INTO comanda (mesa, pedido, precio, estado) VALUES (?, ?, ?, ?)"
        return try {
            val stmt: PreparedStatement = conn.prepareStatement(sql)
            stmt.setInt(1, mesa)
            stmt.setString(2, pedido)
            stmt.setDouble(3, precio)
            stmt.setString(4, estado)
            stmt.executeUpdate()
            stmt.close()
            conn.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun actualizarComanda(
        id: Int,
        mesa: Int,
        pedido: String?,
        precio: Double,
        estado: String
    ): Boolean {
        val conn = conectar() ?: return false
        val sql = """
        INSERT INTO comanda (id_comanda, mesa, pedido, precio, estado)
        VALUES (?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            mesa = VALUES(mesa),
            pedido = VALUES(pedido),
            precio = VALUES(precio),
            estado = VALUES(estado)
    """.trimIndent()
        return try {
            val stmt = conn.prepareStatement(sql)
            stmt.setInt(1, id)
            stmt.setInt(2, mesa)
            stmt.setString(3, pedido)
            stmt.setDouble(4, precio)
            stmt.setString(5, estado)
            stmt.executeUpdate()
            stmt.close()
            conn.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    fun obtenerComandas(): List<Comanda> {
        val comandas = mutableListOf<Comanda>()
        val conn = conectar() ?: return comandas
        val sql = "SELECT id_comanda, mesa, pedido, precio, estado FROM comanda"

        try {
            val stmt = conn.prepareStatement(sql)
            val rs: ResultSet = stmt.executeQuery()
            while (rs.next()) {
                val id = rs.getInt("id_comanda")
                val mesa = rs.getInt("mesa")
                val pedido = rs.getString("pedido")
                val precio = rs.getDouble("precio")
                val estado = rs.getString("estado")

                comandas.add(Comanda(id, mesa, pedido, precio, estado))
            }
            rs.close()
            stmt.close()
            conn.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return comandas
    }
    fun eliminarComanda(id: Int): Boolean {
        val conn = conectar() ?: return false
        val sql = "DELETE FROM comanda WHERE id_comanda = ?"
        return try {
            val stmt: PreparedStatement = conn.prepareStatement(sql)
            stmt.setInt(1, id)
            stmt.executeUpdate()
            stmt.close()
            conn.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
