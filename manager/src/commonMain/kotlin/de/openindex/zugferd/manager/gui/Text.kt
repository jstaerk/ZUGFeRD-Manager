package de.openindex.zugferd.manager.gui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

/*
@Composable
fun SingleLineText(
    text: String,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    modifier: Modifier = Modifier,
) {
    /*
    val localContentColor = LocalContentColor.current
    val localContentAlpha = LocalContentAlpha.current
    val overrideColorOrUnspecified: Color = if (color.isSpecified) {
        color
    } else if (style.color.isSpecified) {
        style.color
    } else {
        localContentColor.copy(localContentAlpha)
    }
    */

    Text(
        text = text,
        color = color,
        //color = overrideColorOrUnspecified,
        style = style,
        overflow = overflow,
        softWrap = false,
        modifier = modifier,
    )
}
*/

@Composable
fun SectionTitle(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) = Text(
    text = text,
    color = color,
    modifier = modifier,
    style = MaterialTheme.typography.titleLarge,
    softWrap = true,
)

@Composable
fun SectionInfo(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) = Text(
    text = text,
    color = color,
    modifier = modifier,
    style = MaterialTheme.typography.bodyMedium,
    softWrap = true,
)

@Composable
fun SectionSubTitle(
    text: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            lineHeight = 1.em,
            softWrap = false,
            modifier = Modifier,
        )

        HorizontalDivider(
            //color = MaterialTheme.colorScheme.onSurface
            //    .copy(alpha = 0.2f),
            modifier = Modifier
                .weight(1f, fill = true),
        )

        actions()
    }
}
