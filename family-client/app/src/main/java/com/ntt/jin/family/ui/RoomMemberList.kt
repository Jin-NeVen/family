package com.ntt.jin.family.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RoomMemberList(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(10) { index ->
            RoomMember(index, modifier)
        }
    }
}

@Composable
fun RoomMember(
    index: Int,
    modifier: Modifier = Modifier) {
    var isChecked by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(16.dp)
            .background(Color.LightGray)
    ) {
        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Member $index",
            modifier = Modifier
                .padding(end = 8.dp)
        )
        Switch(
            checked = isChecked,
            onCheckedChange = { isChecked ->
                // トグルボタンの状態が変更された時の処理を行います。
            },
        )
    }
}

@Preview
@Composable
fun RoomMemberListPreview() {
    RoomMemberList()
}