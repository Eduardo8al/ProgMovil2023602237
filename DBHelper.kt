package com.example.mtoproductos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, "mibase.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val queryCreate = """
            CREATE TABLE producto (
                id_producto INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                nombre TEXT NOT NULL,
                precio REAL NOT NULL,
                descripcion TEXT,
                imagen TEXT
            );
        """.trimIndent()
        db.execSQL(queryCreate)

        // Insertar productos con base64 real
        val productos = listOf(
            Triple("Chetos", 17.50, "Botana crujiente de queso"),
            Triple("Doritos", 18.00, "Totopos sabor queso"),
            Triple("Galletas Oreo", 15.00, "Galletas con crema"),
            Triple("Coca-Cola", 20.00, "Refresco de cola"),
            Triple("Pepsi", 19.50, "Refresco de cola rival"),
            Triple("Agua Bonafont", 10.00, "Agua natural embotellada"),
            Triple("Gansito", 14.00, "Pastelito con fresa y chocolate"),
            Triple("Mazapán", 5.00, "Dulce de cacahuate"),
            Triple("Maruchan", 12.05, "Sopa instantánea"),
            Triple("Sabritas", 17.00, "Papas fritas clásicas")
        )

        val imagenes = listOf(
            Base64Images.chetostxt,
            Base64Images.doritostxt,
            Base64Images.oreotxt,
            Base64Images.cocatxt,
            Base64Images.pepsitxt,
            Base64Images.aguatxt,
            Base64Images.gansitotxt,
            Base64Images.mazapantxt,
            Base64Images.maruchantxt,
            Base64Images.sabritastxt
        )

        productos.forEachIndexed { index, producto ->
            val values = ContentValues().apply {
                put("nombre", producto.first)
                put("precio", producto.second)
                put("descripcion", producto.third)
                put("imagen", imagenes[index])
            }
            db.insert("producto", null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS producto")
        onCreate(db!!)
    }

    fun insertarProducto(nombre: String, descripcion: String?, precio: Double, imagenBase64: String?) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("descripcion", descripcion)
            put("precio", precio)
            put("imagen", imagenBase64)
        }
        db.insert("producto", null, values)
        db.close()
    }

    fun getAllProductos(): List<Producto> {
        val productos = mutableListOf<Producto>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM producto", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id_producto"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"))
                val precio = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"))
                val imagen = cursor.getString(cursor.getColumnIndexOrThrow("imagen"))

                productos.add(Producto(id, nombre, descripcion, precio, imagen))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return productos
    }

    fun deleteProducto(id: Int) {
        val db = writableDatabase
        db.delete("producto", "id_producto = ?", arrayOf(id.toString()))
        db.close()
    }
}
