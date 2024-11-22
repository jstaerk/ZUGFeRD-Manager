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
import io.github.vinceglb.filekit.core.PlatformFile
import org.cef.browser.CefBrowser
import java.awt.BorderLayout
import javax.swing.JPanel


//private const val OSR = false
private const val TRANSPARENT = false
private const val BLANK = "about:blank"

@Composable
actual fun PdfViewer(pdf: PlatformFile, modifier: Modifier) {
    //val scope = rememberCoroutineScope()
    var browserState by remember { mutableStateOf<CefBrowser?>(null) }

    @Suppress("SpellCheckingInspection")
    val pdfUrl = remember(pdf) { "${pdf.file.toURI()}#toolbar=1&navpanes=0" }

    DisposableEffect(Unit) {
        onDispose {
            //APP_LOGGER.debug("CLOSE PDF-BROWSER")
            browserState?.close(true)
        }
    }

    SwingPanel(
        background = MaterialTheme.colorScheme.surface,
        modifier = modifier,
        factory = {
            //val app = CEF_APP
            //val client = app.createClient()
            //val browser = client.createBrowser("https://google.de", OSR, TRANSPARENT)
            val browser = CEF_CLIENT.createBrowser(pdfUrl, false, TRANSPARENT)
            //browser.createImmediately()

            browserState = browser

            PdfPanel(browser)
        },
        update = { panel: PdfPanel ->
            //val javaBg = java.awt.Color(surfaceColor.red, surfaceColor.green, surfaceColor.blue)
            panel.browser.stopLoad()
            panel.browser.loadURL(pdfUrl)

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

private class PdfPanel(
    val browser: CefBrowser
) : JPanel(BorderLayout()) {
    init {
        add(browser.uiComponent, BorderLayout.CENTER)
    }
}

/*
@Composable
actual fun PdfViewer(pdf: PlatformFile, modifier: Modifier) {
    //val appState = LocalAppState.current
    val surfaceColor = MaterialTheme.colorScheme.surface

    SwingPanel(
        background = surfaceColor,
        modifier = modifier,
        factory = {
            //println("create pdf viewer: ${pdf.name}")

            // initiate font caching for faster startups
            FontPropertiesManager.getInstance().loadOrReadSystemFonts()

            // build a controller
            //val controller = SwingController()
            val controller = object : SwingController() {
                // Make Toolbar background transparent.
                override fun setCompleteToolBar(toolbar: JToolBar?) {
                    if (toolbar != null) {
                        toolbar.isOpaque = false
                        toolbar.components
                            .filterIsInstance<JToolBar>()
                            .forEach {
                                it.isOpaque = false
                            }
                    }
                    super.setCompleteToolBar(toolbar)
                }
            }
            controller.setIsEmbeddedComponent(true)

            // Build a SwingViewFactory configured with the controller
            val factory = SwingViewBuilder(
                controller,
                ViewerPropertiesManager.getInstance().apply {
                    set(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_FULL_SCREEN, "false")
                    set(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION, "false")
                    set(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_FORMS, "false")
                    set(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_SEARCH, "false")
                    set(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_UTILITY, "false")
                    set(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ROTATE, "false")
                    set(ViewerPropertiesManager.PROPERTY_SHOW_STATUSBAR, "false")
                },
            )

            // Use the factory to build a JPanel that is pre-configured with a complete, active Viewer UI.
            val viewerComponentPanel = factory.buildViewerPanel()

            // add copy keyboard command
            ComponentKeyBinding.install(controller, viewerComponentPanel)

            // add interactive mouse link annotation support via callback
            controller.documentViewController.annotationCallback = MyAnnotationCallback(
                controller.documentViewController
            )

            viewerComponentPanel.isOpaque = false

            try {
                val splitPane = viewerComponentPanel.getComponent(1) as JSplitPane
                splitPane.border = createEmptyBorder()

                val scrollPane = splitPane.rightComponent as JScrollPane
                scrollPane.border = createEmptyBorder()
                scrollPane.viewportBorder = createEmptyBorder()
            } catch (e: Exception) {
                APP_LOGGER.warn("Can't remove borders from PDF viewer.", e)
            }

            PdfPanel(viewerComponentPanel, controller)
        },
        update = { panel: PdfPanel ->
            //val javaBg = java.awt.Color(surfaceColor.red, surfaceColor.green, surfaceColor.blue)

            //println("update pdf viewer: ${pdf.name}")

            val size = pdf.getSize() ?: 0L
            if (size > 0L) {
                panel.controller.openDocument(pdf.path!!)
            } else {
                panel.controller.closeDocument()
            }

            //panel.blur(appState.locked, javaBg)
        }
    )
}

private class PdfPanel(
    val view: JPanel,
    val controller: SwingController,
) : JPanel(BorderLayout()) {
    //private var blurred: Boolean = false

    init {
        add(view, BorderLayout.CENTER)
    }

    //fun blur(blurred: Boolean, bg: java.awt.Color) {
    //    if (this.blurred == blurred) {
    //        return
    //    }
    //
    //    remove(0)
    //
    //    if (blurred) {
    //        add(JLayer(view, BlurLayerUI(bg)), BorderLayout.CENTER)
    //    } else {
    //        add(view, BorderLayout.CENTER)
    //    }
    //
    //    this.blurred = blurred
    //
    //    revalidate()
    //    repaint()
    //}
}
*/

/*
private class BlurLayerUI(val bg: java.awt.Color) : LayerUI<Component>() {
    private var mOffscreenImage: BufferedImage? = null
    private val mOperation: BufferedImageOp

    init {
        val blurValue = 15
        val blurCount = blurValue * blurValue
        val ninth = 1.0f / blurCount
        val blurKernel = FloatArray(blurCount)
        for (i in 0 until blurCount) {
            blurKernel[i] = ninth
        }

        mOperation = ConvolveOp(Kernel(blurValue, blurValue, blurKernel), ConvolveOp.EDGE_NO_OP, null)
    }

    override fun paint(g: Graphics, c: JComponent) {
        val w = c.width
        val h = c.height
        if (w == 0 || h == 0) {
            return
        }
        // only create the offscreen image if the one we have is the wrong size.
        if (mOffscreenImage == null || mOffscreenImage!!.width != w || mOffscreenImage!!.height != h) {
            mOffscreenImage = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        }
        val ig2 = mOffscreenImage!!.createGraphics()
        ig2.clip = g.clip
        ig2.background = bg
        ig2.fillRect(0, 0, w, h)
        super.paint(ig2, c)
        ig2.dispose()

        val g2 = g as Graphics2D
        g2.drawImage(mOffscreenImage, mOperation, 0, 0)

    }
}
*/
