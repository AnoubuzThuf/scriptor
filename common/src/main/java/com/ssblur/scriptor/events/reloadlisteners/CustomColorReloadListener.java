package com.ssblur.scriptor.events.reloadlisteners;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.ssblur.scriptor.ScriptorMod;
import com.ssblur.scriptor.color.CustomColors;
import com.ssblur.scriptor.registry.words.WordRegistry;
import com.ssblur.scriptor.word.descriptor.color.CustomColorDescriptor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import oshi.util.tuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomColorReloadListener extends ScriptorReloadListener {
  public static final CustomColorReloadListener INSTANCE = new CustomColorReloadListener();
  public List<Triplet<Integer, String, int[]>> cache;

  public CustomColorReloadListener() {
    super("scriptor/colors");
  }

  @Override
  protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
    cache = new ArrayList<>();
    super.apply(object, resourceManager, profilerFiller);
  }

  @Override
  public void loadResource(ResourceLocation resourceLocation, JsonElement jsonElement) {
    assert jsonElement.isJsonObject();
    var json = jsonElement.getAsJsonObject();
    assert json.has("color");

    var name = resourceLocation.toShortLanguageKey();
    ScriptorMod.INSTANCE.getLOGGER().info("Loaded custom color " + name);
    if(json.get("color") instanceof JsonArray array) {
      int[] colors = array
        .asList()
        .stream()
        .map(element -> Integer.parseInt(element.getAsString(), 16))
        .mapToInt(i -> i)
        .toArray();
      int index = CustomColors.INSTANCE.registerWithEasing(name, colors);
      cache.add(new Triplet<>(index, name, colors));
      WordRegistry.INSTANCE.register(
        "color." + resourceLocation.toShortLanguageKey(),
        new CustomColorDescriptor(name)
      );
    } else if(json.get("color").isJsonPrimitive()) {
      CustomColors.INSTANCE.registerWithEasing(
        name,
        new int[]{
          Integer.parseInt(json.get("color").getAsString(), 16)
        }
      );
      WordRegistry.INSTANCE.register(
        "color." + resourceLocation.toShortLanguageKey(),
        new CustomColorDescriptor(name)
      );
    }
  }
}
