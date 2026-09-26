package com.example.helion.core.navigation

import com.example.helion.core.model.RouteOptimizationMode
import com.example.helion.core.model.RoutePlanResult
import com.example.helion.core.model.RouteSegment
import com.example.helion.core.model.SecurityClass
import com.example.helion.core.model.StarLaneConnection
import com.example.helion.core.model.StarLaneType
import com.example.helion.core.model.SystemNode
import java.util.PriorityQueue

object GalaxyRouter {

    /**
     * Compute path between originSystem and destinationSystem given full galaxy node map.
     */
    fun findRoute(
        originId: String,
        destinationId: String,
        allSystems: Map<String, SystemNode>,
        mode: RouteOptimizationMode
    ): RoutePlanResult? {
        val origin = allSystems[originId] ?: return null
        val destination = allSystems[destinationId] ?: return null

        if (originId == destinationId) {
            return RoutePlanResult(
                origin = origin,
                destination = destination,
                segments = emptyList(),
                totalJumps = 0,
                totalDistanceLy = 0f,
                lowestSecurityRating = origin.securityRating,
                sovereigntyTransitions = emptyList(),
                warnings = listOf("Origin and destination are the same system."),
                modeUsed = mode
            )
        }

        if (!isSystemAllowedUnderMode(destination, mode)) {
            return null
        }

        // Dijkstra implementation
        val distances = mutableMapOf<String, Float>().withDefault { Float.MAX_VALUE }
        val previousNode = mutableMapOf<String, String>()
        val previousConnection = mutableMapOf<String, StarLaneConnection>()
        val queue = PriorityQueue<Pair<String, Float>>(compareBy { it.second })

        distances[originId] = 0f
        queue.add(originId to 0f)

        val visited = mutableSetOf<String>()

        while (queue.isNotEmpty()) {
            val (currentId, currentDist) = queue.poll() ?: break

            if (currentId == destinationId) break
            if (currentId in visited) continue
            visited.add(currentId)

            val currentNode = allSystems[currentId] ?: continue

            for (conn in currentNode.connections) {
                val neighborId = conn.targetSystemId
                val neighborNode = allSystems[neighborId] ?: continue

                // Check mode restrictions
                if (!isSystemAllowedUnderMode(neighborNode, mode)) {
                    continue
                }

                val edgeWeight = calculateEdgeWeight(currentNode, neighborNode, conn, mode)
                val newDist = currentDist + edgeWeight

                if (newDist < distances.getValue(neighborId)) {
                    distances[neighborId] = newDist
                    previousNode[neighborId] = currentId
                    previousConnection[neighborId] = conn
                    queue.add(neighborId to newDist)
                }
            }
        }

        if (!previousNode.containsKey(destinationId)) {
            // No route found with current mode constraints
            return null
        }

        // Reconstruct path
        val pathIds = mutableListOf<String>()
        var curr: String? = destinationId
        while (curr != null) {
            pathIds.add(0, curr)
            curr = previousNode[curr]
        }

        val segments = mutableListOf<RouteSegment>()
        var totalDistance = 0f
        var lowestSec = origin.securityRating
        val sovereigntyTransitions = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        for (i in 0 until pathIds.size - 1) {
            val fromNode = allSystems[pathIds[i]]!!
            val toNode = allSystems[pathIds[i + 1]]!!
            val conn = previousConnection[pathIds[i + 1]]
                ?: StarLaneConnection(toNode.systemId, 12.0f, StarLaneType.SECONDARY)

            segments.add(
                RouteSegment(
                    fromSystem = fromNode,
                    toSystem = toNode,
                    laneType = conn.laneType,
                    distanceLy = conn.distanceLy
                )
            )

            totalDistance += conn.distanceLy
            if (toNode.securityRating < lowestSec) {
                lowestSec = toNode.securityRating
            }

            if (fromNode.sovereignName != toNode.sovereignName) {
                sovereigntyTransitions.add("${fromNode.sovereignName} -> ${toNode.sovereignName}")
            }

            if (toNode.securityClass == SecurityClass.LOW_SECURITY) {
                warnings.add("Caution: Gate entry into low-sec space [${toNode.name} (${String.format("%.1f", toNode.securityRating)})].")
            } else if (toNode.securityClass == SecurityClass.NULL_SECURITY) {
                warnings.add("DANGER: 0.0 Null-Sec sector [${toNode.name}]. No naval defense or gate gun protection.")
            }
        }

        return RoutePlanResult(
            origin = origin,
            destination = destination,
            segments = segments,
            totalJumps = segments.size,
            totalDistanceLy = totalDistance,
            lowestSecurityRating = lowestSec,
            sovereigntyTransitions = sovereigntyTransitions.distinct(),
            warnings = warnings.distinct(),
            modeUsed = mode
        )
    }

    private fun isSystemAllowedUnderMode(node: SystemNode, mode: RouteOptimizationMode): Boolean {
        return when (mode) {
            RouteOptimizationMode.HIGH_SEC_ONLY -> node.securityClass == SecurityClass.HIGH_SECURITY
            RouteOptimizationMode.AVOID_LOW_SEC -> node.securityClass != SecurityClass.LOW_SECURITY
            RouteOptimizationMode.AVOID_NULLSEC -> node.securityClass != SecurityClass.NULL_SECURITY
            else -> true
        }
    }

    private fun calculateEdgeWeight(
        from: SystemNode,
        to: SystemNode,
        conn: StarLaneConnection,
        mode: RouteOptimizationMode
    ): Float {
        val baseCost = conn.distanceLy

        return when (mode) {
            RouteOptimizationMode.FASTEST -> {
                // Trunk lanes provide travel speed bonus
                baseCost / conn.laneType.speedMultiplier
            }
            RouteOptimizationMode.SAFEST -> {
                // High security systems receive huge weight discounts; low-sec and null-sec are penalized
                when (to.securityClass) {
                    SecurityClass.HIGH_SECURITY -> baseCost * 0.7f
                    SecurityClass.LOW_SECURITY -> baseCost * 4.0f
                    SecurityClass.NULL_SECURITY -> baseCost * 12.0f
                }
            }
            RouteOptimizationMode.TRADE_ROUTE -> {
                var factor = 1.0f
                if (conn.laneType == StarLaneType.MAJOR_INTERFACTION_TRUNK) factor *= 0.5f
                if (to.isCommerceHub) factor *= 0.7f
                if (to.securityClass != SecurityClass.HIGH_SECURITY) factor *= 3.0f
                baseCost * factor
            }
            else -> baseCost
        }
    }
}
