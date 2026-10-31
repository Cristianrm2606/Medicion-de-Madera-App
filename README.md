# Medir Madera 🪵

App para Android que sirve para medir la madera que se le compra a un proveedor: se van anotando las piezas, la app calcula las **pulgadas** de cada una y cuánto hay que **pagar**, guarda las hojas en el teléfono y las saca en **PDF** para mandarlas por WhatsApp o imprimirlas.

Funciona **sin internet**: todo se guarda en el teléfono.

---

## Qué hace

- **Anotar piezas**: se escribe la *medida* y el *largo* con un teclado grande en pantalla y se toca **Agregar**.
  - Botones rápidos de largo: **2, 3, 4, 5 y 6**. El largo se queda puesto para la siguiente pieza.
  - Al tocar una casilla que ya tiene número, el número nuevo **reemplaza** al anterior (si había un 4 y se escribe 5, queda 5, no 45).
  - La medida solo acepta números enteros; el largo acepta decimales.
- **Desglose en vivo**: cada pieza sale de primera en la lista con sus pulgadas y su precio. Si hubo un error se borra con la ✕ roja (y se puede **Deshacer**).
- **Totales**: en la esquina de arriba se ve lo que hay que pagar, el total de pulgadas y la cantidad de piezas.
- **Hojas guardadas**: cada hoja queda con proveedor, fecha, piezas, pulgadas y total. Desde ahí se puede:
  - **Abrir** la hoja para revisarla o corregirla.
  - **Ver PDF** dentro de la app, sin descargar nada.
  - **Enviar**: mandar por WhatsApp, guardar en Descargas, o compartir por correo, Drive, imprimir…
  - **∑ Sumar todas las hojas**: total de hojas, pulgadas y colones de todo lo guardado.
- **PDF** tamaño carta con proveedor, fecha, tabla de piezas (medida, pulgadas, precio, importe) y el total a pagar en colones (₡).

---

## Fórmulas

### 1. Pulgadas de una pieza

Las piezas se anotan como **MEDIDA × LARGO** (por ejemplo `7 x 4`), igual que en la hoja de papel del proveedor.

```
pulgadas = medida × medida × largo ÷ 4
```

La medida va dos veces porque la pieza es cuadrada (mismo ancho que grueso): `medida × medida` da el área de la punta, al multiplicar por el largo sale el volumen, y el `÷ 4` lo pasa a la unidad con la que se compra y se vende.

Cuando el largo es **4**, el `× 4` y el `÷ 4` se cancelan y el resultado es simplemente la medida al cuadrado:

| Medida | Cuenta | Pulgadas |
|---|---|---|
| 7 x 4 | 7 × 7 × 4 ÷ 4 | **49** |
| 8 x 4 | 8 × 8 × 4 ÷ 4 | **64** |
| 9 x 4 | 9 × 9 × 4 ÷ 4 | **81** |
| 10 x 4 | 10 × 10 × 4 ÷ 4 | **100** |
| 6 x 6 | 6 × 6 × 6 ÷ 4 | **54** |
| 7 x 3 | 7 × 7 × 3 ÷ 4 | **36,75** |
| 9 x 2 | 9 × 9 × 2 ÷ 4 | **40,5** |

### 2. Precio por pulgada

| Medida | Precio por pulgada |
|---|---|
| **8 o menos** | ₡80 |
| **9 o más** | ₡100 |

```
importe de la pieza = pulgadas × precio
```

Ejemplos:

- `7 x 4` → 49 pulgadas × ₡80 = **₡3.920**
- `9 x 4` → 81 pulgadas × ₡100 = **₡8.100**

Cada pieza guarda el precio con el que se anotó, así las hojas viejas no cambian si algún día cambia el precio.

### 3. Totales de la hoja

```
total de pulgadas = suma de las pulgadas de todas las piezas
total a pagar     = suma de los importes de todas las piezas
```

Ejemplo con 4 piezas:

| Pieza | Pulgadas | Precio | Importe |
|---|---|---|---|
| 7 x 4 | 49 | ₡80 | ₡3.920 |
| 8 x 4 | 64 | ₡80 | ₡5.120 |
| 9 x 4 | 81 | ₡100 | ₡8.100 |
| 10 x 4 | 100 | ₡100 | ₡10.000 |
| **Total** | **294** | | **₡27.140** |

### 4. Suma de todas las hojas

El botón **∑ Sumar todas las hojas** suma las pulgadas y los colones de todas las hojas guardadas.

---

## Cómo está hecha

La app es **híbrida**: la pantalla está hecha con tecnologías web (HTML, CSS y JavaScript) y corre dentro de una app nativa de Android gracias a **[Capacitor](https://capacitorjs.com/)**. Por eso en GitHub sale mucho HTML: casi toda la app está en `app/www/index.html`.

| Parte | Tecnología |
|---|---|
| Pantalla y lógica | HTML, CSS y JavaScript (`app/www/index.html`) |
| App de Android | Capacitor 8 (Java / Gradle) |
| Base de datos | SQLite en el teléfono (`@capacitor-community/sqlite`) |
| PDF | [jsPDF](https://github.com/parallax/jsPDF) con la letra DejaVu Sans (trae el símbolo ₡) |
| Vista previa del PDF | [pdf.js](https://mozilla.github.io/pdf.js/) |
| WhatsApp y Descargas | Plugin nativo propio (`PdfPlugin.java`) |
| Compartir | `@capacitor/share` y `@capacitor/filesystem` |

### Base de datos

| Tabla | Qué guarda |
|---|---|
| `hojas` | `id`, `proveedor`, `fecha`, `guardada` |
| `piezas` | `id`, `hoja_id`, `medida`, `largo`, `precio` |
| `ajustes` | la hoja que se está llenando y los precios |

### Carpetas

```
calculadora-madera.html        Primera versión: calculadora web sencilla
app/
├── www/                       La app (lo que se ve en pantalla)
│   ├── index.html             Pantalla, fórmulas, SQLite y PDF
│   ├── capacitor.js           Puente con Android
│   ├── jspdf.umd.min.js       Para crear el PDF
│   ├── pdfjs/                 Para ver el PDF dentro de la app
│   └── fuentes/               Letra DejaVu Sans (con ₡)
├── android/                   Proyecto nativo de Android
│   └── app/src/main/java/com/madera/hoja/
│       ├── MainActivity.java  Arranque de la app
│       └── PdfPlugin.java     WhatsApp, Descargas y abrir PDF
├── recursos/
│   ├── icono-original.webp    Ícono de la app
│   └── hacer-iconos.js        Genera los íconos de Android
├── capacitor.config.json
└── package.json
```

---

## Cómo compilar el APK

Se necesita **Node.js**, **Android Studio** (con el SDK de Android) y **JDK 21** (el que trae Android Studio sirve).

```bash
cd app
npm install
npx cap sync android
cd android
./gradlew assembleDebug
```

El APK queda en `app/android/app/build/outputs/apk/debug/app-debug.apk`. Se pasa al teléfono y se instala (hay que permitir *instalar apps desconocidas*).

Para cambiar el ícono: reemplazar `app/recursos/icono-original.webp` y correr `node recursos/hacer-iconos.js` dentro de `app/`.

Para probar la pantalla en la computadora basta con abrir `app/www/index.html` desde un servidor local; ahí usa SQLite en memoria (no guarda).
