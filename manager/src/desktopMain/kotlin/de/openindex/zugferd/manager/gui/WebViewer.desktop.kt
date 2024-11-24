package de.openindex.zugferd.manager.gui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import de.openindex.zugferd.manager.utils.CEF_CLIENT
import org.cef.browser.CefBrowser
import java.awt.BorderLayout
import javax.swing.JPanel
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

//private const val OSR = false
private const val TRANSPARENT = false
private const val BLANK = "about:blank"

@Composable
@OptIn(ExperimentalEncodingApi::class)
actual fun WebViewer(html: String, modifier: Modifier) {
    var browserState by remember { mutableStateOf<CefBrowser?>(null) }
    val dataUrl = remember(html) {
        "data:text/html;charset=utf-8;base64,".plus(
            Base64.Default.encode(html.encodeToByteArray())
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            //APP_LOGGER.debug("CLOSE WEB-BROWSER")
            browserState?.close(true)
        }
    }

    SwingPanel(
        background = MaterialTheme.colorScheme.surface,
        modifier = modifier,
        factory = {
            val browser = CEF_CLIENT.createBrowser(dataUrl, false, TRANSPARENT)
            //browser.createImmediately()
            browserState = browser
            WebPanel(browser)
        },
        update = { panel: WebPanel ->
            //val javaBg = java.awt.Color(surfaceColor.red, surfaceColor.green, surfaceColor.blue)
            panel.browser.stopLoad()
            panel.browser.loadURL(dataUrl)

            /*
            val size = html.trim().length
            if (size < 1L) {
                panel.browser.loadURL(BLANK)
            } else {
                scope.launch(Dispatchers.IO) {
                    //delay(2500)
                    tempFile.deleteIfExists()
                    tempFile
                        .writer(charset = Charsets.UTF_8)
                        .use { writer ->
                            writer.write(html)
                        }

                    val url = tempFile.toUri().toString()
                    APP_LOGGER.debug("OPEN HTML: $url")
                    panel.browser.loadURL(url)
                }
                //.invokeOnCompletion {
                //    val url = tempFile.toUri().toString()
                //    APP_LOGGER.debug("OPEN HTML: $url")
                //    panel.browser.loadURL(url)
                //    panel.browser.reload()
                //    panel.revalidate()
                //    panel.repaint()
                //}
            }*/
        }
    )
}

private class WebPanel(
    val browser: CefBrowser
) : JPanel(BorderLayout()) {
    init {
        add(browser.uiComponent, BorderLayout.CENTER)
    }
}
