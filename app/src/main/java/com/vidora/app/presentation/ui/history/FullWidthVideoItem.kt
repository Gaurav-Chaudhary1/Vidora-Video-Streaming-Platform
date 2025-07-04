package com.vidora.app.presentation.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vidora.app.data.remote.models.video.history.VideoDetails
import com.vidora.app.presentation.history.HistoryViewModel
import com.vidora.app.utils.Constants
import com.vidora.app.utils.toRelativeTime

@Composable
fun FullWidthVideoItem(
    viewModel: HistoryViewModel,
    video: VideoDetails,
    onClick: (String) -> Unit
) {
    val videoThumb = video.thumbnailUrl!!
    val signedThumb = produceState<String?>(initialValue = null, key1 = videoThumb) {
        if (videoThumb.isNotBlank()){
            value = viewModel.signedUrlForHistory(videoThumb)
        }
    }.value

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(video.id) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = signedThumb,
                contentDescription = video.title,
                modifier = Modifier
                    .size(width = 120.dp, height = 68.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = video.channelId.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${video.views} views • ${video.createdAt?.toRelativeTime()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
