import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayerExo(
    videoUrl: String,
    modifier: Modifier
) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build().apply {
        setMediaItem(MediaItem.fromUri(videoUrl))
    }}

    val playerView = PlayerView(context)
    val playWhenReady by rememberSaveable { mutableStateOf<Boolean>(true) }

    playerView.player = player

    LaunchedEffect(player) {
        player.prepare()
        player.playWhenReady = playWhenReady
    }

    AndroidView(
        modifier = modifier,
        factory = {
            playerView
        },
        update = { view ->
            view.player = player
        }
    )

    // Ensure the player is released when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }
}
