package com.example.oauth_impl.navigation

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.oauth_impl.ui.HomeScreen
import com.example.oauth_impl.ui.LoginScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)
    val signInIntent: Intent = googleSignInClient.signInIntent
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(navController, task)
            }
        }

    val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

    LaunchedEffect(key1 = account) {
        if (account != null) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) {
                    inclusive = true
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen { launcher.launch(signInIntent) }
        }
        composable(Screen.Home.route) {
            HomeScreen {
                googleSignInClient.signOut().addOnCompleteListener {
                    navController.popBackStack()
                    navController.navigate(Screen.Login.route)
                }
            }
        }
    }
}

private fun handleSignInResult(
    navController: NavController,
    completedTask: Task<GoogleSignInAccount>
) {
    runCatching {
        val account = completedTask.getResult(ApiException::class.java)
    }.onSuccess {
        navController.popBackStack()
        navController.navigate(Screen.Home.route)
    }.onFailure {
        Log.e("SignInFeedback", "Something went wrong.")
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Home : Screen("home_screen")
}