package com.example.helion.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.helion.core.HelionAppContainer
import com.example.helion.ui.commander.CommanderProfileScreen
import com.example.helion.ui.commander.CommanderViewModel
import com.example.helion.ui.comms.CommsScreen
import com.example.helion.ui.comms.CommsViewModel
import com.example.helion.ui.components.HelionHeader
import com.example.helion.ui.fleet.FleetScreen
import com.example.helion.ui.fleet.FleetViewModel
import com.example.helion.ui.galnet.GalNetScreen
import com.example.helion.ui.galnet.GalNetViewModel
import com.example.helion.ui.guild.GuildScreen
import com.example.helion.ui.guild.GuildViewModel
import com.example.helion.ui.home.HomeDashboardScreen
import com.example.helion.ui.home.HomeViewModel
import com.example.helion.ui.market.MarketScreen
import com.example.helion.ui.market.MarketViewModel
import com.example.helion.ui.missions.TacticalMissionsScreen
import com.example.helion.ui.missions.TacticalMissionsViewModel
import com.example.helion.ui.settings.SettingsScreen
import com.example.helion.ui.settings.SettingsViewModel
import com.example.helion.ui.universe.UniverseScreen
import com.example.helion.ui.universe.UniverseViewModel
import com.example.ui.theme.HelionCyan
import com.example.ui.theme.HelionDeepGraphite
import com.example.ui.theme.HelionSurface
import com.example.ui.theme.HelionTextMuted
import com.example.ui.theme.HelionVoidBlack

enum class HelionDestination(val route: String, val title: String, val icon: ImageVector) {
    HOME("home", "Home", Icons.Default.Home),
    TACTICAL_MISSIONS("tactical_missions", "Missions", Icons.Default.GpsFixed),
    FLEET("fleet", "Fleet", Icons.Default.Rocket),
    STAR_SYSTEMS("star_systems", "Star Systems", Icons.Default.Explore),
    MARKETS("markets", "Markets", Icons.Default.ShoppingCart),
    COMMUNICATIONS("communications", "Communications", Icons.Default.Email),
    COMMANDER("commander", "Commander", Icons.Default.Person),
    GALNET("galnet", "GalNet", Icons.Default.Newspaper),
    GUILD("guild", "Guild", Icons.Default.Group),
    SETTINGS("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun HelionApp(
    container: HelionAppContainer,
    initialDestination: String? = null,
    initialEntityId: String? = null,
    onDestinationHandled: () -> Unit = {}
) {
    val navController = rememberNavController()

    // ViewModels
    val homeViewModel = remember { HomeViewModel(container) }
    val commanderViewModel = remember { CommanderViewModel(container) }
    val fleetViewModel = remember { FleetViewModel(container) }
    val universeViewModel = remember { UniverseViewModel(container) }
    val marketViewModel = remember { MarketViewModel(container) }
    val galNetViewModel = remember { GalNetViewModel(container) }
    val guildViewModel = remember { GuildViewModel(container) }
    val commsViewModel = remember { CommsViewModel(container) }
    val settingsViewModel = remember { SettingsViewModel(container) }
    val missionsViewModel = remember { TacticalMissionsViewModel(container) }

    // Request notification permission on Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!container.notificationManager.hasNotificationPermission()) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Header State
    val commanderProfile by container.commanderRepository.commanderState.collectAsState()
    val environment by container.settingsRepository.currentEnvironment.collectAsState()
    val isOffline by container.settingsRepository.isOfflineSimulated.collectAsState()

    // Backstack entry observation for dynamic selection highlight
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: HelionDestination.HOME.route

    fun navigateTo(dest: HelionDestination) {
        navController.navigate(dest.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Handle incoming notification navigation
    LaunchedEffect(initialDestination, initialEntityId) {
        if (initialDestination != null) {
            when (initialDestination) {
                HelionDestination.TACTICAL_MISSIONS.route, "missions", "tactical_missions" -> {
                    if (initialEntityId != null) {
                        val m = missionsViewModel.uiState.value.missions.find { it.id == initialEntityId }
                        if (m != null) missionsViewModel.selectMission(m)
                    }
                    navigateTo(HelionDestination.TACTICAL_MISSIONS)
                }
                HelionDestination.FLEET.route, "fleet" -> {
                    if (initialEntityId != null) {
                        val s = fleetViewModel.uiState.value.ownedShips.find { it.instanceId == initialEntityId }
                        if (s != null) fleetViewModel.selectShip(s)
                    }
                    navigateTo(HelionDestination.FLEET)
                }
                HelionDestination.MARKETS.route, "markets" -> {
                    if (initialEntityId != null) {
                        val c = marketViewModel.uiState.value.localCommodities.find { it.commodityId == initialEntityId }
                            ?: marketViewModel.uiState.value.regionalCommodities.find { it.commodityId == initialEntityId }
                        if (c != null) marketViewModel.selectCommodity(c)
                    }
                    navigateTo(HelionDestination.MARKETS)
                }
            }
            onDestinationHandled()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(HelionVoidBlack)) {
        val isWideScreen = maxWidth >= 700.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Adaptive Navigation Rail on Tablet / Wide screens
            if (isWideScreen) {
                NavigationRail(
                    containerColor = HelionDeepGraphite,
                    contentColor = HelionCyan,
                    modifier = Modifier.fillMaxHeight().testTag("adaptive_nav_rail")
                ) {
                    HelionDestination.values().forEach { dest ->
                        val isSelected = currentRoute == dest.route
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { navigateTo(dest) },
                            icon = { Icon(imageVector = dest.icon, contentDescription = dest.title) },
                            label = { Text(dest.title, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("nav_rail_${dest.route}"),
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = HelionCyan,
                                selectedTextColor = HelionCyan,
                                indicatorColor = HelionSurface,
                                unselectedIconColor = HelionTextMuted,
                                unselectedTextColor = HelionTextMuted
                            )
                        )
                    }
                }
            }

            Scaffold(
                topBar = {
                    HelionHeader(
                        commander = commanderProfile,
                        environment = environment,
                        isOffline = isOffline,
                        onLogoClick = { navigateTo(HelionDestination.HOME) },
                        onCommanderClick = { navigateTo(HelionDestination.COMMANDER) },
                        onEnvironmentClick = { navigateTo(HelionDestination.SETTINGS) }
                    )
                },
                bottomBar = {
                    if (!isWideScreen) {
                        // Phone bottom navigation to toggle between Home, Missions, Fleet, Star Systems, Markets, Communications
                        val primaryDestinations = listOf(
                            HelionDestination.HOME,
                            HelionDestination.TACTICAL_MISSIONS,
                            HelionDestination.FLEET,
                            HelionDestination.STAR_SYSTEMS,
                            HelionDestination.MARKETS,
                            HelionDestination.COMMUNICATIONS
                        )
                        NavigationBar(
                            containerColor = HelionDeepGraphite,
                            contentColor = HelionCyan,
                            modifier = Modifier.testTag("phone_bottom_nav")
                        ) {
                            primaryDestinations.forEach { dest ->
                                val isSelected = currentRoute == dest.route
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { navigateTo(dest) },
                                    icon = {
                                        Icon(
                                            imageVector = dest.icon,
                                            contentDescription = dest.title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    label = { Text(dest.title, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.testTag("bottom_nav_${dest.route}"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = HelionCyan,
                                        selectedTextColor = HelionCyan,
                                        indicatorColor = HelionSurface,
                                        unselectedIconColor = HelionTextMuted,
                                        unselectedTextColor = HelionTextMuted
                                    )
                                )
                            }
                        }
                    }
                },
                containerColor = HelionVoidBlack
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = HelionDestination.HOME.route,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable(HelionDestination.HOME.route) {
                        HomeDashboardScreen(
                            viewModel = homeViewModel,
                            onNavigateToCommander = { navigateTo(HelionDestination.COMMANDER) },
                            onNavigateToFleet = { navigateTo(HelionDestination.FLEET) },
                            onNavigateToUniverse = { navigateTo(HelionDestination.STAR_SYSTEMS) },
                            onNavigateToMarket = { navigateTo(HelionDestination.MARKETS) },
                            onNavigateToGalNet = { navigateTo(HelionDestination.GALNET) },
                            onNavigateToGuild = { navigateTo(HelionDestination.GUILD) },
                            onNavigateToComms = { navigateTo(HelionDestination.COMMUNICATIONS) },
                            onNavigateToSettings = { navigateTo(HelionDestination.SETTINGS) },
                            onNavigateToMissions = { navigateTo(HelionDestination.TACTICAL_MISSIONS) }
                        )
                    }
                    composable(HelionDestination.TACTICAL_MISSIONS.route) {
                        TacticalMissionsScreen(
                            viewModel = missionsViewModel,
                            onNavigateToUniverse = { systemId ->
                                if (systemId != null) {
                                    universeViewModel.selectSystemById(systemId)
                                }
                                navigateTo(HelionDestination.STAR_SYSTEMS)
                            }
                        )
                    }
                    composable(HelionDestination.FLEET.route) {
                        FleetScreen(viewModel = fleetViewModel)
                    }
                    composable(HelionDestination.STAR_SYSTEMS.route) {
                        UniverseScreen(viewModel = universeViewModel)
                    }
                    composable(HelionDestination.MARKETS.route) {
                        MarketScreen(viewModel = marketViewModel)
                    }
                    composable(HelionDestination.COMMUNICATIONS.route) {
                        CommsScreen(viewModel = commsViewModel)
                    }
                    composable(HelionDestination.COMMANDER.route) {
                        CommanderProfileScreen(viewModel = commanderViewModel)
                    }
                    composable(HelionDestination.GALNET.route) {
                        GalNetScreen(viewModel = galNetViewModel)
                    }
                    composable(HelionDestination.GUILD.route) {
                        GuildScreen(viewModel = guildViewModel)
                    }
                    composable(HelionDestination.SETTINGS.route) {
                        SettingsScreen(viewModel = settingsViewModel)
                    }
                }
            }
        }
    }
}
