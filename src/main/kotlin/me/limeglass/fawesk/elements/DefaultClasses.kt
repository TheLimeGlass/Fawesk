package me.limeglass.fawesk.elements

import ch.njol.skript.classes.ClassInfo
import ch.njol.skript.classes.EnumClassInfo
import ch.njol.skript.classes.Parser
import ch.njol.skript.expressions.base.EventValueExpression
import ch.njol.skript.lang.ParseContext
import ch.njol.skript.registrations.Classes
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats

class DefaultClasses {

    companion object {
        @JvmStatic
        fun register() {
            Classes.registerClass(ClassInfo(ClipboardFormat::class.java, "clipboardformat")
                .user("clipboard ?formats?")
                .name("Clipboard Format")
                .defaultExpression(EventValueExpression(BuiltInClipboardFormat::class.java))
                .parser(object : Parser<ClipboardFormat>() {
                    override fun parse(input: String, context: ParseContext): ClipboardFormat? {
                        return try {
                            BuiltInClipboardFormat.valueOf(input.uppercase().replace(" ", "_"))
                        } catch (e: IllegalArgumentException) {
                            ClipboardFormats.findByAlias(input)
                        }
                    }

                    override fun canParse(context: ParseContext): Boolean {
                        return true
                    }

                    override fun toVariableNameString(e: ClipboardFormat): String {
                        return "clipboardformat:" + e.name.lowercase()
                    }

                    override fun toString(e: ClipboardFormat, flags: Int): String {
                        return e.name.lowercase().replace("_", " ")
                    }
                })
            )

            Classes.registerClass(EnumClassInfo(BuiltInClipboardFormat::class.java, "builtinclipboardformat", "built in clipboard format")
                .user("built ?in ?clipboard ?formats?")
                .name("Built In Clipboard Format")
                .defaultExpression(EventValueExpression(BuiltInClipboardFormat::class.java))
            )
        }
    }

}