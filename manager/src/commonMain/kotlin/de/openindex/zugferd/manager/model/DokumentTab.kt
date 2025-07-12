package de.openindex.zugferd.manager.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.vinceglb.filekit.core.PlatformFile


/*
class DocumentTab(
    name: String = "",
    pdf: PlatformFile? = null,
    tags: List<String> = emptyList()
) {
    var name by mutableStateOf(name)
    var pdf by mutableStateOf(pdf)
    var tags by mutableStateOf(tags)
}

 */


class DocumentTab(
    name: String = "",
    pdf: PlatformFile? = null,
    tags: List<String> = emptyList()
) {
    var name by mutableStateOf(name)
    var pdf by mutableStateOf(pdf)
    var tags by mutableStateOf(tags)
    var html by mutableStateOf<String?>(null)
    var xml by mutableStateOf<String?>(null)
}