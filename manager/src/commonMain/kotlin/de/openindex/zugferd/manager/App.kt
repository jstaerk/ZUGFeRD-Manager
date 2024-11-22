package de.openindex.zugferd.manager

import androidx.compose.runtime.Composable
import de.openindex.zugferd.manager.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

const val APP_NAME: String = AppInfo.Project.NAME
const val APP_VERSION: String = AppInfo.Project.VERSION
const val APP_VENDOR: String = AppInfo.Custom.VENDOR

val APP_TITLE: String = APP_NAME.replace('-', ' ').trim()
val APP_TITLE_FULL: String = "$APP_VENDOR $APP_TITLE"
val APP_VERSION_SHORT: String = APP_VERSION.substringBefore('-').trim()

@Composable
@Preview
fun App() {
    //println(AppInfo.Project.NAME)

    AppTheme {
        AppLayout()
        //DummyContent()
    }
}
