Objetivo
Desarrollar e implementar una aplicación móvil que permita gestionar de forma eficiente las comandas de una cafeteria, facilitando el registro,
edicion, visualización y sincronización de pedidos, tanto de manera local mediante SQLite como de forma remota a través de MySQL con el uso de clever cloud
asegurando así una operación continua.

Alcance del proyecto
-La aplicación permitirá registrar nuevas comandas y editarlas, con datos como número de mesa, descripción del pedido, precio y estado.
-Se podrán cambiar los estados de los pedidos entre "En preparación" y "Terminada".
-La app cuenta con un sistema de sincronización bidireccional con un servidor MySQL, lo que permite mantener actualizadas las comandas entre dispositivos.
-El diseño de la interfaz está pensado para ser intuitivo y adaptable a diferentes temas visuales.
-Se incluye un panel de ayuda interactivo y personalización de apariencia mediante selección de temas.
-El sistema no contempla por ahora autenticación de usuarios ni control de roles (meseros, cocina, administrador), pero puede ser ampliado fácilmente.

Consultas útiles en MySQL
SELECT * FROM comanda ORDER BY id_comanda DESC;
SELECT * FROM comanda WHERE estado = 'En preparación';
DELETE FROM comanda WHERE estado = 'Terminada';
SELECT * FROM comanda WHERE mesa = 3;

Requerimientos técnicos

Herramientas de Desarrollo
-Android Studio: Versión Hedgehog | 2023.1.1 o superior
-Lenguaje de programación: Kotlin
-Jetpack Compose para UI
-Corrutinas (kotlinx.coroutines) para sincronización asíncrona

Adicionales:
-Material 3 (androidx.compose.material3)
-Navigation Compose (androidx.navigation:navigation-compose)
-JDBC (Conector .jar de MySQL: mysql-connector-java 5.1.x compatible con com.mysql.jdbc.Driver)
-Emulador o dispositivo físico con Android API 26 (Android 8.0 Oreo) o superior.

Base de Datos Local
-Motor local: SQLite (integrado en Android)
-DBHelper personalizado en Kotlin para CRUD local
-Las comandas se guardan localmente y pueden sincronizarse con el servidor MySQL.

Base de Datos Remota (Servidor)
-Proveedor de hosting: Clever Cloud

Herramienta de administración recomendada:
-MySQL Workbench versión 8.0 o superior (para consultas y mantenimiento)

Equipo de desarrollo conformado por:

Elías Vázquez Esly Fabiola
Correo: eslyfabiola10@gmail.com

Ochoa López Angel Eduardo de Jesús
Correo: ochoa.eduardo.v11@gmail.com






