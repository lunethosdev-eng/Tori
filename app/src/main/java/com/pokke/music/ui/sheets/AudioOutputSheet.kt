package com.pokke.music.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AudioOutputSheet(onDismiss: () -> Unit) {
    var volume by remember { mutableFloatStateOf(0.7f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Audio output",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        OutputItem(
            icon = Icons.Default.PhoneAndroid,
            title = "This phone",
            subtitle = null,
            selected = false,
            onClick = {}
        )
        OutputItem(
            icon = Icons.Default.Speaker,
            title = "CMF Buds 2a",
            subtitle = "Playing here",
            selected = true,
            onClick = {}
        )
        OutputItem(
            icon = Icons.Outlined.Cast,
            title = "Cast",
            subtitle = "Chromecast and Google Cast speakers",
            selected = false,
            onClick = {},
            showArrow = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Speaker, null, tint = Color.White.copy(0.6f), modifier = Modifier.size(20.dp))
            Slider(
                value = volume,
                onValueChange = { volume = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(0.25f)
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutputItem(
            icon = Icons.Outlined.GraphicEq,
            title = "Audio Pipeline",
            subtitle = "16-bit PCM · 48 kHz",
            selected = false,
            onClick = {},
            showArrow = true
        )
    }
}

@Composable
private fun OutputItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
    showArrow: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFF2A2A2A) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, color = Color.White.copy(0.55f), fontSize = 13.sp)
            }
        }
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}
