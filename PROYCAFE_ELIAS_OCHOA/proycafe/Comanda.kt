package com.example.proycafe

data class Comanda(
    val id: Int,
    val mesa: Int,
    val pedido: String?,
    val precio: Double,
    val estado: String
)