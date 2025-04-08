package com.example.mtoproductos

data class Producto(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val imagenBase64: String?
)
