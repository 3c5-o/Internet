package com.internet.linkextractor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(11, 16, 32);
    private static final int SURFACE = Color.rgb(20, 28, 47);
    private static final int SURFACE_ALT = Color.rgb(27, 39, 64);
    private static final int GREEN = Color.rgb(41, 211, 145);
    private static final int WHITE = Color.rgb(244, 247, 251);
    private static final int MUTED = Color.rgb(167, 179, 199);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private LinearLayout browserRoot;
    private WebView webView;
    private EditText addressInput;
    private TextView browserStatus;
    private ProgressBar progressBar;
    private boolean browserVisible = false;
    private final List<ArchiveItem> accountItems = new ArrayList<>();
    private final Set<String> selectedArchiveIds = new LinkedHashSet<>();
    private String accountUsername = "";
    private int accountTotal = 0;
    private int accountNextStart = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        if (Build.VERSION.SDK_INT >= 29) {
            getWindow().setNavigationBarContrastEnforced(false);
            getWindow().setStatusBarContrastEnforced(false);
        }
        CookieManager.getInstance().setAcceptCookie(true);
        showHome();
    }

    private void showHome() {
        browserVisible = false;

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        page.setPadding(dp(22), dp(26), dp(22), dp(22));
        scroll.addView(page, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView brand = text("INTERNET ARCHIVE  •  VIDEO LINKS", 11, GREEN, true);
        brand.setLetterSpacing(0.04f);
        page.addView(brand, fullWidth());

        TextView title = text("مستخرج روابط الفيديو", 27, WHITE, true);
        title.setPadding(0, dp(10), 0, dp(4));
        page.addView(title, fullWidth());

        TextView subtitle = text(
                "تصفّح Internet Archive، افتح صفحة العنصر، واستخرج روابط ملفات الفيديو المتاحة لحسابك.",
                15, MUTED, false);
        subtitle.setLineSpacing(dp(4), 1.0f);
        page.addView(subtitle, fullWidth());

        LinearLayout infoCard = new LinearLayout(this);
        infoCard.setOrientation(LinearLayout.VERTICAL);
        infoCard.setPadding(dp(16), dp(15), dp(16), dp(15));
        infoCard.setBackground(rounded(SURFACE, 18));
        LinearLayout.LayoutParams cardParams = fullWidth();
        cardParams.topMargin = dp(22);
        page.addView(infoCard, cardParams);

        TextView infoTitle = text("طريقة الاستخدام", 17, WHITE, true);
        infoCard.addView(infoTitle, fullWidth());

        TextView infoBody = text(
                "١. افتح الموقع وسجّل الدخول.\n٢. افتح صفحتك الشخصية من My Library.\n٣. اضغط «فيديوهاتي» وحدد العناصر المطلوبة.\n٤. استخرج الروابط وانسخها مع أسمائها بالتسلسل.\nويمكنك أيضاً استخراج ملفات عنصر واحد.",
                14, MUTED, false);
        infoBody.setLineSpacing(dp(5), 1.0f);
        LinearLayout.LayoutParams bodyParams = fullWidth();
        bodyParams.topMargin = dp(9);
        infoCard.addView(infoBody, bodyParams);

        Button loginButton = button("تسجيل الدخول وفتح الموقع", GREEN, Color.rgb(5, 30, 22));
        LinearLayout.LayoutParams loginParams = fullWidth();
        loginParams.topMargin = dp(20);
        page.addView(loginButton, loginParams);
        loginButton.setOnClickListener(v -> showBrowser("https://archive.org/account/login.php"));

        Button itemButton = button("فتح رابط عنصر أو معرّفه", SURFACE_ALT, WHITE);
        LinearLayout.LayoutParams itemParams = fullWidth();
        itemParams.topMargin = dp(10);
        page.addView(itemButton, itemParams);
        itemButton.setOnClickListener(v -> showItemInputDialog());

        Button accountButton = button("فتح صفحة الحساب", SURFACE, WHITE);
        LinearLayout.LayoutParams accountParams = fullWidth();
        accountParams.topMargin = dp(10);
        page.addView(accountButton, accountParams);
        accountButton.setOnClickListener(v -> showBrowser("https://archive.org/account/"));

        TextView privacy = text(
                "الخصوصية: تسجيل الدخول يتم داخل موقع Internet Archive. التطبيق لا يخزّن كلمة المرور. الروابط الخاصة قد تحتاج صلاحية الحساب، وقد لا تعمل في مشغّل خارجي.",
                12, MUTED, false);
        privacy.setLineSpacing(dp(3), 1.0f);
        LinearLayout.LayoutParams privacyParams = fullWidth();
        privacyParams.topMargin = dp(22);
        page.addView(privacy, privacyParams);

        TextView footer = text("نسخة أولية • 1.0.0", 11, MUTED, false);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerParams = fullWidth();
        footerParams.topMargin = dp(18);
        page.addView(footer, footerParams);

        setContentView(scroll);
    }

    private void ensureBrowser() {
        if (browserRoot != null) {
            return;
        }

        browserRoot = new LinearLayout(this);
        browserRoot.setOrientation(LinearLayout.VERTICAL);
        browserRoot.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        browserRoot.setPadding(dp(8), dp(7), dp(8), dp(7));
        browserRoot.setBackgroundColor(BG);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        Button backButton = smallButton("رجوع", SURFACE_ALT, WHITE);
        topRow.addView(backButton, new LinearLayout.LayoutParams(dp(76), dp(42)));
        backButton.setOnClickListener(v -> {
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
            } else {
                showHome();
            }
        });

        Button homeButton = smallButton("الرئيسية", SURFACE, WHITE);
        LinearLayout.LayoutParams homeParams = new LinearLayout.LayoutParams(dp(86), dp(42));
        homeParams.rightMargin = dp(7);
        topRow.addView(homeButton, homeParams);
        homeButton.setOnClickListener(v -> showHome());
        browserRoot.addView(topRow, fullWidth());

        LinearLayout addressRow = new LinearLayout(this);
        addressRow.setOrientation(LinearLayout.HORIZONTAL);
        addressRow.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        LinearLayout.LayoutParams addressRowParams = fullWidth();
        addressRowParams.topMargin = dp(7);
        browserRoot.addView(addressRow, addressRowParams);

        addressInput = new EditText(this);
        addressInput.setSingleLine(true);
        addressInput.setTextSize(13);
        addressInput.setTextColor(WHITE);
        addressInput.setHintTextColor(MUTED);
        addressInput.setHint("رابط العنصر أو المعرّف");
        addressInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        addressInput.setImeOptions(EditorInfo.IME_ACTION_GO);
        addressInput.setTextDirection(View.TEXT_DIRECTION_LTR);
        addressInput.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        addressInput.setPadding(dp(11), 0, dp(11), 0);
        addressInput.setBackground(rounded(SURFACE, 12));
        addressRow.addView(addressInput, new LinearLayout.LayoutParams(0, dp(44), 1f));

        Button goButton = smallButton("فتح", GREEN, Color.rgb(5, 30, 22));
        LinearLayout.LayoutParams goParams = new LinearLayout.LayoutParams(dp(62), dp(44));
        goParams.leftMargin = dp(7);
        addressRow.addView(goButton, goParams);
        goButton.setOnClickListener(v -> openAddressInput());
        addressInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                openAddressInput();
                return true;
            }
            return false;
        });

        browserStatus = text("افتح صفحة عنصر حتى تستخرج روابطه.", 11, MUTED, false);
        browserStatus.setPadding(dp(3), dp(6), dp(3), dp(5));
        browserRoot.addView(browserStatus, fullWidth());

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        if (Build.VERSION.SDK_INT >= 21) {
            progressBar.setProgressTintList(ColorStateList.valueOf(GREEN));
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(GREEN));
        }
        progressBar.setVisibility(View.GONE);
        browserRoot.addView(progressBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(3)));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (progressBar != null) {
                    progressBar.setProgress(newProgress);
                    progressBar.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (!request.isForMainFrame()) {
                    return false;
                }
                return handleMainFrameNavigation(request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleMainFrameNavigation(url);
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                if (browserStatus != null) {
                    browserStatus.setText("جارٍ تحميل الصفحة...");
                }
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (addressInput != null && url != null) {
                    addressInput.setText(url);
                }
                if (browserStatus != null) {
                    if (isProfilePage(url)) {
                        browserStatus.setText("صفحتك جاهزة. اضغط «فيديوهاتي» لعرض المنشورات وتحديدها.");
                    } else if (isItemPage(url)) {
                        browserStatus.setText("صفحة العنصر جاهزة. اضغط استخراج روابط الفيديو.");
                    } else {
                        browserStatus.setText("سجّل الدخول، ثم افتح صفحتك من My Library أو افتح عنصراً.");
                    }
                }
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                CookieManager.getInstance().flush();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler sslHandler, SslError error) {
                sslHandler.cancel();
                if (browserStatus != null) {
                    browserStatus.setText("تم إيقاف اتصال غير موثوق.");
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame() && browserStatus != null) {
                    browserStatus.setText("تعذّر تحميل الصفحة. تحقق من الإنترنت وحاول مرة أخرى.");
                }
            }
        });

        browserRoot.addView(webView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        Button extractButton = button("استخراج روابط العنصر الحالي", GREEN, Color.rgb(5, 30, 22));
        LinearLayout.LayoutParams extractParams = fullWidth();
        extractParams.topMargin = dp(7);
        browserRoot.addView(extractButton, extractParams);
        extractButton.setOnClickListener(v -> extractCurrentItem());

        Button myVideosButton = button("فيديوهاتي: تحديد عدة فيديوهات", SURFACE_ALT, WHITE);
        LinearLayout.LayoutParams myVideosParams = fullWidth();
        myVideosParams.topMargin = dp(7);
        browserRoot.addView(myVideosButton, myVideosParams);
        myVideosButton.setOnClickListener(v -> loadMyVideos());
    }

    private boolean handleMainFrameNavigation(String rawUrl) {
        if (rawUrl == null) {
            return true;
        }
        Uri uri = Uri.parse(rawUrl);
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.US);
        if (("https".equals(scheme) || "http".equals(scheme)) && isArchiveHost(uri.getHost())) {
            return false;
        }
        if ("https".equals(scheme) || "http".equals(scheme)) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                Toast.makeText(this, "تم فتح الرابط الخارجي بالمتصفح.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "ما كدرنا نفتح الرابط الخارجي.", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    private boolean isArchiveHost(String host) {
        return host != null
                && ("archive.org".equalsIgnoreCase(host)
                || "www.archive.org".equalsIgnoreCase(host));
    }

    private boolean isProfilePage(String rawUrl) {
        if (rawUrl == null) {
            return false;
        }
        Uri uri = Uri.parse(rawUrl);
        String path = uri.getPath();
        return isArchiveHost(uri.getHost())
                && path != null
                && path.matches("^/details/@[^/]+/?$");
    }

    private boolean isItemPage(String rawUrl) {
        if (rawUrl == null) {
            return false;
        }
        Uri uri = Uri.parse(rawUrl);
        String path = uri.getPath();
        return isArchiveHost(uri.getHost())
                && path != null
                && (path.startsWith("/download/")
                || (path.startsWith("/details/") && !path.startsWith("/details/@")));
    }

    private void showBrowser(String initialUrl) {
        ensureBrowser();
        browserVisible = true;
        setContentView(browserRoot);
        if (initialUrl != null && !initialUrl.trim().isEmpty()) {
            String current = webView.getUrl();
            if (current == null || !current.equals(initialUrl)) {
                webView.loadUrl(initialUrl);
            }
        }
    }

    private void openAddressInput() {
        if (addressInput == null) {
            return;
        }
        String input = addressInput.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "أدخل رابط العنصر أو معرّفه.", Toast.LENGTH_SHORT).show();
            return;
        }

        String target = input;
        if (!target.contains("://")) {
            if (target.startsWith("www.archive.org/") || target.startsWith("archive.org/")) {
                target = "https://" + target;
            } else if (target.contains("/") || target.contains(".")) {
                target = "https://" + target;
            } else {
                target = "https://archive.org/details/" + Uri.encode(target);
            }
        }

        Uri uri = Uri.parse(target);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || !isArchiveHost(uri.getHost())) {
            Toast.makeText(this, "لأمان الحساب، افتح روابط archive.org فقط.", Toast.LENGTH_LONG).show();
            return;
        }
        webView.loadUrl(uri.toString());
    }

    private void showItemInputDialog() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(15);
        input.setTextColor(Color.BLACK);
        input.setHintTextColor(Color.DKGRAY);
        input.setHint("رابط صفحة العنصر أو المعرّف");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setTextDirection(View.TEXT_DIRECTION_LTR);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));

        LinearLayout wrap = new LinearLayout(this);
        wrap.setPadding(dp(20), dp(8), dp(20), 0);
        wrap.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(50)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("فتح عنصر من Internet Archive")
                .setMessage("تگدر تلصق رابط صفحة العنصر أو تكتب المعرّف فقط.")
                .setView(wrap)
                .setNegativeButton("إلغاء", null)
                .setPositiveButton("فتح", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String value = input.getText().toString().trim();
            if (value.isEmpty()) {
                input.setError("أدخل الرابط أو المعرّف");
                return;
            }
            dialog.dismiss();
            showBrowser(null);
            addressInput.setText(value);
            openAddressInput();
        }));
        dialog.show();
    }

    private void extractCurrentItem() {
        if (webView == null || webView.getUrl() == null) {
            Toast.makeText(this, "افتح صفحة العنصر أولاً.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isArchiveHost(Uri.parse(webView.getUrl()).getHost())) {
            Toast.makeText(this, "افتح صفحة العنصر من archive.org أولاً.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        browserStatus.setText("جارٍ فحص ملفات العنصر...");
        String script =
                "(function(){"
                + "window.__iaResult=null;"
                + "var match=location.pathname.match(/^\\/(?:details|download)\\/([^\\/]+)/);"
                + "if(!match){window.__iaResult={error:'افتح صفحة عنصر تبدأ بـ /details/ أو /download/.'};return;}"
                + "var id;try{id=decodeURIComponent(match[1]);}catch(e){id=match[1];}"
                + "fetch('/metadata/'+encodeURIComponent(id),{credentials:'include'})"
                + ".then(function(r){if(!r.ok)throw new Error('HTTP '+r.status);return r.json();})"
                + ".then(function(data){"
                + "var files=(data.files||[]).filter(function(f){"
                + "var name=(f.name||'').toLowerCase();"
                + "var ext=name.indexOf('.')>=0?name.substring(name.lastIndexOf('.')+1):'';"
                + "return ['mp4','m4v','mov','webm','ogv','ogg','mkv','avi','mpeg','mpg','m3u8','ts'].indexOf(ext)>=0"
                + "|| /mpeg4|h\\.264|webm|ogg video/i.test(f.format||'');"
                + "});"
                + "var list=files.map(function(f){return {name:f.name||'video',format:f.format||'',size:String(f.size||''),"
                + "url:location.origin+'/download/'+encodeURIComponent(id)+'/'+encodeURIComponent(f.name||'')};});"
                + "window.__iaResult={identifier:id,title:(data.metadata&&data.metadata.title)||id,files:list,"
                + "error:list.length?null:'لم نعثر على ملفات فيديو في بيانات هذا العنصر.'};"
                + "}).catch(function(e){window.__iaResult={error:'تعذر قراءة بيانات العنصر: '+String(e.message||e)};});"
                + "})();";

        webView.evaluateJavascript(script, value -> pollExtraction(0));
    }

    private void pollExtraction(int attempt) {
        if (webView == null) {
            progressBar.setVisibility(View.GONE);
            return;
        }
        webView.evaluateJavascript(
                "window.__iaResult ? JSON.stringify(window.__iaResult) : null",
                value -> {
                    if (value == null || "null".equals(value)) {
                        if (attempt < 45) {
                            handler.postDelayed(() -> pollExtraction(attempt + 1), 300);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            browserStatus.setText("انتهت مهلة الفحص. حاول مرة أخرى.");
                            showMessage("انتهت المهلة",
                                    "ما وصلنا رد من بيانات العنصر. تأكد من تسجيل الدخول واتصال الإنترنت، ثم حاول مرة ثانية.");
                        }
                        return;
                    }

                    progressBar.setVisibility(View.GONE);
                    String json = decodeJavascriptString(value);
                    try {
                        JSONObject result = new JSONObject(json);
                        String error = result.optString("error", "");
                        if (!error.isEmpty() && !"null".equals(error)) {
                            browserStatus.setText("تعذّر استخراج الروابط.");
                            showMessage("ما تم استخراج الروابط", error);
                            return;
                        }

                        String title = result.optString("title", result.optString("identifier", "عنصر"));
                        JSONArray filesJson = result.optJSONArray("files");
                        List<MediaFile> files = new ArrayList<>();
                        if (filesJson != null) {
                            for (int i = 0; i < filesJson.length(); i++) {
                                JSONObject item = filesJson.optJSONObject(i);
                                if (item == null) {
                                    continue;
                                }
                                String name = item.optString("name", "video");
                                String url = item.optString("url", "");
                                if (!url.isEmpty()) {
                                    files.add(new MediaFile(
                                            title,
                                            name,
                                            item.optString("format", ""),
                                            item.optString("size", ""),
                                            url));
                                }
                            }
                        }

                        if (files.isEmpty()) {
                            showMessage("ماكو ملفات فيديو", "ما لقينا ملفات فيديو مدرجة ببيانات العنصر الحالي.");
                        } else {
                            browserStatus.setText("تم العثور على " + files.size() + " ملف فيديو.");
                            showFilesDialog(title, result.optString("identifier", ""), files);
                        }
                    } catch (JSONException e) {
                        browserStatus.setText("تعذّر قراءة نتيجة الفحص.");
                        showMessage("خطأ بالنتيجة", "وصلتنا نتيجة غير متوقعة. أعد فتح صفحة العنصر وحاول مرة ثانية.");
                    }
                });
    }


    private void loadMyVideos() {
        if (webView == null || webView.getUrl() == null) {
            showMessage("افتح صفحتك أولاً", "سجّل الدخول إلى Internet Archive وافتح صفحتك الشخصية من My Library، ثم اضغط «فيديوهاتي».");
            return;
        }
        if (!isArchiveHost(Uri.parse(webView.getUrl()).getHost())) {
            showMessage("افتح Internet Archive", "ميزة فيديوهاتي تعمل من صفحات archive.org فقط.");
            return;
        }
        if (!isProfilePage(webView.getUrl())) {
            showMessage("افتح صفحتك الشخصية", "من موقع Internet Archive افتح قائمة حسابك واختر My Library حتى تظهر صفحة منشوراتك، ثم اضغط «فيديوهاتي». لا تحتاج إلى إدخال كلمة المرور داخل التطبيق.");
            return;
        }
        accountItems.clear();
        selectedArchiveIds.clear();
        accountUsername = "";
        accountTotal = 0;
        accountNextStart = 0;
        fetchMyVideoPage(0);
    }

    private void fetchMyVideoPage(int start) {
        if (webView == null) return;
        progressBar.setVisibility(View.VISIBLE);
        browserStatus.setText(start == 0 ? "جارٍ تحميل قائمة فيديوهات صفحتك..." : "جارٍ تحميل المزيد من فيديوهات صفحتك...");
        String script =
                "(function(){"
                + "window.__iaListResult=null;"
                + "var m=location.pathname.match(/^\\/details\\/@([^\\/]+)/);"
                + "if(!m){window.__iaListResult={error:'افتح صفحة ملفك الشخصي من My Library أولاً.'};return;}"
                + "var username;try{username=decodeURIComponent(m[1]);}catch(e){username=m[1];}"
                + "var q='uploader:'+username+' AND mediatype:movies';"
                + "var url='/advancedsearch.php?q='+encodeURIComponent(q)"
                + "+'&fl%5B%5D=identifier&fl%5B%5D=title&fl%5B%5D=mediatype&rows=100&start=" + start + "&output=json&sort%5B%5D=date+desc';"
                + "fetch(url,{credentials:'include'}).then(function(r){if(!r.ok)throw new Error('HTTP '+r.status);return r.json();})"
                + ".then(function(data){var response=data.response||{};window.__iaListResult={username:username,total:Number(response.numFound||0),start:" + start + ",docs:response.docs||[],error:null};})"
                + ".catch(function(e){window.__iaListResult={error:'تعذر تحميل قائمة المنشورات: '+String(e.message||e)};});"
                + "})();";
        webView.evaluateJavascript(script, value -> pollMyVideos(0));
    }

    private void pollMyVideos(int attempt) {
        if (webView == null) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            return;
        }
        webView.evaluateJavascript("window.__iaListResult ? JSON.stringify(window.__iaListResult) : null", value -> {
            if (value == null || "null".equals(value)) {
                if (attempt < 100) {
                    handler.postDelayed(() -> pollMyVideos(attempt + 1), 300);
                } else {
                    progressBar.setVisibility(View.GONE);
                    browserStatus.setText("انتهت مهلة تحميل المنشورات.");
                    showMessage("انتهت المهلة", "لم يصل رد من Internet Archive. تحقق من الاتصال وحاول مرة أخرى.");
                }
                return;
            }
            progressBar.setVisibility(View.GONE);
            try {
                JSONObject result = new JSONObject(decodeJavascriptString(value));
                String error = result.optString("error", "");
                if (!error.isEmpty() && !"null".equals(error)) {
                    browserStatus.setText("تعذر تحميل فيديوهات الصفحة.");
                    showMessage("تعذر عرض فيديوهاتك", error);
                    return;
                }
                accountUsername = result.optString("username", accountUsername);
                accountTotal = result.optInt("total", accountTotal);
                JSONArray docs = result.optJSONArray("docs");
                if (docs != null) {
                    for (int i = 0; i < docs.length(); i++) {
                        JSONObject doc = docs.optJSONObject(i);
                        if (doc == null) continue;
                        String id = doc.optString("identifier", "").trim();
                        if (id.isEmpty()) continue;
                        boolean exists = false;
                        for (ArchiveItem existing : accountItems) {
                            if (existing.identifier.equals(id)) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) accountItems.add(new ArchiveItem(id, doc.optString("title", id)));
                    }
                }
                accountNextStart = result.optInt("start", 0) + (docs == null ? 0 : docs.length());
                if (accountItems.isEmpty()) {
                    browserStatus.setText("ما ظهرت منشورات فيديو في البحث.");
                    showMessage("ما لقينا فيديوهات", "ما ظهرت عناصر فيديو في نتائج صفحتك. قد لا يعرض البحث العناصر الخاصة أو قد تحتاج إلى فتح صفحة My Library الصحيحة.");
                    return;
                }
                browserStatus.setText("تم تحميل " + accountItems.size() + " من " + accountTotal + " عنصر.");
                showMyVideosDialog();
            } catch (Exception e) {
                browserStatus.setText("تعذر قراءة قائمة الفيديوهات.");
                showMessage("خطأ بالقائمة", "وصلتنا نتيجة غير متوقعة من البحث. حاول مرة ثانية.");
            }
        });
    }

    private void showMyVideosDialog() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(7), dp(12), dp(5));

        TextView summary = text("الصفحة: @" + accountUsername + "\nالمعروض: " + accountItems.size() + " من " + accountTotal, 13, SURFACE_ALT, true);
        content.addView(summary, fullWidth());

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        LinearLayout.LayoutParams controlsParams = fullWidth();
        controlsParams.topMargin = dp(7);
        content.addView(controls, controlsParams);

        Button selectVisible = smallButton("تحديد/إلغاء الكل", SURFACE_ALT, WHITE);
        controls.addView(selectVisible, new LinearLayout.LayoutParams(0, dp(40), 1f));

        Button loadMore = smallButton("تحميل المزيد", SURFACE, WHITE);
        LinearLayout.LayoutParams moreParams = new LinearLayout.LayoutParams(0, dp(40), 1f);
        moreParams.rightMargin = dp(7);
        controls.addView(loadMore, moreParams);
        loadMore.setEnabled(accountNextStart < accountTotal);
        loadMore.setAlpha(loadMore.isEnabled() ? 1f : 0.45f);

        ScrollView scroll = new ScrollView(this);
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(0, dp(8), 0, dp(5));
        for (ArchiveItem item : accountItems) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(9), dp(7), dp(9), dp(7));
            card.setBackground(rounded(Color.rgb(241, 245, 249), 10));
            LinearLayout.LayoutParams cardParams = fullWidth();
            cardParams.bottomMargin = dp(6);
            rows.addView(card, cardParams);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(item.title);
            checkBox.setTextColor(Color.rgb(20, 28, 47));
            checkBox.setTextSize(14);
            checkBox.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            checkBox.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            checkBox.setChecked(selectedArchiveIds.contains(item.identifier));
            card.addView(checkBox, fullWidth());
            checkBox.setOnCheckedChangeListener((buttonView, checked) -> {
                if (checked) selectedArchiveIds.add(item.identifier);
                else selectedArchiveIds.remove(item.identifier);
            });

            TextView id = text(item.identifier, 11, Color.rgb(92, 106, 126), false);
            id.setTextDirection(View.TEXT_DIRECTION_LTR);
            id.setGravity(Gravity.LEFT);
            card.addView(id, fullWidth());
        }
        scroll.addView(rows, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(330));
        scrollParams.topMargin = dp(4);
        content.addView(scroll, scrollParams);

        TextView selectedSummary = text("المحدد حالياً: " + selectedArchiveIds.size(), 12, MUTED, false);
        LinearLayout.LayoutParams selectedParams = fullWidth();
        selectedParams.topMargin = dp(5);
        content.addView(selectedSummary, selectedParams);

        Button extract = button("استخراج روابط المحدد", GREEN, Color.rgb(5, 30, 22));
        LinearLayout.LayoutParams extractParams = fullWidth();
        extractParams.topMargin = dp(8);
        content.addView(extract, extractParams);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("فيديوهاتي المنشورة")
                .setView(content)
                .setNegativeButton("إغلاق", null)
                .create();

        selectVisible.setOnClickListener(v -> {
            boolean allSelected = !accountItems.isEmpty();
            for (ArchiveItem item : accountItems) {
                if (!selectedArchiveIds.contains(item.identifier)) {
                    allSelected = false;
                    break;
                }
            }
            for (ArchiveItem item : accountItems) {
                if (allSelected) selectedArchiveIds.remove(item.identifier);
                else selectedArchiveIds.add(item.identifier);
            }
            dialog.dismiss();
            showMyVideosDialog();
        });
        loadMore.setOnClickListener(v -> {
            if (accountNextStart < accountTotal) {
                int next = accountNextStart;
                dialog.dismiss();
                fetchMyVideoPage(next);
            }
        });
        extract.setOnClickListener(v -> {
            if (selectedArchiveIds.isEmpty()) {
                Toast.makeText(this, "حدد فيديو واحداً على الأقل.", Toast.LENGTH_SHORT).show();
                return;
            }
            List<ArchiveItem> selected = new ArrayList<>();
            for (ArchiveItem item : accountItems) {
                if (selectedArchiveIds.contains(item.identifier)) selected.add(item);
            }
            dialog.dismiss();
            extractSelectedItems(selected);
        });
        dialog.show();
    }

    private void extractSelectedItems(List<ArchiveItem> selected) {
        if (webView == null || selected.isEmpty()) return;
        JSONArray ids = new JSONArray();
        for (ArchiveItem item : selected) ids.put(item.identifier);
        String script =
                "(function(){"
                + "window.__iaBatchResult=null;"
                + "var ids=" + ids.toString() + ";"
                + "function isVideo(f){var n=(f.name||'').toLowerCase();var ext=n.indexOf('.')>=0?n.substring(n.lastIndexOf('.')+1):'';"
                + "return ['mp4','m4v','mov','webm','ogv','ogg','mkv','avi','mpeg','mpg','m3u8','ts'].indexOf(ext)>=0||/mpeg4|h\\.264|webm|ogg video/i.test(f.format||'');}"
                + "Promise.all(ids.map(function(id){return fetch('/metadata/'+encodeURIComponent(id),{credentials:'include'})"
                + ".then(function(r){if(!r.ok)throw new Error('HTTP '+r.status);return r.json();})"
                + ".then(function(data){var title=(data.metadata&&data.metadata.title)||id;var files=(data.files||[]).filter(isVideo).map(function(f){return {title:title,name:f.name||'video',format:f.format||'',size:String(f.size||''),url:location.origin+'/download/'+encodeURIComponent(id)+'/'+encodeURIComponent(f.name||'')};});return {identifier:id,files:files,error:null};})"
                + ".catch(function(e){return {identifier:id,files:[],error:String(e.message||e)};});}))"
                + ".then(function(items){var files=[];var failed=0;items.forEach(function(item){if(!item.files.length)failed++;item.files.forEach(function(f){files.push(f);});});window.__iaBatchResult={files:files,requested:ids.length,failed:failed};})"
                + ".catch(function(e){window.__iaBatchResult={error:String(e.message||e)};});"
                + "})();";
        progressBar.setVisibility(View.VISIBLE);
        browserStatus.setText("جارٍ استخراج روابط " + selected.size() + " عنصر...");
        webView.evaluateJavascript(script, value -> pollBatchExtraction(0, selected.size()));
    }

    private void pollBatchExtraction(int attempt, int requested) {
        if (webView == null) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            return;
        }
        webView.evaluateJavascript("window.__iaBatchResult ? JSON.stringify(window.__iaBatchResult) : null", value -> {
            if (value == null || "null".equals(value)) {
                if (attempt < 100) {
                    handler.postDelayed(() -> pollBatchExtraction(attempt + 1, requested), 300);
                } else {
                    progressBar.setVisibility(View.GONE);
                    browserStatus.setText("انتهت مهلة استخراج الروابط.");
                    showMessage("انتهت المهلة", "لم تكتمل قراءة بيانات العناصر المحددة. حاول تحديد عدد أقل.");
                }
                return;
            }
            progressBar.setVisibility(View.GONE);
            try {
                JSONObject result = new JSONObject(decodeJavascriptString(value));
                String error = result.optString("error", "");
                if (!error.isEmpty() && !"null".equals(error)) {
                    showMessage("تعذر استخراج الروابط", error);
                    return;
                }
                JSONArray filesJson = result.optJSONArray("files");
                List<MediaFile> files = new ArrayList<>();
                if (filesJson != null) {
                    for (int i = 0; i < filesJson.length(); i++) {
                        JSONObject file = filesJson.optJSONObject(i);
                        if (file == null) continue;
                        String url = file.optString("url", "");
                        if (!url.isEmpty()) {
                            files.add(new MediaFile(
                                    file.optString("title", ""),
                                    file.optString("name", "video"),
                                    file.optString("format", ""),
                                    file.optString("size", ""),
                                    url));
                        }
                    }
                }
                int failed = result.optInt("failed", 0);
                if (files.isEmpty()) {
                    browserStatus.setText("لم يتم العثور على ملفات فيديو في العناصر المحددة.");
                    showMessage("ماكو روابط فيديو", "ما لقينا ملفات فيديو قابلة للاستخراج في العناصر المحددة. بعض العناصر قد لا تكون فيديو أو قد لا تكون متاحة للحساب.");
                    return;
                }
                browserStatus.setText("تم استخراج " + files.size() + " رابط من " + requested + " عنصر.");
                showFilesDialog("روابط الفيديوهات المحددة", accountUsername, files);
                if (failed > 0) {
                    Toast.makeText(this, "تعذر استخراج ملفات من " + failed + " عنصر.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                browserStatus.setText("تعذر قراءة نتائج الاستخراج.");
                showMessage("خطأ بالنتيجة", "وصلتنا نتيجة غير متوقعة. حاول مرة أخرى.");
            }
        });
    }

    private String decodeJavascriptString(String value) {
        try {
            Object decoded = new JSONTokener(value).nextValue();
            if (decoded instanceof String) {
                return (String) decoded;
            }
        } catch (Exception ignored) {
        }
        return value;
    }

    private void showFilesDialog(String title, String identifier, List<MediaFile> files) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14), dp(8), dp(14), dp(6));
        content.setBackgroundColor(Color.WHITE);

        TextView heading = new TextView(this);
        heading.setText(title + (identifier.isEmpty() ? "" : "\n" + identifier));
        heading.setTextColor(Color.rgb(27, 39, 64));
        heading.setTextSize(13);
        heading.setGravity(Gravity.RIGHT);
        heading.setPadding(0, 0, 0, dp(8));
        content.addView(heading, fullWidth());

        Button copyAll = button("نسخ كل الروابط", GREEN, Color.rgb(5, 30, 22));
        content.addView(copyAll, fullWidth());
        copyAll.setOnClickListener(v -> {
            StringBuilder all = new StringBuilder();
            for (MediaFile file : files) {
                if (!file.title.isEmpty()) all.append(file.title).append('\n');
                all.append(file.name).append('\n');
                all.append(file.url).append("\n\n");
            }
            copyText("روابط الفيديو", all.toString().trim());
        });

        ScrollView scroll = new ScrollView(this);
        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(0, dp(9), 0, dp(3));

        for (MediaFile file : files) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(11), dp(10), dp(11), dp(10));
            card.setBackground(rounded(Color.rgb(241, 245, 249), 12));
            LinearLayout.LayoutParams cardParams = fullWidth();
            cardParams.bottomMargin = dp(8);
            rows.addView(card, cardParams);

            if (!file.title.isEmpty()) {
                TextView itemTitle = new TextView(this);
                itemTitle.setText(file.title);
                itemTitle.setTextColor(Color.rgb(20, 28, 47));
                itemTitle.setTextSize(14);
                itemTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                itemTitle.setGravity(Gravity.RIGHT);
                itemTitle.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_RTL);
                card.addView(itemTitle, fullWidth());
            }

            TextView name = new TextView(this);
            name.setText(file.name);
            name.setTextColor(Color.rgb(68, 79, 96));
            name.setTextSize(12);
            name.setGravity(Gravity.RIGHT);
            name.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_LTR);
            LinearLayout.LayoutParams nameParams = fullWidth();
            nameParams.topMargin = dp(3);
            card.addView(name, nameParams);

            String details = file.format;
            String size = humanSize(file.size);
            if (!size.isEmpty()) {
                details = details.isEmpty() ? size : details + " • " + size;
            }
            TextView meta = new TextView(this);
            meta.setText(details.isEmpty() ? "ملف فيديو" : details);
            meta.setTextColor(Color.rgb(92, 106, 126));
            meta.setTextSize(11);
            meta.setGravity(Gravity.RIGHT);
            LinearLayout.LayoutParams metaParams = fullWidth();
            metaParams.topMargin = dp(3);
            card.addView(meta, metaParams);

            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            LinearLayout.LayoutParams actionParams = fullWidth();
            actionParams.topMargin = dp(9);
            card.addView(actions, actionParams);

            Button copy = smallButton("نسخ الرابط", GREEN, Color.rgb(5, 30, 22));
            actions.addView(copy, new LinearLayout.LayoutParams(0, dp(40), 1f));
            copy.setOnClickListener(v -> copyText("رابط الفيديو", file.url));

            Button open = smallButton("فتح خارجي", SURFACE_ALT, WHITE);
            LinearLayout.LayoutParams openParams = new LinearLayout.LayoutParams(0, dp(40), 1f);
            openParams.rightMargin = dp(7);
            actions.addView(open, openParams);
            open.setOnClickListener(v -> openInExternalPlayer(file.url));
        }

        scroll.addView(rows, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(330));
        scrollParams.topMargin = dp(4);
        content.addView(scroll, scrollParams);

        TextView note = new TextView(this);
        note.setText("ملاحظة: الروابط الخاصة قد تحتاج جلسة الحساب، وقد لا تعمل عند فتحها في مشغّل خارجي.");
        note.setTextColor(Color.rgb(92, 106, 126));
        note.setTextSize(11);
        note.setGravity(Gravity.RIGHT);
        note.setPadding(0, dp(8), 0, 0);
        content.addView(note, fullWidth());

        new AlertDialog.Builder(this)
                .setTitle("روابط الفيديو (" + files.size() + ")")
                .setView(content)
                .setPositiveButton("تم", null)
                .show();
    }

    private void openInExternalPlayer(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "video/*");
            startActivity(intent);
        } catch (Exception e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception ignored) {
                Toast.makeText(this, "ماكو تطبيق مناسب لفتح الرابط.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String humanSize(String rawSize) {
        try {
            long bytes = Long.parseLong(rawSize);
            if (bytes < 1) {
                return "";
            }
            if (bytes < 1024) {
                return bytes + " B";
            }
            if (bytes < 1024L * 1024L) {
                return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
            }
            if (bytes < 1024L * 1024L * 1024L) {
                return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
            }
            return String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        } catch (Exception e) {
            return "";
        }
    }

    private void copyText(String label, String value) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(label, value));
            Toast.makeText(this, "تم نسخ الرابط.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "تعذّر النسخ.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("حسناً", null)
                .show();
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.RIGHT);
        view.setTextDirection(View.TEXT_DIRECTION_RTL);
        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    private Button button(String value, int background, int textColor) {
        Button view = new Button(this);
        view.setText(value);
        view.setTextSize(14);
        view.setTextColor(textColor);
        view.setAllCaps(false);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(12), dp(8), dp(12), dp(8));
        view.setBackground(rounded(background, 14));
        view.setMinHeight(dp(50));
        return view;
    }

    private Button smallButton(String value, int background, int textColor) {
        Button view = button(value, background, textColor);
        view.setTextSize(12);
        view.setMinHeight(dp(40));
        view.setPadding(dp(7), dp(4), dp(7), dp(4));
        return view;
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onBackPressed() {
        if (browserVisible) {
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
            } else {
                showHome();
            }
            return;
        }
        super.onBackPressed();
    }

    private static final class ArchiveItem {
        final String identifier;
        final String title;

        ArchiveItem(String identifier, String title) {
            this.identifier = identifier;
            this.title = title;
        }
    }

    private static final class MediaFile {
        final String title;
        final String name;
        final String format;
        final String size;
        final String url;

        MediaFile(String title, String name, String format, String size, String url) {
            this.title = title == null ? "" : title;
            this.name = name;
            this.format = format;
            this.size = size;
            this.url = url;
        }
    }
}
