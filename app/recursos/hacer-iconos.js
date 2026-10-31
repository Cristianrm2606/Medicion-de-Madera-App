// Genera los íconos de Android a partir de recursos/icono-original.webp
// Uso: node recursos/hacer-iconos.js
const sharp = require('sharp');
const path = require('path');

const original = path.join(__dirname, 'icono-original.webp');
const res = path.join(__dirname, '..', 'android', 'app', 'src', 'main', 'res');
const FONDO = '#14301d';

// Medidas del dibujo original (1254 px): el borde verde va de 31 a 1221 con esquinas de radio ~250,
// y el paisaje de adentro va de ~62 a ~1192 (se recorta un poco más para no agarrar la orilla verde)
const borde = { x: 31, lado: 1191, radio: 250 };
const adentro = { x: 72, lado: 1110, radio: 190 };
// Tamaño del dibujo dentro de la capa de 108dp: el paisaje llena casi todo, sin cortar la cinta en los íconos redondos
const ESCALA = 70;

const densidades = { mdpi: 1, hdpi: 1.5, xhdpi: 2, xxhdpi: 3, xxxhdpi: 4 };

const esquinas = (lado, radio) => Buffer.from(
  `<svg width="${lado}" height="${lado}"><rect width="${lado}" height="${lado}" rx="${radio}" fill="#fff"/></svg>`);
const circulo = lado => Buffer.from(
  `<svg width="${lado}" height="${lado}"><circle cx="${lado / 2}" cy="${lado / 2}" r="${lado / 2}" fill="#fff"/></svg>`);

async function recortar({ x, lado, radio }) {
  return sharp(original)
    .extract({ left: x, top: x, width: lado, height: lado })
    .ensureAlpha()
    .composite([{ input: esquinas(lado, radio), blend: 'dest-in' }])
    .png()
    .toBuffer();
}

(async () => {
  const completo = await recortar(borde);   // con el borde verde, para Android viejo
  const dibujo = await recortar(adentro);   // solo el paisaje, para el ícono adaptable

  for (const [nombre, d] of Object.entries(densidades)) {
    const carpeta = path.join(res, 'mipmap-' + nombre);

    // Ícono adaptable (Android 8+): capa de 108dp; lo que se ve seguro es el centro de ~66dp
    const capa = Math.round(108 * d);
    const tam = Math.round(capa * ESCALA / 108);
    const centro = Math.round((capa - tam) / 2);
    const frente = await sharp({ create: { width: capa, height: capa, channels: 4, background: { r: 0, g: 0, b: 0, alpha: 0 } } })
      .composite([{ input: await sharp(dibujo).resize(tam, tam).toBuffer(), left: centro, top: centro }])
      .png().toBuffer();
    await sharp(frente).toFile(path.join(carpeta, 'ic_launcher_foreground.png'));

    // Íconos de Android viejo (48dp): cuadrado redondeado con borde verde, y versión redonda
    const lado = Math.round(48 * d);
    await sharp(completo).resize(lado, lado).toFile(path.join(carpeta, 'ic_launcher.png'));
    const redondo = await sharp({ create: { width: capa, height: capa, channels: 4, background: FONDO } })
      .composite([{ input: frente }]).png().toBuffer();
    await sharp(redondo)
      .extract({ left: Math.round(18 * d), top: Math.round(18 * d), width: Math.round(72 * d), height: Math.round(72 * d) })
      .resize(lado, lado)
      .composite([{ input: circulo(lado), blend: 'dest-in' }])
      .toFile(path.join(carpeta, 'ic_launcher_round.png'));
  }

  // Vista previa para revisar cómo queda en círculo y en cuadro redondeado
  const capa = 432;
  const frente = await sharp(path.join(res, 'mipmap-xxxhdpi', 'ic_launcher_foreground.png')).toBuffer();
  const vista = await sharp({ create: { width: capa, height: capa, channels: 4, background: FONDO } })
    .composite([{ input: frente }]).png().toBuffer();
  const visible = await sharp(vista).extract({ left: 72, top: 72, width: 288, height: 288 }).toBuffer();
  const enCirculo = await sharp(visible).composite([{ input: circulo(288), blend: 'dest-in' }]).png().toBuffer();
  const enCuadro = await sharp(visible).composite([{ input: esquinas(288, 70), blend: 'dest-in' }]).png().toBuffer();
  await sharp({ create: { width: 940, height: 320, channels: 4, background: '#e8e8e8' } })
    .composite([
      { input: enCirculo, left: 16, top: 16 },
      { input: enCuadro, left: 326, top: 16 },
      { input: await sharp(completo).resize(288, 288).toBuffer(), left: 636, top: 16 },
    ])
    .png().toFile(path.join(__dirname, 'vista-iconos.png'));

  console.log('Íconos listos');
})();
