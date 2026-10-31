package com.madera.hoja;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/** Manda el PDF de la hoja directo a WhatsApp o lo guarda en la carpeta Descargas. */
@CapacitorPlugin(name = "Pdf")
public class PdfPlugin extends Plugin {

    private static final String[] WHATSAPP = { "com.whatsapp", "com.whatsapp.w4b" };

    @PluginMethod
    public void whatsapp(PluginCall call) {
        File archivo = archivo(call);
        if (archivo == null) return;
        Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", archivo);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // Primero WhatsApp normal y si no está, WhatsApp Business
        for (String paquete : WHATSAPP) {
            intent.setPackage(paquete);
            try {
                getActivity().startActivity(intent);
                call.resolve();
                return;
            } catch (ActivityNotFoundException e) {
                // probar el siguiente
            }
        }
        call.reject("WhatsApp no está instalado en este teléfono", "SIN_WHATSAPP");
    }

    @PluginMethod
    public void descargar(PluginCall call) {
        File archivo = archivo(call);
        if (archivo == null) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Android 9 o más viejo necesita otro permiso; la app usa el menú de compartir en su lugar
            call.reject("Android muy viejo para guardar en Descargas", "ANDROID_VIEJO");
            return;
        }
        String nombre = call.getString("nombre", archivo.getName());
        ContentResolver resolver = getContext().getContentResolver();
        ContentValues datos = new ContentValues();
        datos.put(MediaStore.Downloads.DISPLAY_NAME, nombre);
        datos.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
        datos.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        Uri destino = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, datos);
        if (destino == null) {
            call.reject("No se pudo crear el archivo en Descargas");
            return;
        }
        try (InputStream entrada = new FileInputStream(archivo); OutputStream salida = resolver.openOutputStream(destino)) {
            byte[] bufer = new byte[8192];
            int leidos;
            while ((leidos = entrada.read(bufer)) > 0) salida.write(bufer, 0, leidos);
        } catch (Exception e) {
            resolver.delete(destino, null, null);
            call.reject("No se pudo guardar el PDF: " + e.getMessage());
            return;
        }
        JSObject resultado = new JSObject();
        resultado.put("uri", destino.toString());
        call.resolve(resultado);
    }

    @PluginMethod
    public void abrir(PluginCall call) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(call.getString("uri")), "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getActivity().startActivity(intent);
            call.resolve();
        } catch (ActivityNotFoundException e) {
            call.reject("No hay una app para abrir PDF en este teléfono", "SIN_VISOR");
        }
    }

    private File archivo(PluginCall call) {
        String ruta = call.getString("ruta");
        File archivo = ruta == null ? null : new File(Uri.parse(ruta).getPath());
        if (archivo == null || !archivo.exists()) {
            call.reject("No se encontró el PDF");
            return null;
        }
        return archivo;
    }
}
