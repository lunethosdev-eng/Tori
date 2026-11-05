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
 * Built-in avatar frames.
 * Stored in profiles.frame as "none" | "frame_01"… | named id like "sakura".
 */
object AvatarFrames {
    data class Frame(val id: String, val label: String, @DrawableRes val resId: Int?)

    val ALL: List<Frame> = buildList {
        add(Frame("none", "Ninguno", null))
        val numeric = listOf(
            R.drawable.frame_01, R.drawable.frame_02, R.drawable.frame_03, R.drawable.frame_04,
            R.drawable.frame_05, R.drawable.frame_06, R.drawable.frame_07, R.drawable.frame_08,
            R.drawable.frame_09, R.drawable.frame_10, R.drawable.frame_11, R.drawable.frame_12,
            R.drawable.frame_13, R.drawable.frame_14, R.drawable.frame_15, R.drawable.frame_16,
            R.drawable.frame_17, R.drawable.frame_18, R.drawable.frame_19, R.drawable.frame_20,
            R.drawable.frame_21, R.drawable.frame_22, R.drawable.frame_23, R.drawable.frame_24,
            R.drawable.frame_25, R.drawable.frame_26
        )
        numeric.forEachIndexed { i, res ->
            val id = "frame_%02d".format(i + 1)
            add(Frame(id, "Marco ${i + 1}", res))
        }
        val named = listOf(
            "afternoon_breeze" to R.drawable.afternoon_breeze,
            "airona_fireworks" to R.drawable.airona_fireworks,
            "blanket_green" to R.drawable.blanket_green,
            "blanket_orange" to R.drawable.blanket_orange,
            "blanket_pink" to R.drawable.blanket_pink,
            "blanket_purple" to R.drawable.blanket_purple,
            "box_green_red" to R.drawable.box_green_red,
            "box_red_white" to R.drawable.box_red_white,
            "box_red_white_blue" to R.drawable.box_red_white_blue,
            "cab_monster" to R.drawable.cab_monster,
            "cosy_deer" to R.drawable.cosy_deer,
            "cyber_katana" to R.drawable.cyber_katana,
            "dog_1" to R.drawable.dog_1,
            "dog_2" to R.drawable.dog_2,
            "dog_3" to R.drawable.dog_3,
            "frog_derpy" to R.drawable.frog_derpy,
            "fuchsia_agent" to R.drawable.fuchsia_agent,
            "hoppy_day" to R.drawable.hoppy_day,
            "icicle_gleaming" to R.drawable.icicle_gleaming,
            "icicle_snowing" to R.drawable.icicle_snowing,
            "lover_girl_autumn" to R.drawable.lover_girl_autumn,
            "neon_cat_hoodie_black" to R.drawable.neon_cat_hoodie_black,
            "neon_cat_hoodie_pink" to R.drawable.neon_cat_hoodie_pink,
            "sakura" to R.drawable.sakura,
            "sakura_gyoiko" to R.drawable.sakura_gyoiko,
            "sakura_pink" to R.drawable.sakura_pink,
            "sakura_ukon" to R.drawable.sakura_ukon,
            "towerborne_play" to R.drawable.towerborne_play,
            "treat_ghost" to R.drawable.treat_ghost,
            "treat_pumpkin" to R.drawable.treat_pumpkin,
            "trick_pumpkin" to R.drawable.trick_pumpkin,
            "ultron" to R.drawable.ultron,
        )
        named.forEach { (id, res) ->
            add(Frame(id, id.replace('_', ' ').replaceFirstChar { it.uppercase() }, res))
        }
    }

    fun byId(id: String?): Frame = ALL.firstOrNull { it.id == id } ?: ALL.first()
}

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
    val photoScale = if (hasFrame) 0.78f else 1f
    val photoSize = size * photoScale

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
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
