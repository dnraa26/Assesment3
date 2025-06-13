package com.danira0037.assesment3.ui.screen

import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.danira0037.assesment3.BuildConfig
import com.danira0037.assesment3.R
import com.danira0037.assesment3.model.Buku
import com.danira0037.assesment3.model.User
import com.danira0037.assesment3.network.ApiStatus
import com.danira0037.assesment3.network.UserDataStore
import com.danira0037.assesment3.ui.theme.Assesment3Theme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User())

    val viewModel: MainViewModel = viewModel()

    var showDialog by remember { mutableStateOf(false) }
    var showBukuDialog by remember { mutableStateOf(false) }

    var bukuToDelete by remember { mutableStateOf<Buku?>(null) }

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    val launcher = rememberLauncherForActivityResult(CropImageContract()) {
        bitmap = getCroppedImage(context.contentResolver, it)
        if (bitmap != null) showBukuDialog = true
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var editedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val editLauncher = rememberLauncherForActivityResult(CropImageContract()) {
        editedBitmap = getCroppedImage(context.contentResolver, it)
    }

    var selectedBuku by remember { mutableStateOf<Buku?>(null)}

    val error by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = {
                        if (user.email.isEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch { signIn(dataStore,context) }
                        }
                        else {
                            showDialog = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.account_circle),
                            contentDescription = stringResource(id = R.string.profile),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                    if (!viewModel.isUploading.value) {
                        if (user.email.isEmpty()) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.add_error),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val options = CropImageContractOptions(
                                null, CropImageOptions(
                                    imageSourceIncludeGallery = false,
                                    imageSourceIncludeCamera = true,
                                    fixAspectRatio = true
                                )
                            )
                            launcher.launch(options)
                        }
                    }
                },
                containerColor = if(!viewModel.isUploading.value) MaterialTheme.colorScheme.primaryContainer else Color.Gray,
                contentColor = Color.White
            ) {
                if (viewModel.isUploading.value)
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                )else(
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.tambah_hewan)
                    )
                )
            }
        }

    ) { innerPadding ->
        ScreenContent(
            user,
            viewModel,
            Modifier.padding(innerPadding),
            onEditClick = { buku ->
                selectedBuku = buku
                showEditDialog = true
            },
            onDeleteClick = { buku ->
                bukuToDelete = buku
            }
        )

        if(showDialog){
            ProfilDialog(
                user = user,
                onDismissRequest = { showDialog = false },
            ){
                CoroutineScope(Dispatchers.IO).launch { signOut(context, dataStore) }
                showDialog = false
            }
        }

        if (showBukuDialog) {
            BukuDialog(
                bitmap = bitmap,
                onDismissRequest = { showBukuDialog = false }
            ) { namaBuku, author ->
                viewModel.uploadAndPostPakaian(
                    bitmap = bitmap!!,
                    namaBuku = namaBuku,
                    author = author,
                    email = user.email,
                    onSuccess = {
                        showBukuDialog = false
                    }
                )
                Log.d("TAMBAH", "$namaBuku $author ditambahkan.")
                showBukuDialog = false
            }
        }

        if (showEditDialog && selectedBuku != null) {
            EditBukuDialog(
                initialData = selectedBuku!!,
                currentBitmap = editedBitmap,
                onGantiGambar = {
                    val options = CropImageContractOptions(
                        null,
                        CropImageOptions(
                            imageSourceIncludeCamera = true,
                            imageSourceIncludeGallery = false,
                            fixAspectRatio = true
                        )
                    )
                    editLauncher.launch(options)
                },
                onDismissRequest = {
                    showEditDialog = false
                    selectedBuku = null
                },
                isUploading = viewModel.isUploading.value
            ){ namaBuku, author ->
                if (editedBitmap != null) {
                    viewModel.uploadImageToImgBB(
                        editedBitmap!!
                    ) { newUrl ->
                        val updated = selectedBuku!!.copy(
                            namaBuku = namaBuku,
                            author = author,
                            gambar = newUrl
                        )
                        viewModel.updateBuku(updated) {
                            showEditDialog = false
                            selectedBuku = null
                            editedBitmap = null
                        }
                    }
                } else {
                    val updated = selectedBuku!!.copy(
                        namaBuku = namaBuku,
                        author = author
                    )
                    viewModel.updateBuku(updated) {
                        showEditDialog = false
                        selectedBuku = null
                    }
                }
            }
        }

        if (bukuToDelete != null) {
            AlertDialog(
                onDismissRequest = { bukuToDelete = null },
                title = { Text("Konfirmasi Hapus") },
                text = { Text("Yakin ingin menghapus pakaian ini?") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.deleteBuku(bukuToDelete!!.id)
                        bukuToDelete = null
                    }) { Text("Hapus") }
                },
                dismissButton = {
                    Button(onClick = { bukuToDelete = null }) { Text("Batal") }
                }
            )
        }

        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }

    }
}

@Composable
fun ScreenContent(user: User, viewModel: MainViewModel, modifier: Modifier = Modifier, onEditClick:(Buku) -> Unit, onDeleteClick:(Buku) -> Unit) {
    val data by viewModel.data
    val status by viewModel.status.collectAsState()

    when(status){
        ApiStatus.LOADING -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                CircularProgressIndicator()
            }
        }
        ApiStatus.SUCCESS -> {

                val filteredData = data.filter { it.owner == user.email }
                if(user.email.isEmpty()){
                        Column (
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ){
                            Text(text = stringResource(id = R.string.login_dahulu))
                        }
                }else {
                    if (filteredData.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = stringResource(id = R.string.list_kosong))
                            }
                    } else {
                        LazyVerticalGrid(
                            modifier = modifier.fillMaxSize().padding(4.dp),
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                        items(data) {
                            if (it.owner == user.email) {
                                ListItem(
                                    it,
                                    {
                                        onEditClick(it)
                                    },
                                    {
                                        onDeleteClick(it)
                                    }
                                )
                            }

                        }
                    }
                }
            }
        }

        ApiStatus.FAILED -> {
            Column (
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = stringResource(id = R.string.error)
                )

                Button(
                    onClick = {
                        viewModel.retrieveData()
                    },
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.try_again)
                    )
                }
            }
        }
    }

}

@Composable
fun ListItem(buku: Buku, onClick :() -> Unit,onDelete :() -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = buku.gambar,
                contentDescription = stringResource(R.string.gambar, buku.namaBuku),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.loading_img),
                error = painterResource(id = R.drawable.broken_image),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = buku.namaBuku,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = buku.author,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { onClick() }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onDelete() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
private suspend fun signIn(dataStore: UserDataStore,context: Context){
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request : GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException){
        Log.e("SIGN-IN","Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(
    result: GetCredentialResponse,
    dataStore: UserDataStore
){
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            val nama = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri.toString()
            dataStore.saveData(User(nama, email, photoUrl))
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore){
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User())
    }catch (e: ClearCredentialException){
        Log.e("SIGN-IN","Error: ${e.errorMessage}")
    }
}

private fun getCroppedImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
): Bitmap? {
    if (!result.isSuccessful) {
        Log.e("IMAGE", "Error: ${result.error}")
        return null
    }

    val uri = result.uriContent ?: return null

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        MediaStore.Images.Media. getBitmap(resolver, uri)
    } else {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}


@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun MainScreenPreview() {
    Assesment3Theme {
        MainScreen()
    }
}