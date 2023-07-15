package com.eproduce.nourindx

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eproduce.nourindx.ui.theme.NourindxTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Result
import com.google.android.gms.tasks.Task


const val RC_SIGN_IN = 1000
const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val gsc = GoogleSignIn.getClient(this, gso)

        setContent {
            NourindxTheme {
                // A surface container using the 'background' color from the theme
                MyApp(
                    gsc.signInIntent,
                    GoogleSignIn.getLastSignedInAccount(this),
                    {gsc.signOut()})
            }
        }
    }

    @Composable
    fun MyApp(gcIntent:Intent, lastAccount : GoogleSignInAccount?, signOut:()->Unit, modifier: Modifier = Modifier){
        var mState by remember { mutableStateOf(lastAccount!=null) }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()) { result ->
            mState = result.resultCode== RESULT_OK
            if(mState){
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    val account = task.getResult(ApiException::class.java)
                    Log.w(TAG, "getAccount:${account.email.toString()}")
                } catch (e: ApiException) {
                    Log.w(TAG, "signInResult:failed code=" + e.statusCode)
                }
            }
        }

        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.background
        ) {
            Log.w(TAG, "Composing")
            if (!mState){
                SignInWindow({launcher.launch(gcIntent)})
            } else {
                GoogleSignIn.getLastSignedInAccount(this)?.email?.let {
                    DebugWindow(it, {signOut(); mState = !mState})
                }
            }
        }
    }

    @Composable
    fun SignInWindow(onClick: () -> Unit, modifier: Modifier = Modifier) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = onClick) {
                Text(text = stringResource(R.string.signIn))
            }
        }
    }

    @Composable
    fun DebugWindow(
        accountInfo: String,
        signOut: ()->Unit,
        modifier: Modifier = Modifier,
        names: List<String> = List(1000) { "$it" }
    ) {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            Text(text = accountInfo)
            Button(onClick = signOut){Text(text = stringResource(R.string.signOut))}
            LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
                items(items = names) { name ->
                    FileCard(name = name)
                }
            }
        }
    }


    @Composable
    fun FileCard(name: String, modifier: Modifier = Modifier) {
        var expanded by remember { mutableStateOf(false) }
        val extraPadding = if (expanded) 48.dp else 0.dp

        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Row(modifier = modifier.padding(24.dp)) {
                Column(
                    modifier = modifier
                        .weight(1f)
                        .padding(bottom = extraPadding)
                ) {
                    Text(text = "Hello, ")
                    Text(text = name)
                }
                ElevatedButton(
                    onClick = { expanded = !expanded }
                ) {
                    Text(if (expanded) "Show less" else "Show more")
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun MyAppPreview() {
        NourindxTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                SignInWindow({})
            }
        }
    }
}
