package me.limeglass.fawesk.elements

import ch.njol.skript.Skript
import ch.njol.skript.classes.Changer.ChangeMode
import ch.njol.skript.doc.Examples
import ch.njol.skript.expressions.ExprInput
import ch.njol.skript.lang.Condition
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.InputSource
import ch.njol.skript.lang.InputSource.InputData
import ch.njol.skript.lang.SkriptParser.ParseResult
import ch.njol.skript.lang.parser.ParserInstance
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.skript.util.BlockStateBlock
import ch.njol.skript.util.Direction
import ch.njol.util.Kleenean
import com.fastasyncworldedit.core.FaweAPI
import com.google.common.collect.Iterators
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.world.block.BlockTypes
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.event.Event
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Examples(
	"parallel set fawe blocks from {test1} to {test2} where [block input is not air] to air # Use parallel for unordered large updates",
	"set fawe blocks from {test1} to {test2} to wheat[age=5]",
	"loop fawe blocks within {test1} to {test2} where [block input is a diamond block or a grass block] to air"
)
class ExprBlocks : SimpleExpression<Block>(), InputSource {

	companion object {
		init {
			Skript.registerExpression(ExprBlocks::class.java, Block::class.java, ExpressionType.SIMPLE,
				"(worldedit|fawe) (blocks|things) (within|from) %location% (to|and) %location%",
				"(worldedit|fawe) (blocks|things) (within|from) %location% (to|and) %location% (where|that match) \\[<.+>\\]",
				"[the] (worldedit|fawe) (block|thing)[s] %directions% %locations%",
				"[the] (worldedit|fawe) (block|thing)[s] %directions% %locations% (where|that match) \\[<.+>\\]",
				"[the] [surface:surface [tolerance %number%] of] (worldedit|fawe) (block|thing)[s] in radius %number% around %location%",
				"[the] [surface:surface [tolerance %number%] of] (worldedit|fawe) (block|thing)[s] in radius %number% around %location% (where|that match) \\[<.+>\\]"
			)
			if (!ParserInstance.isRegistered(InputData::class.java))
				ParserInstance.registerData(InputData::class.java) { InputData(ParserInstance.get()) }
		}
	}

	private val dependentInputs = mutableSetOf<ExprInput<Block>>()
	private var location1: Expression<Location>? = null
	private var location2: Expression<Location>? = null
	private var locations: Expression<Location>? = null
	private var tolerance: Expression<Number>? = null
	private var radius: Expression<Number>? = null
	private var filterCondition: Condition? = null
	private var unparsedCondition: String? = null
	private var currentValue: Block? = null
	private var surface = false

	@Suppress("UNCHECKED_CAST")
	override fun init(
		expressions: Array<out Expression<*>?>,
		matchedPattern: Int,
		isDelayed: Kleenean?,
		parseResult: ParseResult
	): Boolean {
		surface = parseResult.hasTag("surface")
		if (matchedPattern < 2) {
			location1 = expressions[0] as Expression<Location>
			location2 = expressions[1] as Expression<Location>
		} else if (matchedPattern < 4) {
			locations = Direction.combine(expressions[0] as Expression<Direction>, expressions[1] as Expression<Location>)
		} else {
			tolerance = expressions[0] as Expression<Number>
			radius = expressions[1] as Expression<Number>
			location1 = expressions[2] as Expression<Location>
		}
		if (matchedPattern == 0 || matchedPattern == 2 || matchedPattern == 4) return true

		unparsedCondition = parseResult.regexes[0].group()
		val inputData = parser.getData(InputData::class.java).apply { source = this@ExprBlocks }
		filterCondition = Condition.parse(unparsedCondition, "Can't understand this condition: $unparsedCondition")
		inputData.source = this
		return filterCondition != null
	}

	override fun iterator(event: Event?): Iterator<Block>? {
		try {
			return CompletableFuture.supplyAsync {
				val vectors = mutableListOf<BlockVector3>()
				val world: World
				if (locations != null) {
					val locations = locations!!.getArray(event)
					if (locations.any { it.world == null }) {
						println("One or more locations have a null world in ExprBlocks. Returning null.")
						return@supplyAsync null
					}
					world = locations.all { it.world == locations.first().world }.let {
						locations.first().world!!
					}
					vectors.addAll(locations.map { BlockVector3.at(it.blockX, it.blockY, it.blockZ) })
				} else if (location2 != null) {
					val loc1 = location1!!.getSingle(event) ?: return@supplyAsync null
					val loc2 = location2!!.getSingle(event) ?: return@supplyAsync null
					if (loc1.world == null || loc2.world == null) return@supplyAsync null
					if (loc1.world != loc2.world) return@supplyAsync null
					world = loc1.world!!
					val pos1 = BlockVector3.at(loc1.blockX, loc1.blockY, loc1.blockZ)
					val pos2 = BlockVector3.at(loc2.blockX, loc2.blockY, loc2.blockZ)
					val region = CuboidRegion(pos1, pos2)
					for (vector in region) {
						vectors.add(BlockVector3.at(vector.x(), vector.y(), vector.z()))
					}
				} else if (radius != null && location1 != null) {
					val loc = location1!!.getSingle(event) ?: return@supplyAsync null
					if (loc.world == null) return@supplyAsync null
					world = loc.world!!
					val radiusValue = radius!!.getSingle(event)?.toInt() ?: return@supplyAsync null
					val toleranceValue = tolerance?.getSingle(event)?.toInt() ?: 5.toInt()
					val center = BlockVector3.at(loc.blockX, loc.blockY, loc.blockZ)
					for (x in -radiusValue..radiusValue) {
						for (y in -radiusValue..radiusValue) {
							for (z in -radiusValue..radiusValue) {
								var distanceSquared = x * x + y * y + z * z
								if (surface) {
									val dx = x
									val dy = y
									val dz = z
									val distanceSquared = dx * dx + dy * dy + dz * dz
									val radiusSquared = radiusValue * radiusValue
									val minRadiusSquared = (radiusValue - toleranceValue).coerceAtLeast(1) * (radiusValue - toleranceValue).coerceAtLeast(1)
									if (distanceSquared in minRadiusSquared..radiusSquared) {
										vectors.add(center.add(x, y, z))
									}
								} else {
									if (distanceSquared <= radiusValue * radiusValue) {
										vectors.add(center.add(x, y, z))
									}
								}
							}
						}
					}
				} else {
					return@supplyAsync null
				}

				val editSession = WorldEdit.getInstance().newEditSessionBuilder().world(BukkitWorld(world)).build()
				try {
					vectors.map { vector ->
						val block = editSession.getBlock(vector)
						val location = BukkitAdapter.adapt(world, vector)
						val blockState = BukkitAdapter.adapt(block).createBlockState().copy(location)
						BlockStateBlock(blockState)
					}.filter {
						if (filterCondition == null) return@filter true
						currentValue = it
						filterCondition!!.check(event)
					}.toList().iterator()
				} finally {
					editSession.close()
				}
			}.get(5, TimeUnit.MINUTES)
		} catch (e: TimeoutException) {
			Skript.error("FAWE took too long to respond. Returning null.")
			return null
		} catch (e: Exception) {
			// WorldEdit can hault when it runs out of memory, so we'll just keep trying.
			return iterator(event)
		}
	}

	override fun acceptChange(change: ChangeMode): Array<out Class<*>?>? =
		if (change == ChangeMode.SET) arrayOf(BlockData::class.java) else null

	override fun change(event: Event?, delta: Array<out Any?>?, mode: ChangeMode) {
		val blockDatas = delta as? Array<out BlockData> ?: return

		CompletableFuture.runAsync {
			val blocksList = iterator(event)?.asSequence()?.toList() ?: return@runAsync
			val world = blocksList.firstOrNull()?.world ?: return@runAsync
			if (!blocksList.all { it.world == world }) {
				Skript.error("All blocks must be in the same world for Fawesk block change expression.")
				return@runAsync
			}

			val vectors = blocksList.map {
				BlockVector3.at(it.location.blockX, it.location.blockY, it.location.blockZ)
			}
			val editSession = WorldEdit.getInstance().newEditSessionBuilder().world(BukkitWorld(world)).build()
			try {
				when (blockDatas.size) {
					1 -> {
						val blockData = blockDatas[0]
						val type = if (blockData.material.isAir) BlockTypes.AIR else BukkitAdapter.adapt(blockData)
						editSession.setBlocks(vectors.toSet(), type)
					}

					vectors.size -> {
						vectors.forEachIndexed { i, vector ->
							val blockData = blockDatas.getOrNull(i) ?: return@forEachIndexed
							val type = if (blockData.material.isAir) BlockTypes.AIR else BukkitAdapter.adapt(blockData)
							editSession.setBlock(vector, type)
						}
					}

					else -> Skript.error("You must provide the same amount of block data as there are blocks to change.")
				}
			} finally {
				editSession.close()
			}
		}
	}

	override fun toString(e: Event?, debug: Boolean): String {
		if (locations != null) return "fawe blocks ${locations!!.toString(e, debug)}"
		if (radius != null) {
			return "fawe blocks in radius ${radius!!.toString(e, debug)} around ${location1!!.toString(e, debug)}" +
				(filterCondition?.let { " where ${it.toString(e, debug)}" } ?: "")
		}
		return "fawe blocks from ${location1!!.toString(e, debug)} to ${location2!!.toString(e, debug)}" +
			(filterCondition?.let { " where ${it.toString(e, debug)}" } ?: "")
	}

	// bloat
	override fun get(event: Event?): Array<Block>? = iterator(event)?.let { Iterators.toArray(it, Block::class.java) }
	override fun isSingle(): Boolean = locations != null && locations!!.isSingle
	override fun getDependentInputs(): Set<ExprInput<Block>> = dependentInputs
	override fun getReturnType(): Class<out Block> = Block::class.java
	override fun getCurrentValue(): Block? = currentValue

}
