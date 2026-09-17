package verdant_leaves.forge;

import net.minecraftforge.fml.common.Mod;

import verdant_leaves.ExampleMod;

@Mod(ExampleMod.MOD_ID)
public final class ExampleModForge {
    public ExampleModForge() {
        // Run our common setup.
        ExampleMod.init();
    }
}
