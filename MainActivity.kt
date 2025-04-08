package com.example.mtoproductos

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import java.io.ByteArrayOutputStream

fun base64ToBitmap(base64Str: String?): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

val poppins = FontFamily(
    Font(R.font.poppins_regular),
    Font(R.font.poppins_bold, weight = FontWeight.Bold)
)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF6750A4),
                    secondary = Color(0xFF03DAC6)
                ),
                typography = Typography(
                    bodyLarge = TextStyle(
                        fontFamily = poppins,
                        fontSize = 16.sp
                    ),
                    headlineSmall = TextStyle(
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                )
            ) {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "lista") {
                    composable("lista") { PantallaPrincipal(navController) }
                    //composable("nuevo") { PantallaFormularioProducto(navController) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(navController: NavController) {
    val context = LocalContext.current
    val db = remember { DBHelper(context) }
    var productos by remember { mutableStateOf(db.getAllProductos()) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {/* navController.navigate("nuevo")*/ },
                containerColor = Color(0xFF7798AB)  ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Productos disponibles ✔", style = MaterialTheme.typography.headlineSmall)
                },
                modifier = Modifier.height(35.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(productos) { producto ->
                    ProductoCard(
                        producto = producto,
                        onDelete = {
                            /*db.deleteProducto(producto.id)
                            productos = db.getAllProductos()*/
                        },
                        onEdit = {

                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/*@Composable
fun PantallaFormularioProducto(navController: NavController) {
    val context = LocalContext.current
    val db = remember { DBHelper(context) }

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var imagenBase64 by remember { mutableStateOf<String?>(null) }
    var imagenPreview by remember { mutableStateOf<Bitmap?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                imagenPreview = bitmap
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                imagenBase64 = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Nuevo Producto", fontFamily = poppins)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") },    modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
                singleLine = true
            )
            OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") },    modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
                singleLine = true
            )
            OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio") },    modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
                singleLine = true
            )

            Button(onClick = {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                launcher.launch(intent)
            },   modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF453643), // Morado fuerte, o el color que desees
                    contentColor = Color.White          // Color del texto del botón
                )
            ) {
                Text("Seleccionar Imagen")
            }

            imagenPreview?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }

            Button(onClick = {
                db.insertarProducto(
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio.toDoubleOrNull() ?: 0.0,
                    imagenBase64 = imagenBase64
                )
                navController.popBackStack()
            },  modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF453643),
                    contentColor = Color.White
                )) {
                Text("Guardar Producto")
            }
        }
    }
}*/

@Composable
fun ProductoCard(
    producto: Producto,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8DCB9)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(producto.nombre, fontWeight = FontWeight.Bold)
                Text("Precio: \$${String.format("%.2f", producto.precio)}")
                producto.descripcion?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(it, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onEdit,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFC7A27C),
                            contentColor = Color(0xFF000000),
                        )
                    ) {
                        Text("✏️ Editar")
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFC7A27C),
                            contentColor = Color(0xFF000000),
                        )
                    ) {
                        Text("❌ Eliminar")
                    }
                }

            }

            producto.imagenBase64?.let {
                base64ToBitmap(it)?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }
    }
}