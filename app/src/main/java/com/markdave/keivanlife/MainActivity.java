package com.markdave.keivanlife;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** Full-screen landscape shell around the game page in assets/index.html. */
public class MainActivity extends Activity {
    private static final int REQ_CAMERA = 11;
    private static final int REQ_PICK = 12;
    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }
        web = new WebView(this);
        web.setBackgroundColor(0xFF0B0F19);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        s.setTextZoom(100);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        web.addJavascriptInterface(new Bridge(), "AndroidBridge");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http")) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception ignored) { }
                    return true;
                }
                return false;
            }
        });
        web.loadUrl("file:///android_asset/index.html");
        hideBars();
    }

    private void hideBars() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideBars();
    }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript("(window.keiBack?window.keiBack():false)", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (!"true".equals(value)) finish();
            }
        });
    }

    @Override
    protected void onPause() {
        web.evaluateJavascript("window.keiPause&&window.keiPause()", null);
        web.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
        hideBars();
    }

    @Override
    protected void onDestroy() {
        web.destroy();
        super.onDestroy();
    }

    /** Camera thumbnail or gallery picture comes back here and is handed to the page as a data URL. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;
        try {
            Bitmap bmp = null;
            if (requestCode == REQ_CAMERA && data.getExtras() != null) {
                Object o = data.getExtras().get("data");
                if (o instanceof Bitmap) bmp = (Bitmap) o;
            } else if (requestCode == REQ_PICK && data.getData() != null) {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;
                InputStream in = getContentResolver().openInputStream(data.getData());
                BitmapFactory.decodeStream(in, null, opt);
                if (in != null) in.close();
                int sample = 1;
                while (Math.max(opt.outWidth, opt.outHeight) / sample > 1000) sample *= 2;
                opt = new BitmapFactory.Options();
                opt.inSampleSize = sample;
                in = getContentResolver().openInputStream(data.getData());
                bmp = BitmapFactory.decodeStream(in, null, opt);
                if (in != null) in.close();
            }
            if (bmp == null) {
                Toast.makeText(this, "That photo could not be opened.", Toast.LENGTH_SHORT).show();
                return;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, out);
            String b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
            web.evaluateJavascript("window.keiPhoto&&window.keiPhoto('data:image/jpeg;base64," + b64 + "')", null);
        } catch (Exception e) {
            Toast.makeText(this, "That photo could not be opened.", Toast.LENGTH_SHORT).show();
        }
    }

    private void launch(final Intent intent, final int code, final String problem) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    startActivityForResult(intent, code);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, problem, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class Bridge {
        @JavascriptInterface
        public void takePhoto() {
            launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQ_CAMERA, "No camera app was found. Choose a photo instead.");
        }

        @JavascriptInterface
        public void pickPhoto() {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            i.addCategory(Intent.CATEGORY_OPENABLE);
            launch(Intent.createChooser(i, "Choose a photo of your van"), REQ_PICK, "No photo app was found.");
        }

        @JavascriptInterface
        public String version() {
            try {
                return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (Exception e) {
                return "3.0";
            }
        }

        @JavascriptInterface
        public void quit() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }

        @JavascriptInterface
        public void share(final String text) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT, text);
                    startActivity(Intent.createChooser(i, "Share Kei Van Life"));
                }
            });
        }

        @JavascriptInterface
        public void copy(final String text) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("Kei Van Life", text));
                }
            });
        }
    }
}
