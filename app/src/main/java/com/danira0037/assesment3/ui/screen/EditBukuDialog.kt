package com.danira0037.assesment3.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.danira0037.assesment3.R
import com.danira0037.assesment3.model.Buku

@Composable
fun EditBukuDialog(
    initialData: Buku,
    onDismissRequest: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var namaBuku by remember { mutableStateOf(initialData.namaBuku) }
    var author by remember { mutableStateOf(initialData.author) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = { onSubmit(namaBuku, author) }) {
                Text(stringResource(id = R.string.simpan))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text(stringResource(id = R.string.batal))
            }
        },
        title = { Text(stringResource(id = R.string.edit_buku))},
        text = {
            Column {
                OutlinedTextField(
                    value = namaBuku,
                    onValueChange = { namaBuku = it },
                    label = { Text(stringResource(id = R.string.namaBuku)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text(stringResource(id = R.string.author)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}