package com.vidora.app.presentation.ui.channel.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    showBottomSheet: MutableState<Boolean>,
    text1: String,
    text2: String,
    icon1: Int,
    icon2: Int,
    onClick1: () -> Unit,
    onClick2: () -> Unit
){
    if (showBottomSheet.value){
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        LaunchedEffect(Unit) {
            sheetState.show()
        }

        ModalBottomSheet(
            onDismissRequest = { showBottomSheet.value = false },
            sheetState = sheetState,
            content = {
                Column(
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ListItem(
                        headlineContent = {
                            Text(text1)
                        },
                        leadingContent = {
                            Icon(painter = painterResource(icon1), contentDescription = text1)
                        },
                        modifier = Modifier.clickable { onClick1() }
                    )
                    ListItem(
                        headlineContent = {
                            Text(text2)
                        },
                        leadingContent = {
                            Icon(painter = painterResource(icon2), contentDescription = text2)
                        },
                        modifier = Modifier.clickable { onClick2() }
                    )
                }
            }
        )
    }
}
