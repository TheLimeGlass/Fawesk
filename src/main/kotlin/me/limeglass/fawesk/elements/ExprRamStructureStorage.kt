package me.limeglass.fawesk.elements

import ch.njol.skript.ScriptLoader
import ch.njol.skript.ScriptLoader.ScriptUnloadEvent
import ch.njol.skript.Skript
import ch.njol.skript.classes.Changer.ChangeMode
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Name
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.Literal
import ch.njol.skript.lang.SkriptParser.ParseResult
import ch.njol.skript.lang.parser.ParserInstance
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.util.Kleenean
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import org.bukkit.event.Event
import org.skriptlang.skript.lang.script.Script
import java.util.concurrent.CopyOnWriteArrayList

@Name("RAM Structure Storage")
@Description(
    "Stores objects in a RAM-based map, allowing retrieval by structure.",
    "Skript is very laggy with local variable lists.",
    "This can slow down high-performance iterations like Fawesk syntaxes.",
    "Use the 'single' option to make the object being retrieved a single object or a list.",
)
class ExprRamStructureStorage : SimpleExpression<Object>() {

    companion object {
        val table: Table<String, String, MutableList<Object>> = HashBasedTable.create()

        init {
            ScriptLoader.eventRegistry().register(FaweskScriptUnloadListener())
            Skript.registerExpression(
                ExprRamStructureStorage::class.java, Object::class.java, ExpressionType.SIMPLE,
                "[fawesk] [:single] ram [structure] storage %strings% [from script %-*string%]",
            )
        }
    }

    private class FaweskScriptUnloadListener : ScriptUnloadEvent {
        override fun onUnload(
            parser: ParserInstance,
            script: Script
        ) {
            val scriptName = script.name()
            val sections = table.row(scriptName).keys.toList()
            for (section in sections) {
                table.remove(scriptName, section)
            }
        }
    }

    private lateinit var keys: Expression<String>
    private lateinit var scriptName: String
    private var single: Boolean = false

    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: ParseResult
    ): Boolean {
        scriptName = if (parser.isActive) parser.currentScript.name() else "global"
        keys = expressions[0] as Expression<String>
        single = parseResult.hasTag("single")
        if (expressions[1] != null) {
            scriptName = (expressions[1] as? Literal<String>)?.getSingle() ?: run {
                Skript.error("Provided script is not a literal.")
                return false
            }
        }
        return true
    }

    override fun get(event: Event?): Array<Object>? {
        val result = keys.getArray(event)
            .mapNotNull { table.get(scriptName, it) }
            .flatten()
            .toTypedArray()
        return result
    }

    override fun acceptChange(mode: ChangeMode): Array<Class<*>>? {
        return when (mode) {
            ChangeMode.SET,
            ChangeMode.ADD,
            ChangeMode.REMOVE,
            ChangeMode.DELETE -> arrayOf(Array<out Object>::class.java)
            else -> null
        }
    }

    override fun change(event: Event?, delta: Array<out Any?>?, mode: ChangeMode) {
        val items = if (mode != ChangeMode.DELETE) delta as Array<out Object> else emptyArray()
        for (section in keys.getArray(event)) {
            when (mode) {
                ChangeMode.SET, ChangeMode.ADD -> {
                    val list = table.get(scriptName, section) ?: CopyOnWriteArrayList<Object>().also {
                        table.put(scriptName, section, it)
                    }
                    if (mode == ChangeMode.SET) list.clear()
                    list.addAll(items)
                }
                ChangeMode.REMOVE -> table.get(scriptName, section)?.removeAll(items)
                ChangeMode.DELETE -> table.remove(scriptName, section)
                else -> {}
            }
        }
    }

    override fun toString(e: Event?, debug: Boolean): String {
        return "fawesk ram storage ${keys.toString(e, debug)}" +
            if (scriptName.isNotEmpty()) " from script $scriptName" else ""
    }

    override fun getReturnType(): Class<out Object> = Object::class.java
    override fun isSingle(): Boolean = single
}