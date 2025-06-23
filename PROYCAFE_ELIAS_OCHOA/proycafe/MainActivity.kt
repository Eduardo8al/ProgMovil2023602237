package com.example.proycafe


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.filled.Refresh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.CloudUpload




class ThemePreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    fun saveThemeMode(themeMode: ThemeMode) {
        sharedPreferences.edit().putString("theme_mode", themeMode.name).apply()
    }

    fun loadThemeMode(): ThemeMode {
        val themeString = sharedPreferences.getString("theme_mode", ThemeMode.LIGHT.name)
        return ThemeMode.valueOf(themeString ?: ThemeMode.LIGHT.name)
    }
}

val poppins = FontFamily(
    Font(R.font.poppins_regular),
    Font(R.font.poppins_bold, weight = FontWeight.Bold)
)

enum class ThemeMode { LIGHT, DARK, AZUL, ROSA, VERDE }

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themePreferences = ThemePreferences(this)

        setContent {
            var themeMode by rememberSaveable { mutableStateOf(themePreferences.loadThemeMode()) }

            MyApp(
                themeMode = themeMode,
                onThemeChange = { newMode ->
                    themeMode = newMode
                    themePreferences.saveThemeMode(newMode)
                }
            )
        }
    }
}

@Composable
fun MyApp(themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    val selectedColors = when (themeMode) {
        ThemeMode.LIGHT -> lightColorScheme(
            primary = Color(0xFF6750A4),
            secondary = Color(0xFF03DAC6),
            background = Color.White,
            onBackground = Color.Black
        )
        ThemeMode.DARK -> darkColorScheme(
            primary = Color(0xFF2E7D32),
            secondary = Color(0xFF66BB6A),
            background = Color.Black,
            onBackground = Color.White
        )
        ThemeMode.AZUL -> lightColorScheme(
            primary = Color(0xFF0D1317),
            secondary = Color(0xFF6564DB),
            background = Color(0xFFA3C9D3),
            onBackground = Color.White
        )

        ThemeMode.ROSA -> darkColorScheme(
            primary = Color(0xFFFFC2D1),
            secondary = Color(0xFFCD647E),
            background = Color(0xFF965364),
            onBackground = Color(0xFFFFE5EC)
        )

        ThemeMode.VERDE -> lightColorScheme(
            primary = Color(0xFF255939),
            secondary = Color(0xFF48AF71),
            background = Color(0xFFC3E8D4),
            onBackground = Color(0xFF88EDAF)
        )

    }

    MaterialTheme(
        colorScheme = selectedColors,
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
                PantallaPrincipal(
                    navController = navController,
                    themeMode = themeMode,
                    onThemeChange = onThemeChange
                )
            }
            composable("nuevo") {
                PantallaFormularioProducto(navController = navController)
            }
            composable("editar/{id}", arguments = listOf(
                navArgument("id") { type = NavType.StringType }
            )) { backStackEntry ->
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
fun PantallaPrincipal(
    navController: NavController,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    val context = LocalContext.current
    val db = remember { DBHelper(context) }

    var comandas by remember { mutableStateOf(db.getAllComandas()) }
    var mostrarAyuda by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    suspend fun sincronizarComandasDesdeMySQL(): List<Comanda> = withContext(Dispatchers.IO) {
        MySQLHelper.obtenerComandas()
    }

    suspend fun enviarComandasLocalesAMySQL() = withContext(Dispatchers.IO) {
        val locales = db.getAllComandas()
        locales.forEach { comanda ->
            MySQLHelper.actualizarComanda(
                id = comanda.id,
                mesa = comanda.mesa,
                pedido = comanda.pedido,
                precio = comanda.precio,
                estado = comanda.estado
            )
        }
    }

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

                        val nextTheme = when (themeMode) {
                            ThemeMode.LIGHT -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.AZUL
                            ThemeMode.AZUL -> ThemeMode.ROSA
                            ThemeMode.ROSA -> ThemeMode.VERDE
                            ThemeMode.VERDE -> ThemeMode.LIGHT
                        }
                        onThemeChange(nextTheme)
                    }
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Comandas", style = MaterialTheme.typography.headlineSmall) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.List, contentDescription = "Menú")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            scope.launch {
                                cargando = true
                                val nuevasComandas = try {
                                    sincronizarComandasDesdeMySQL()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    snackbarHostState.showSnackbar("Error al sincronizar desde MySQL")
                                    null
                                }
                                if (nuevasComandas != null) {
                                    nuevasComandas.forEach { comanda ->
                                        db.insertarComandaConId(
                                            id = comanda.id,
                                            mesa = comanda.mesa,
                                            pedido = comanda.pedido,
                                            precio = comanda.precio,
                                            estado = comanda.estado
                                        )
                                    }
                                    comandas = db.getAllComandas()
                                    snackbarHostState.showSnackbar("Sincronización completada")
                                }
                                cargando = false
                            }
                        }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Sincronizar")
                        }

                        IconButton(onClick = {
                            scope.launch {
                                cargando = true
                                try {
                                    enviarComandasLocalesAMySQL()
                                    snackbarHostState.showSnackbar("Datos enviados a MySQL")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    snackbarHostState.showSnackbar("Error al enviar a MySQL")
                                }
                                cargando = false
                            }
                        }) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Enviar datos")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("nuevo") },
                    containerColor = Color(0xFF7798AB)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar")
                }
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
                    items(comandas) { comanda ->
                        ComandaCard(
                            comanda = comanda,
                            onDelete = {
                                scope.launch {
                                    cargando = true
                                    try {
                                        db.deleteComanda(comanda.id)
                                        val exito = MySQLHelper.eliminarComanda(comanda.id)
                                        if (exito) {
                                            snackbarHostState.showSnackbar("Comanda eliminada correctamente")
                                        } else {
                                            snackbarHostState.showSnackbar("Se eliminó localmente, pero falló en el servidor")
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        snackbarHostState.showSnackbar("Error al eliminar comanda")
                                    }
                                    comandas = db.getAllComandas()
                                    cargando = false
                                }
                            },
                            onEdit = {
                                navController.navigate("editar/${comanda.id}")
                            },
                            onEstadoChange = { estado ->
                                db.actualizarEstado(comanda.id, estado)
                                comandas = db.getAllComandas()
                            },
                            themeMode = themeMode
                        )
                    }
                }
            }

            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }

        if (mostrarAyuda) {
            AlertDialog(
                onDismissRequest = { mostrarAyuda = false },
                confirmButton = {
                    TextButton(onClick = { mostrarAyuda = false }) {
                        Text("Entendido", color = Color(0xFF2E7D32))
                    }
                },
                title = { Text("Tutorial de la App") },
                text = {
                    Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            "• Pulsa el botón '+' para agregar una nueva comanda.\n" +
                                    "• Usa '✏️' para editar y '❌' para eliminar comandas existentes.\n" +
                                    "• Cambia el estado de la comanda con '🧑🏼‍🍳' (En preparación) y '✅' (Terminada).\n" +
                                    "• Abre el menú para volver a ver esta ayuda.\n" +
                                    "• Desde los ⋮ puedes cambiar el tema de la aplicación.\n" +
                                    "• Pulsa la flecha ↻ para sincronizar los datos.\n" +
                                    "• Pulsa ☁️ para enviar los datos locales al servidor."
                        )
                    }
                }
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormularioProducto(navController: NavController, comandaId: Int? = null) {
    val context = LocalContext.current
    val db = remember { DBHelper(context) }

    var mesa by remember { mutableStateOf("") }
    var pedido by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }

    var precioError by remember { mutableStateOf(false) }
    var mesaError by remember { mutableStateOf(false) }

    LaunchedEffect(comandaId) {
        if (comandaId != null) {
            val comanda = db.getComandaPorId(comandaId)
            comanda?.let {
                mesa = it.mesa.toString()
                pedido = it.pedido ?: ""
                precio = it.precio.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (comandaId == null) "Nueva Comanda" else "Editar Comanda", fontFamily = poppins) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState()), // <- para que el contenido se pueda desplazar
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            var pedidoError by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = mesa,
                onValueChange = {
                    mesa = it
                    mesaError = it.isBlank() },
                label = { Text("Mesa") },
                isError = mesaError,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                singleLine = true
            )
            if (mesaError) {
                Text(
                    text = "El campo no puede estar vacío",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = pedido,
                onValueChange = {
                    pedido = it
                    pedidoError = it.isBlank()
                },
                label = { Text("Pedido") },
                isError = pedidoError,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                singleLine = true
            )
            if (pedidoError) {
                Text(
                    text = "El campo no puede estar vacío",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = precio,
                onValueChange = {
                    precio = it
                    precioError = it.toDoubleOrNull()?.let { n -> n < 0 } ?: true },
                label = { Text("Total") },
                isError = precioError,
                modifier = Modifier.fillMaxWidth().height(60.dp), singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp)) // separación entre campos y botón
            Button(
                onClick = {
                    mesaError = mesa.isBlank()
                    pedidoError = pedido.isBlank()
                    val precioDoubleOrNull = precio.toDoubleOrNull()
                    precioError = precioDoubleOrNull == null || precioDoubleOrNull < 0

                    if (!mesaError && !pedidoError && !precioError) {
                        val precioFinal = precioDoubleOrNull ?: 0.0

                        if (comandaId == null) {
                            db.insertarComanda(
                                mesa = mesa,
                                pedido = pedido,
                                precio = precioFinal,
                                estado = "Pendiente"
                            )
                        } else {
                            val comandaActual = db.getComandaPorId(comandaId)
                            val estadoActual = comandaActual?.estado ?: "Pendiente"
                            db.actualizarComanda(
                                id = comandaId,
                                mesa = mesa,
                                pedido = pedido,
                                precio = precioFinal,
                                estado = estadoActual
                            )
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF453643),
                    contentColor = Color.White
                )
            ) {
                Text("Guardar Comanda")
            }
        }
    }
}

@Composable
fun ComandaCard(
    comanda: Comanda, onDelete: () -> Unit, onEdit: () -> Unit, onEstadoChange: (String) -> Unit, themeMode: ThemeMode
) {
    // Colores para las tarjetas, botones y texto según el tema
    val (cardColor, buttonColor, textColor) = when (themeMode) {
        ThemeMode.LIGHT -> Triple(
            Color(0xFFE8DCB9), Color(0xFFC7A27C), Color.Black
        )
        ThemeMode.DARK -> Triple(
            Color(0xFF18314F), Color(0xFF212738), Color.White
        )
        ThemeMode.AZUL -> Triple(
            Color(0xFF101D42), Color(0xFF4A49C0), Color.White
        )
        ThemeMode.ROSA -> Triple(
            Color(0xFFECA3B5), Color(0xFFD97991), Color.White
        )
        ThemeMode.VERDE -> Triple(
            Color(0xB93D9861), Color(0xDA44DA7E), Color.White
        )
    }

    fun getEstadoColor(estado: String): Color {
        return when (estado) {
            "Pendiente" -> Color.Gray
            "En proceso" -> Color(0xFFFFC107) // Amarillo tipo Material
            "Terminada" -> Color(0xFF4CAF50)  // Verde tipo Material
            else -> Color.LightGray
        }
    }

    // Componente Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Mesa: ${comanda.mesa}", fontWeight = FontWeight.Bold, color = textColor)
                comanda.pedido?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(it, fontSize = 16.sp, color = textColor)
                }
                Text("Precio: \$${String.format("%.2f", comanda.precio)}", fontSize = 14.sp, color = textColor)

                Spacer(modifier = Modifier.height(8.dp))

                // ✅ Estado en su propio renglón
                Text(
                    text = "Estado: ${comanda.estado}",
                    color = getEstadoColor(comanda.estado),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // ✅ Botones abajo del estado
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onEstadoChange("En proceso") },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = buttonColor,
                            contentColor = textColor
                        )
                    ) {
                        Text("🧑🏼‍🍳")
                    }

                    OutlinedButton(
                        onClick = { onEstadoChange("Terminada") },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = buttonColor,
                            contentColor = textColor
                        )
                    ) {
                        Text("✅")
                    }

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
        }
    }
}