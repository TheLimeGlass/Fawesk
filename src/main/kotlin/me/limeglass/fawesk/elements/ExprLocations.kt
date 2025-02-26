package me.limeglass.fawesk.elements

import ch.njol.skript.Skript
import ch.njol.skript.expressions.ExprInput
import ch.njol.skript.lang.Condition
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.InputSource
import ch.njol.skript.lang.InputSource.InputData
import ch.njol.skript.lang.SkriptParser.ParseResult
import ch.njol.skript.lang.parser.ParserInstance
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.util.Kleenean
import com.google.common.collect.Iterators
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.event.Event
import java.util.concurrent.CompletableFuture

class ExprLocations : SimpleExpression<Location>(), InputSource {

    companion object {
        init {
            Skript.registerExpression(ExprLocations::class.java, Location::class.java, ExpressionType.SIMPLE,
                "(worldedit|fawe) locations (within|from) %location% (to|and) %location%",
                "(worldedit|fawe) locations (within|from) %location% (to|and) %location% (where|that match) \\[<.+>\\]"
            )
            if (!ParserInstance.isRegistered(InputData::class.java))
                ParserInstance.registerData(InputData::class.java) { InputData(ParserInstance.get()) }
        }
    }

    private val dependentInputs = mutableSetOf<ExprInput<*>>()
    private var location1: Expression<Location>? = null
    private var location2: Expression<Location>? = null
    private var filterCondition: Condition? = null
    private var unparsedCondition: String? = null
    private var currentValue: Any? = null

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: ParseResult
    ): Boolean {
        location1 = expressions[0] as Expression<Location>
        location2 = expressions[1] as Expression<Location>
        if (matchedPattern == 0) return true

        unparsedCondition = parseResult.regexes[0].group()
        val inputData = parser.getData(InputData::class.java).apply { source = this@ExprLocations }
        filterCondition = Condition.parse(unparsedCondition, "Can't understand this condition: $unparsedCondition")
        inputData.source = this
        return filterCondition != null
    }

    override fun iterator(event: Event?): Iterator<Location>? {
        return CompletableFuture.supplyAsync {
            val locations = mutableListOf<Location>()
            val world: World
            val loc1 = location1!!.getSingle(event) ?: return@supplyAsync null
            val loc2 = location2!!.getSingle(event) ?: return@supplyAsync null
            if (loc1.world == null || loc2.world == null) return@supplyAsync null
            if (loc1.world != loc2.world) return@supplyAsync null
            world = loc1.world!!
            val pos1 = BlockVector3.at(loc1.blockX, loc1.blockY, loc1.blockZ)
            val pos2 = BlockVector3.at(loc2.blockX, loc2.blockY, loc2.blockZ)
            val region = CuboidRegion(pos1, pos2)
            for (vector in region) {
                locations.add(Location(world, vector.x().toDouble(), vector.y().toDouble(), vector.z().toDouble()))
            }

            locations.parallelStream().filter {
                if (filterCondition == null) return@filter true
                currentValue = it
                filterCondition!!.check(event)
            }.toList().iterator()
        }.get()
    }

    override fun toString(e: Event?, debug: Boolean): String {
        return "fawe locations from ${location1!!.toString(e, debug)} to ${location2!!.toString(e, debug)}" +
            (filterCondition?.let { " where ${it.toString(e, debug)}" } ?: "")
    }

    // bloat
    override fun get(event: Event?): Array<Location>? = iterator(event)?.let { Iterators.toArray(it, Location::class.java) }
    override fun getDependentInputs(): Set<ExprInput<*>> = dependentInputs
    override fun getReturnType(): Class<out Location> = Location::class.java
    override fun getCurrentValue(): Any? = currentValue
    override fun isSingle(): Boolean = false

}
