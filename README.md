# Cachupin 🐾 (Android)

Aplicación Android (Jetpack Compose) orientada a una experiencia tipo e-commerce/veterinaria para **visualizar productos**, **agregar al carrito**, **gestionar perfil**, y **agendar una cita**.  
> **Importante:** La obtención/almacenamiento de datos del proyecto se realiza principalmente mediante **Firebase** (Auth, **Firestore** y Storage).

## 🔗 Repositorio
- GitHub: https://github.com/MatiasNeira1/Cachupin-Android.git

---

## 📌 Contexto
**Cachupin** busca centralizar funcionalidades comunes para una app de mascotas:
- Catálogo de productos con imagen y stock.
- Carrito de compras.
- Registro e inicio de sesión.
- Perfil del usuario.
- Agendamiento de una cita (se guarda en **Firestore** y opcionalmente se inserta en el **Calendario** del dispositivo).
- Módulo de “ScanPet” con cámara (preview) usando **CameraX**.

---

## ✅ Requerimientos (funcionales)
- Registrar usuario y autenticar (Firebase Auth).
- Almacenamiento de perfiles de usuario (Firestore / colección `usuarios`).
- Mostrar listado de productos desde base de datos (Firestore / colección `productos`).
- Visualización de imágenes desde Firebase Storage (por ruta `gs://...` o URL).
- Carrito persistente (Firestore / colección `carrito`) y gestión de cantidades/eliminación.
- Agendar cita: inserción en calendario del dispositivo + registro en Firestore (colección `citas`).

## ⚙️ Requerimientos (técnicos)
- Android Studio + Gradle Wrapper.
- **minSdk 24** | **targetSdk 36** | **compileSdk 36**
- Kotlin + Jetpack Compose + Navigation Compose.
- Firebase: Auth, Firestore, Storage (y dependencias adicionales incluidas en el proyecto).

---

## 🚀 Funcionalidades implementadas (Firebase / Firestore)

### 1) “API externa tipo GET” (lectura de datos)
En este proyecto, la “API externa” corresponde al **consumo del SDK de Firebase**, principalmente **Firestore**, para leer información:
- **Productos**: lectura desde `productos` (Catálogo/menú/destacados).
- **Perfil**: lectura desde `usuarios/{uid}`.
- **Carrito**: lectura desde `carrito`.

> En términos prácticos, estas lecturas equivalen a un **GET** hacia una fuente externa (Firebase/Firestore), pero usando el SDK oficial.

### 2) Backend propio con CRUD (capa Repository)
Se implementa una capa de repositorios para encapsular operaciones **Create / Read / Update / Delete** sobre Firestore:

- **AuthRepository**
  - Registro e inicio de sesión (Auth) y persistencia de datos en `usuarios`.

- **ProductosRepository**
  - Lectura reactiva/listener de productos.
  - Agregar al carrito y **actualizar stock** del producto en Firestore.

- **CartRepository / CartStorage**
  - Cargar carrito desde Firestore (`carrito`).
  - Actualizar cantidad de ítems.
  - Eliminar ítems (y ajuste de stock cuando aplique).

- **ProfileRepository**
  - Obtención del perfil del usuario desde `usuarios/{uid}`.

- **Citas / DatePicker**
  - Guardar cita en **Firestore** (`citas`) y crear evento en Calendario del dispositivo.

---

## 🧱 Modelo de datos (Firestore)

### `usuarios` (documento = UID)
Campos típicos:
- `nombre` (String)
- `email` (String)
- `createdAt` (Long)

### `productos`
Campos (según `Producto.kt`):
- `nombre` (String)
- `descripcion` (String)
- `categoria` (String)
- `imageUrl` (String)
- `peso` (String)
- `precio` (Int)
- `material` (String)
- `stock` (Int)

### `carrito`
Campos (según `CarritoItem.kt`):
- `nombre` (String)
- `precio` (Int)
- `qty` (Int)
- `categoria` (String)
- `imageUrl` (String)

### `citas`
Campos típicos:
- `usuarioId` (String)
- `fecha` (Long/millis)
- `titulo` (String)
- `descripcion` (String)
- `hora` (String)

---

## 🗂️ Estructura del proyecto (resumen)

- `app/src/main/java/com/example/cachupin/`
  - `MainActivity.kt` (Navigation / Routes)
- `domain/` (modelos)
  - `Producto`, `CarritoItem`, `UserProfile`, `Cita`
- `backend/data/repository/`
  - repositorios Firebase/Firestore
- `frontend/viewmodel/`
  - ViewModels (estado UI)
- `frontend/ui/screens/`
  - pantallas Compose (Login, Register, Menu, Productos, Carrito, Profile, DatePicker, ScanPet)

---

## ▶️ Cómo ejecutar (Android Studio)

1. Clona el repo:
   ```bash
   git clone https://github.com/MatiasNeira1/Cachupin-Android.git
