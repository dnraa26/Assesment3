package com.danira0037.assesment3.ui.screen

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.danira0037.assesment3.R
import com.danira0037.assesment3.model.Buku

@Composable
fun EditBukuDialog(
    initialData: Buku,
    currentBitmap: Bitmap?,
    onGantiGambar: () -> Unit,
    onDismissRequest: () -> Unit,
    isUploading : Boolean,
    onSubmit: (String, String) -> Unit
) {
    var namaBuku by remember { mutableStateOf(initialData.namaBuku) }
    var author by remember { mutableStateOf(initialData.author) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = {
                    if(!isUploading)
                    onSubmit(namaBuku, author)
                },
                enabled = if(!isUploading) namaBuku.isNotEmpty() && author.isNotEmpty() else false,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(!isUploading) MaterialTheme.colorScheme.primaryContainer else Color.Gray,
                    contentColor = Color.White
                )
            ) {
                if(isUploading){
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp)
                    )
                } else {
                    Text(stringResource(id = R.string.simpan))
                }
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
                if (currentBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = currentBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AsyncImage(
                        model = initialData.gambar,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Button(
                    onClick = { onGantiGambar() },
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                ) {
                    Text(stringResource(id = R.string.ganti_gambar))
                }
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