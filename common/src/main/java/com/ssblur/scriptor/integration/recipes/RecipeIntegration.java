package com.ssblur.scriptor.integration.recipes;

import com.ssblur.scriptor.ScriptorMod;
import com.ssblur.scriptor.helpers.SpellbookHelper;
import com.ssblur.scriptor.item.ScriptorItems;
import com.ssblur.scriptor.item.ScriptorTags;
import com.ssblur.scriptor.recipe.SpellbookCloningRecipe;
import com.ssblur.scriptor.recipe.SpellbookDyeingRecipe;
import com.ssblur.scriptor.recipe.SpellbookRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class RecipeIntegration {
  public interface InformationRegistrar {
    void register(ResourceLocation location, List<ItemStack> items, Component... components);
  }

  public interface RecipesHolder {
    List<RecipeHolder<CraftingRecipe>> recipes();
  }

  public interface ShapelessRecipeRegistrar {
    void register(List<Ingredient> ingredients, ItemStack result, ResourceLocation id);
  }

  public static void registerItemInfo(InformationRegistrar informationRegistrar) {
    informationRegistrar.register(
      ScriptorMod.INSTANCE.location("spellbook_info"),
      SpellbookHelper.INSTANCE.getSPELLBOOKS().stream().map(ItemStack::new).toList(),
      Component.translatable("info.scriptor.spellbook_1"),
      Component.translatable("info.scriptor.spellbook_2"),
      Component.translatable("info.scriptor.spellbook_3")
    );

    informationRegistrar.register(
      ScriptorMod.INSTANCE.location("book_of_books_info"),
      List.of(new ItemStack(ScriptorItems.INSTANCE.getBOOK_OF_BOOKS())),
      Component.translatable("info.scriptor.book_of_books_1"),
      Component.translatable("info.scriptor.book_of_books_2"),
      Component.translatable("info.scriptor.book_of_books_3")
    );

    informationRegistrar.register(
      ScriptorMod.INSTANCE.location("bound_tool_info"),
      List.of(
        new ItemStack(ScriptorItems.INSTANCE.getBOUND_SWORD()),
        new ItemStack(ScriptorItems.INSTANCE.getBOUND_AXE().get()),
        new ItemStack(ScriptorItems.INSTANCE.getBOUND_PICKAXE().get()),
        new ItemStack(ScriptorItems.INSTANCE.getBOUND_SHOVEL().get())
      ),
      Component.translatable("info.scriptor.bound_tool_1"),
      Component.translatable("info.scriptor.bound_tool_2")
    );
  }

  public static void registerRecipes(RecipesHolder recipesHolder, ShapelessRecipeRegistrar shapelessRecipeRegistrar) {
    var registrar = ScriptorItems.INSTANCE.getITEMS().getRegistrar();
    recipesHolder.recipes().forEach(holder -> {
        switch (holder.value()) {
          case SpellbookDyeingRecipe recipe -> shapelessRecipeRegistrar.register(
            List.of(
              Ingredient.of(ScriptorTags.INSTANCE.getREADABLE_SPELLBOOKS()),
              recipe.addition
            ),
            recipe.result,
            holder.id()
          );
          case SpellbookCloningRecipe ignored -> SpellbookHelper.INSTANCE.getSPELLBOOKS().forEach(spellbook -> shapelessRecipeRegistrar.register(
            List.of(
              Ingredient.of(spellbook),
              Ingredient.of(new ItemStack(Items.PAPER)),
              Ingredient.of(new ItemStack(Items.PAPER)),
              Ingredient.of(new ItemStack(Items.PAPER)),
              Ingredient.of(new ItemStack(ScriptorItems.INSTANCE.getSPELLBOOK_BINDER()))
            ),
            new ItemStack(spellbook),
            holder.id().withPath(holder.id().getPath() + registrar.getId(spellbook).toLanguageKey())
          ));
          case SpellbookRecipe recipe -> shapelessRecipeRegistrar.register(
            List.of(
              recipe.getBase(),
              recipe.getAddition()
            ),
            recipe.getResult(),
            holder.id()
          );
          default -> {
          }
        }
    });
  }
}
