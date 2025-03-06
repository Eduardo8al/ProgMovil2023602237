class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App1Theme {
                MenuScreen()
            }
        }
    }
}

@Composable
fun MenuScreen() {
    var selectedOption by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { selectedOption = 1 }) { Text("Sumar números") }
        Button(onClick = { selectedOption = 2 }) { Text("Ingresar nombre") }
        Button(onClick = { selectedOption = 3 }) { Text("Calcular edad") }
        Button(onClick = { selectedOption = 4 }) { Text("Salir") }

        when (selectedOption) {
            1 -> SumarNumeros()
            2 -> IngresarNombre()
            3 -> CalcularEdad()
            4 -> Text("Saliendo...")
        }
    }
}

@Composable
fun SumarNumeros() {
    var num1 by remember { mutableStateOf("") }
    var num2 by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf("") }

    Column {
        OutlinedTextField(value = num1, onValueChange = { num1 = it }, label = { Text("Número 1") })
        OutlinedTextField(value = num2, onValueChange = { num2 = it }, label = { Text("Número 2") })
        Button(onClick = {
            resultado = ((num1.toIntOrNull() ?: 0) + (num2.toIntOrNull() ?: 0)).toString()
        }) { Text("Sumar") }
        if (resultado.isNotEmpty()) Text("Resultado: $resultado")
    }
}

@Composable
fun IngresarNombre() {
    var nombre by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
        Text("Hola, $nombre!")
    }
}

@Composable
fun CalcularEdad() {
    var anio by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf("") }

    Column {
        OutlinedTextField(value = anio, onValueChange = { anio = it }, label = { Text("Año de nacimiento") })
        Button(onClick = {
            resultado = (2024 - (anio.toIntOrNull() ?: 0)).toString()
        }) { Text("Calcular") }
        if (resultado.isNotEmpty()) Text("Edad: $resultado años")
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMenuScreen() {
    App1Theme {
        MenuScreen()
    }
}