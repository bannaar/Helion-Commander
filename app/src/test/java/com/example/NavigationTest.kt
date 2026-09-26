package com.example

import com.example.helion.ui.HelionDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NavigationTest {

    @Test
    fun testNavigationRoutesConfigured() {
        val fleet = HelionDestination.FLEET
        val systems = HelionDestination.STAR_SYSTEMS
        val markets = HelionDestination.MARKETS
        val comms = HelionDestination.COMMUNICATIONS
        val missions = HelionDestination.TACTICAL_MISSIONS

        assertEquals("fleet", fleet.route)
        assertEquals("Fleet", fleet.title)

        assertEquals("star_systems", systems.route)
        assertEquals("Star Systems", systems.title)

        assertEquals("markets", markets.route)
        assertEquals("Markets", markets.title)

        assertEquals("communications", comms.route)
        assertEquals("Communications", comms.title)

        assertEquals("tactical_missions", missions.route)
        assertEquals("Missions", missions.title)

        assertNotNull(fleet.icon)
        assertNotNull(systems.icon)
        assertNotNull(markets.icon)
        assertNotNull(comms.icon)
        assertNotNull(missions.icon)
    }
}
