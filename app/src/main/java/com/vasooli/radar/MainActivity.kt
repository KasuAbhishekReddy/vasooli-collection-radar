package com.vasooli.radar

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vasooli.radar.ui.AppViewModel
import com.vasooli.radar.ui.screens.AddEntryScreen
import com.vasooli.radar.ui.screens.AddRetailerScreen
import com.vasooli.radar.ui.screens.DashboardScreen
import com.vasooli.radar.ui.screens.DetailScreen
import com.vasooli.radar.ui.screens.RetailersScreen
import com.vasooli.radar.ui.screens.UsageScreen
import com.vasooli.radar.ui.theme.VasooliTheme

object Routes {
    const val DASHBOARD = "dashboard"
    const val RETAILERS = "retailers"
    const val USAGE = "usage"
    const val ADD_RETAILER = "add_retailer"
    fun editRetailer(id: Long) = "edit_retailer/$id"
    fun detail(id: Long) = "detail/$id"
    fun entry(id: Long, kind: String) = "entry/$id/$kind"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        val repo = (application as VasooliApp).repository
        setContent {
            VasooliTheme {
                val vm: AppViewModel = viewModel(factory = AppViewModel.factory(repo))
                AppRoot(vm)
            }
        }
    }
}

@Composable
fun AppRoot(vm: AppViewModel) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) { DashboardScreen(vm, nav) }
        composable(Routes.RETAILERS) { RetailersScreen(vm, nav) }
        composable(Routes.USAGE) { UsageScreen(vm, nav) }
        composable(Routes.ADD_RETAILER) { AddRetailerScreen(vm, nav) }
        composable("edit_retailer/{id}") { entry ->
            AddRetailerScreen(vm, nav, entry.arguments?.getString("id")?.toLongOrNull())
        }
        composable("detail/{id}") { entry ->
            DetailScreen(vm, nav, entry.arguments?.getString("id")?.toLongOrNull() ?: 0L)
        }
        composable("entry/{id}/{kind}") { entry ->
            AddEntryScreen(
                vm, nav,
                entry.arguments?.getString("id")?.toLongOrNull() ?: 0L,
                entry.arguments?.getString("kind") ?: "sale"
            )
        }
    }
}

@Composable
fun BottomBar(nav: NavController, current: String) {
    NavigationBar {
        NavigationBarItem(
            selected = current == Routes.DASHBOARD,
            onClick = {
                if (current != Routes.DASHBOARD) nav.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.DASHBOARD) { inclusive = true }; launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Filled.Radar, contentDescription = null) },
            label = { Text("Radar") }
        )
        NavigationBarItem(
            selected = current == Routes.RETAILERS,
            onClick = {
                if (current != Routes.RETAILERS) nav.navigate(Routes.RETAILERS) {
                    popUpTo(Routes.DASHBOARD); launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Filled.Store, contentDescription = null) },
            label = { Text("Retailers") }
        )
        NavigationBarItem(
            selected = current == Routes.USAGE,
            onClick = {
                if (current != Routes.USAGE) nav.navigate(Routes.USAGE) {
                    popUpTo(Routes.DASHBOARD); launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Filled.Whatshot, contentDescription = null) },
            label = { Text("Usage") }
        )
    }
}
