package ru.noties.markwon;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.text.style.StrikethroughSpan;
import android.widget.TextView;

import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import ru.noties.debug.AndroidLogDebugOutput;
import ru.noties.debug.Debug;
import ru.noties.markwon.spans.DrawableSpanUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    static {
        Debug.init(new AndroidLogDebugOutput(true));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textView = (TextView) findViewById(R.id.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream stream = null;
                Scanner scanner = null;
                String md = null;
                try {
                    stream = getAssets().open("test.md");
                    scanner = new Scanner(stream).useDelimiter("\\A");
                    if (scanner.hasNext()) {
                        md = scanner.next();
                    }
                } catch (Throwable t) {
                    Debug.e(t);
                } finally {
                    if (stream != null) {
                        try { stream.close(); } catch (IOException e) {}
                    }
                    if (scanner != null) {
                        scanner.close();
                    }
                }

                if (md != null) {
                    final long start = SystemClock.uptimeMillis();
                    final Parser parser = new Parser.Builder()
                            .extensions(Arrays.asList(StrikethroughExtension.create()))
                            .build();
                    final Node node = parser.parse(md);
                    final CharSequence text = new SpannableRenderer()._render(node/*, new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(textView.getText());
                            final Drawable drawable = null;
                            drawable.setCallback(textView);
                        }
                    }*/);
                    final long end = SystemClock.uptimeMillis();
                    Debug.i("Rendered: %d ms, length: %d", end - start, text.length());
                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            // NB! LinkMovementMethod forces frequent updates...
//                            textView.setMovementMethod(LinkMovementMethod.getInstance());
                            textView.setText(text);
                            DrawableSpanUtils.scheduleDrawables(textView);
                        }
                    });
                }
            }
        }).start();
    }
}