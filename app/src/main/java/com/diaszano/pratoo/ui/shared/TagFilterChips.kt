package com.diaszano.pratoo.ui.shared

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diaszano.pratoo.R
import com.diaszano.pratoo.recipe.domain.model.Tag

@Composable
fun TagFilterChips(
    tags: List<Tag>,
    selectedTagId: Long?,
    onTagSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    showAllChip: Boolean = false
) {
    if (tags.isEmpty() && !showAllChip) return

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showAllChip) {
            FilterChip(
                selected = selectedTagId == null,
                onClick = { onTagSelected(null) },
                label = { Text(stringResource(R.string.all)) }
            )
        }
        tags.forEach { tag ->
            FilterChip(
                selected = selectedTagId == tag.id,
                onClick = { onTagSelected(tag.id) },
                label = { Text(tag.name) }
            )
        }
    }
}
