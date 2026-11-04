package com.pokke.music.ui.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pokke.music.R

/**
 * Built-in avatar frames (transparent PNGs from gallery).
 * Values stored in profiles.frame column as "none" | "frame_01" … "frame_20".
 */
object AvatarFrames {
    data class Frame(val id: String, val label: String, @DrawableRes val resId: Int?)

    val ALL: List<Frame> = listOf(
        Frame("none", "Ninguno", null),
        Frame("frame_01", "Marco 1", R.drawable.frame_01),
        Frame("frame_02", "Marco 2", R.drawable.frame_02),
        Frame("frame_03", "Marco 3", R.drawable.frame_03),
        Frame("frame_04", "Marco 4", R.drawable.frame_04),
        Frame("frame_05", "Marco 5", R.drawable.frame_05),
        Frame("frame_06", "Marco 6", R.drawable.frame_06),
        Frame("frame_07", "Marco 7", R.drawable.frame_07),
        Frame("frame_08", "Marco 8", R.drawable.frame_08),
        Frame("frame_09", "Marco 9", R.drawable.frame_09),
        Frame("frame_10", "Marco 10", R.drawable.frame_10),
        Frame("frame_11", "Marco 11", R.drawable.frame_11),
        Frame("frame_12", "Marco 12", R.drawable.frame_12),
        Frame("frame_13", "Marco 13", R.drawable.frame_13),
        Frame("frame_14", "Marco 14", R.drawable.frame_14),
        Frame("frame_15", "Marco 15", R.drawable.frame_15),
        Frame("frame_16", "Marco 16", R.drawable.frame_16),
        Frame("frame_17", "Marco 17", R.drawable.frame_17),
        Frame("frame_18", "Marco 18", R.drawable.frame_18),
        Frame("frame_19", "Marco 19", R.drawable.frame_19),
        Frame("frame_20", "Marco 20", R.drawable.frame_20),
    )

    fun byId(id: String?): Frame = ALL.firstOrNull { it.id == id } ?: ALL.first()
}

/**
 * Avatar circle with optional decorative frame overlay (Discord-style).
 * Frame sits on top of the circular avatar; avatar is slightly inset so the frame border shows.
 */
@Composable
fun FramedAvatar(
    avatarUrl: String?,
    displayName: String,
    frameId: String?,
    size: Dp = 96.dp,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val frame = AvatarFrames.byId(frameId)
    val hasFrame = frame.resId != null
    // When a PNG frame is present, shrink the photo a bit so the frame edge remains visible.
    val photoScale = if (hasFrame) 0.78f else 1f
    val photoSize = size * photoScale

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(photoSize)
                .clip(CircleShape)
                .background(Color(0xFF2A2F42))
                .then(
                    if (!hasFrame && borderWidth > 0.dp)
                        Modifier.border(borderWidth, borderColor, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = (displayName.firstOrNull() ?: 'P').uppercaseChar().toString(),
                    color = Color.White,
                    fontSize = (size.value * 0.32f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (hasFrame && frame.resId != null) {
            Image(
                painter = painterResource(frame.resId),
                contentDescription = frame.label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}
