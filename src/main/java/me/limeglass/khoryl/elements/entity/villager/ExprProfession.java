package me.limeglass.khoryl.elements.entity.villager;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import me.limeglass.khoryl.lang.EntityPropertyExpression;

@Name("Villager Profession")
@Description("Gets the profession of villagers.")
@Since("1.0.0")
/**
 * Skript "should" have proper support for getting this but at the moment it doesn't.
 * There is currently proper setting of professions using entity data, and that should be used over a changer.
 * Khoryl will also register the classinfo if Skript doesn't.
 */
public class ExprProfession extends EntityPropertyExpression<LivingEntity, Villager, Profession> {

	static {
		register(ExprProfession.class, Profession.class, "[villager] profession", "livingentities");
	}

	@Override
	@Nullable
	protected Profession grab(Villager villager) {
		return villager.getProfession();
	}

	@Override
	protected String getPropertyName() {
		return "villager profession";
	}

}
