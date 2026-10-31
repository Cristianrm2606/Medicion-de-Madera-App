package com.madera.hoja;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(PdfPlugin.class);
        super.onCreate(savedInstanceState);
        // Que la letra grande del sistema (p. ej. en Samsung) no agrande la app y la saque de la pantalla
        getBridge().getWebView().getSettings().setTextZoom(100);
    }
}
