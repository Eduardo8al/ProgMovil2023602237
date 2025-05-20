package com.example.mtoproductos


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.*
import java.io.ByteArrayOutputStream
import java.io.File
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.filled.ArrowDropDown
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat


class ThemePreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    // Guardar el tema (modo claro o oscuro)
    fun saveThemeMode(themeMode: ThemeMode) {
        val editor = sharedPreferences.edit()
        editor.putString("theme_mode", themeMode.name)
        editor.apply()
    }

    // Leer el tema guardado (predeterminado a LIGHT si no se ha guardado nada)
    fun loadThemeMode(): ThemeMode {
        val themeModeString = sharedPreferences.getString("theme_mode", ThemeMode.LIGHT.name)
        return ThemeMode.valueOf(themeModeString ?: ThemeMode.LIGHT.name)
    }
}

fun base64ToBitmap(base64Str: String?): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

//  FUNCIÓN PARA REDIMENSIONAR IMÁGENES
fun resizeImage(bitmap: Bitmap, maxSize: Int = 500): Bitmap {
    val ratio: Float = bitmap.width.toFloat() / bitmap.height.toFloat()
    val width: Int
    val height: Int

    if (ratio > 1) {
        width = maxSize
        height = (maxSize / ratio).toInt()
    } else {
        height = maxSize
        width = (maxSize * ratio).toInt()
    }

    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}


val poppins = FontFamily(
    Font(R.font.poppins_regular),
    Font(R.font.poppins_bold, weight = FontWeight.Bold)
)
enum class ThemeMode {
    LIGHT, DARK
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themePreferences = ThemePreferences(this) // Crear fuera del composable

        setContent {
            var themeMode by rememberSaveable {
                mutableStateOf(themePreferences.loadThemeMode()) // Usar dentro de setContent
            }

            MyApp(themeMode = themeMode, onThemeChange = { newMode ->
                themeMode = newMode
                themePreferences.saveThemeMode(newMode)
            })
        }
    }
}

// Aquí está la función composable que maneja la UI
@Composable
fun MyApp(themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    val lightColors = lightColorScheme(
        primary = Color(0xFF6750A4),
        secondary = Color(0xFF03DAC6),
        background = Color.White,
        onBackground = Color.Black
    )

    val darkColors = darkColorScheme(
        primary = Color(0xFF2E7D32),
        secondary = Color(0xFF66BB6A),
        background = Color.Black,
        onBackground = Color.White
    )

    MaterialTheme(
        colorScheme = if (themeMode == ThemeMode.LIGHT) lightColors else darkColors,
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
            composable("lista") {
                PantallaPrincipal(navController, themeMode, onThemeChange = { newThemeMode ->
                    onThemeChange(newThemeMode)  // Propagar el cambio de tema
                })
            }
            composable("nuevo") { PantallaFormularioProducto(navController) }
            composable("editar/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
                if (id != null) {
                    PantallaFormularioProducto(navController, id)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(navController: NavController, themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    val context = LocalContext.current
    val db = remember { DBHelper(context) }
    var productos by remember { mutableStateOf(db.getAllProductos()) }
    var mostrarAyuda by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menú", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "Ayuda") },
                    label = { Text("Ayuda") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        mostrarAyuda = true
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.MoreVert, contentDescription = "Cambiar tema") },
                    label = { Text("Cambiar tema") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onThemeChange(if (themeMode == ThemeMode.LIGHT) ThemeMode.DARK else ThemeMode.LIGHT)
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Exportar CSV") },
                    label = { Text("Exportar CSV") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        exportarYCompartirCSV(context, productos)
                    }
                )
            }
        }
    ){
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Productos disponibles ✔", style = MaterialTheme.typography.headlineSmall)
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open()}}) {
                            Icon(Icons.Default.List, contentDescription = "Menú")
                        }
                    },
                    modifier = Modifier.height(35.dp)
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { navController.navigate("nuevo") },
                    containerColor = Color(0xFF7798AB)) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar")
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                    items(productos) { producto ->
                        ProductoCard(
                            producto = producto,
                            onDelete = {
                                db.deleteProducto(producto.id)
                                productos = db.getAllProductos()
                            },
                            onEdit = {
                                navController.navigate("editar/${producto.id}")
                            },
                            themeMode = themeMode
                        )
                    }
                }
            }
        }

        // Diálogo de ayuda
        if (mostrarAyuda) {
            AlertDialog(
                onDismissRequest = { mostrarAyuda = false },
                confirmButton = {
                    TextButton(onClick = { mostrarAyuda = false }) {
                        Text("Entendido", color = Color(0xFFFFFFFF))
                    }
                },
                title = { Text("Tutorial de la App") },
                text = {
                    Text(
                        "• Toca el botón '+' para agregar un producto.\n" +
                                "• Usa el botón '✏\uFE0F' para editar y el  '❌' para eliminar productos.\n" +
                                "• Toca el menú para abrir esta ayuda nuevamente.\n" +
                                "• Puedes tomar fotos con la cámara o elegir de la galería."
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormularioProducto(navController: NavController, productoId: Int? = null) {
    val context = LocalContext.current
    val db = remember { DBHelper(context) }

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var imagenBase64 by remember { mutableStateOf<String?>(null) }
    var imagenPreview by remember { mutableStateOf<Bitmap?>(null) }

    var precioError by remember { mutableStateOf(false) }
    var nombreError by remember { mutableStateOf(false) }

    LaunchedEffect(productoId) {
        if (productoId != null) {
            val producto = db.getProductoPorId(productoId)
            producto?.let {
                nombre = it.nombre
                descripcion = it.descripcion ?: ""
                precio = it.precio.toString()
                imagenBase64 = it.imagenBase64
                imagenBase64?.let { base64 ->
                    imagenPreview = base64ToBitmap(base64)
                }
            }
        }
    }

    val imageFile = remember {
        File(context.getExternalFilesDir("Pictures"), "foto_producto.jpg")
    }

    val imageUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )

    // Lanzador para tomar foto
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            val resizedBitmap = resizeImage(bitmap)
            imagenPreview = resizedBitmap
            val stream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            imagenBase64 = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
        }
    }

    // Lanzador para pedir permiso cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(imageUri)
        } else {
            Toast.makeText(context, "Se requiere permiso de cámara", Toast.LENGTH_SHORT).show()
        }
    }

    // Lanzador para galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val resizedBitmap = resizeImage(bitmap)
            imagenPreview = resizedBitmap

            val byteArrayOutputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            imagenBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
        }
    }

    // Función para manejar click cámara: chequear permiso o pedirlo
    fun onClickCamera() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                // Permiso concedido, lanzar cámara
                cameraLauncher.launch(imageUri)
            }
            else -> {
                // Pedir permiso
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (productoId == null) "Nuevo Producto" else "Editar Producto", fontFamily = poppins) },
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
            // Campos y validaciones igual que antes...
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    nombreError = it.isBlank()
                },
                label = { Text("Nombre") },
                isError = nombreError,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                singleLine = true
            )
            if (nombreError) {
                Text(
                    text = "El nombre no puede estar vacío",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = precio,
                onValueChange = {
                    precio = it
                    precioError = it.toDoubleOrNull()?.let { n -> n < 0 } ?: true
                },
                label = { Text("Precio") },
                isError = precioError,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                singleLine = true
            )


            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF453643),
                        contentColor = Color.White
                    )
                ) {
                    Text("Galería")
                }

                Button(
                    onClick = { onClickCamera() },  // <-- Cambiado aquí
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF453643),
                        contentColor = Color.White
                    )
                ) {
                    Text("Cámara")
                }
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

            Button(
                onClick = {
                    nombreError = nombre.isBlank()
                    val precioDoubleOrNull = precio.toDoubleOrNull()
                    precioError = precioDoubleOrNull == null || precioDoubleOrNull < 0

                    if (!nombreError && !precioError) {
                        val precioFinal = precioDoubleOrNull ?: 0.0

                        if (productoId == null) {
                            db.insertarProducto(
                                nombre = nombre,
                                descripcion = descripcion,
                                precio = precioFinal,
                                imagenBase64 = imagenBase64
                            )
                        } else {
                            db.actualizarProducto(
                                id = productoId,
                                nombre = nombre,
                                descripcion = descripcion,
                                precio = precioFinal,
                                imagenBase64 = imagenBase64
                            )
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF453643),
                    contentColor = Color.White
                )
            ) {
                Text("Guardar Producto")
            }
        }
    }
}

@Composable
fun ProductoCard(
    producto: Producto,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    themeMode: ThemeMode
) {
    // Colores para las tarjetas, botones y texto según el tema
    val cardColor = if (themeMode == ThemeMode.DARK) Color(0xFF18314F) else Color(0xFFE8DCB9) // Verde oscuro para modo oscuro y beige para modo claro
    val buttonColor = if (themeMode == ThemeMode.DARK) Color(0xFF212738) else Color(0xFFC7A27C) // Verde más oscuro para modo oscuro y marrón claro para modo claro
    val textColor = if (themeMode == ThemeMode.DARK) Color.White else Color.Black // Blanco para el texto en modo oscuro y negro en modo claro

    // Componente Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, fontWeight = FontWeight.Bold, color = textColor)
                Text("Precio: \$${String.format("%.2f", producto.precio)}", color = textColor)
                producto.descripcion?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(it, fontSize = 12.sp, color = textColor)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onEdit,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = buttonColor,
                            contentColor = textColor
                        )
                    ) {
                        Text("✏️")
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = buttonColor,
                            contentColor = textColor
                        )
                    ) {
                        Text("❌")
                    }
                }
            }

            // Imagen del producto
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

fun exportarYCompartirCSV(context: Context, productos: List<Producto>) {
    try {
        // Crear el contenido CSV
        val csvContent = StringBuilder()
        csvContent.append("ID,Nombre,Precio,Descripción\n")
        for (producto in productos) {
            csvContent.append("${producto.id},\"${producto.nombre}\",${producto.precio},\"${producto.descripcion ?: ""}\"\n")
        }

        // Crear archivo en cache
        val fileName = "productos_exportados.csv"
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { it.write(csvContent.toString().toByteArray()) }

        // Obtener URI segura
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

        // Crear intención para compartir
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Compartir archivo CSV"))

    } catch (e: Exception) {
        e.printStackTrace()
    }
}



