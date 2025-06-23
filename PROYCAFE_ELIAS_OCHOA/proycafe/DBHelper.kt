package com.example.proycafe

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, "cafeteria.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val queryCreate = """
            CREATE TABLE comanda (
                id_comanda INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                mesa INT NOT NULL,
                precio REAL NOT NULL,
                pedido TEXT,
                estado TEXT,
                sincronizada INTEGER DEFAULT 0
            );
        """.trimIndent()
        db.execSQL(queryCreate)

        // Insertar comandas de ejemplo
        data class ComanEjem(val mesa: Int, val precio: Double, val pedido: String)
        val comandas = listOf(
            ComanEjem(1, 17.50, "Botana crujiente de queso"),
            ComanEjem(2, 18.00, "Totopos sabor queso"),
            ComanEjem(3, 15.00, "Galletas con crema")
        )
        comandas.forEach { comanda ->
            val values = ContentValues().apply {
                put("mesa", comanda.mesa)
                put("precio", comanda.precio)
                put("pedido", comanda.pedido)
                put("estado", "Pendiente")
                put("sincronizada", 1) // Ejemplos ya sincronizados
            }
            db.insert("comanda", null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS comanda")
        onCreate(db!!)
    }

    fun insertarComandaConId(
        id: Int,
        mesa: Int,
        pedido: String?,
        precio: Double,
        estado: String = "Pendiente"
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id_comanda", id)
            put("mesa", mesa)
            put("pedido", pedido)
            put("precio", precio)
            put("estado", estado)
        }
        db.insertWithOnConflict("comanda", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    // Función para insertar comandas marcadas como sincronizadas (ej. importadas de MySQL)
    fun insertarComandaSync(mesa: String, pedido: String?, precio: Double, estado: String = "Pendiente") {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("mesa", mesa)
            put("pedido", pedido)
            put("precio", precio)
            put("estado", estado)
            put("sincronizada", 1) // Ya sincronizada
        }
        db.insert("comanda", null, values)
        db.close()
    }

    fun getAllComandas(): List<Comanda> {
        val comandas = mutableListOf<Comanda>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM comanda", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id_comanda"))
                val mesa = cursor.getInt(cursor.getColumnIndexOrThrow("mesa"))
                val pedido = cursor.getString(cursor.getColumnIndexOrThrow("pedido"))
                val precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"))
                val estado = cursor.getString(cursor.getColumnIndexOrThrow("estado"))

                comandas.add(Comanda(id, mesa, pedido, precio, estado))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return comandas
    }

    fun getComandaPorId(id: Int): Comanda? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM comanda WHERE id_comanda = ?", arrayOf(id.toString()))
        val comanda = if (cursor.moveToFirst()) {
            Comanda(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id_comanda")),
                mesa = cursor.getInt(cursor.getColumnIndexOrThrow("mesa")),
                pedido = cursor.getString(cursor.getColumnIndexOrThrow("pedido")),
                precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio")),
                estado = cursor.getString(cursor.getColumnIndexOrThrow("estado"))
            )
        } else null
        cursor.close()
        db.close()
        return comanda
    }

    fun actualizarComanda(id: Int, mesa: String, pedido: String?, precio: Double, estado: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("mesa", mesa)
            put("pedido", pedido)
            put("precio", precio)
            put("estado", estado)
            put("sincronizada", 0) // Marcamos para sincronizar cambios
        }
        db.update("comanda", values, "id_comanda = ?", arrayOf(id.toString()))
        db.close()
    }

    fun actualizarEstado(id: Int, estado: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("estado", estado)
            put("sincronizada", 0) // Marcamos para sincronizar cambios
        }
        db.update("comanda", values, "id_comanda = ?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteComanda(id: Int) {
        val db = writableDatabase
        db.delete("comanda", "id_comanda = ?", arrayOf(id.toString()))
        db.close()
    }

    // Comandas que aún no han sido sincronizadas con el servidor remoto
    fun getComandasNoSincronizadas(): List<Comanda> {
        val comandas = mutableListOf<Comanda>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM comanda WHERE sincronizada = 0", null)
        if (cursor.moveToFirst()) {
            do {
                comandas.add(
                    Comanda(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id_comanda")),
                        mesa = cursor.getInt(cursor.getColumnIndexOrThrow("mesa")),
                        pedido = cursor.getString(cursor.getColumnIndexOrThrow("pedido")),
                        precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio")),
                        estado = cursor.getString(cursor.getColumnIndexOrThrow("estado"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return comandas
    }

    // Marca una comanda como sincronizada después de subirla al servidor remoto
    fun marcarSincronizada(id: Int) {
        val db = writableDatabase
        val values = ContentValues().apply { put("sincronizada", 1) }
        db.update("comanda", values, "id_comanda = ?", arrayOf(id.toString()))
        db.close()
    }
    fun borrarTodasComandas() {
        val db = writableDatabase
        db.delete("comanda", null, null)
        db.close()
    }
    fun insertarComanda(
        mesa: String,
        pedido: String?,
        precio: Double,
        estado: String = "Pendiente"
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("mesa", mesa.toIntOrNull() ?: 0) // conversión segura
            put("pedido", pedido)
            put("precio", precio)
            put("estado", estado)
            put("sincronizada", 0) // Por defecto no sincronizada
        }
        db.insert("comanda", null, values)
        db.close()
    }

}

