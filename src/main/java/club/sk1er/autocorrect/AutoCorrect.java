package club.sk1er.autocorrect;

import club.sk1er.modcore.ModCoreInstaller;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.languagetool.JLanguageTool;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.language.AmericanEnglish;

@Mod(modid = "autocorrect", name = "AutoCorrect", version = "1.2")
public class AutoCorrect {
    @Mod.Instance
    public static AutoCorrect INSTANCE;

    public JLanguageTool checker = new MultiThreadedJLanguageTool(new AmericanEnglish());

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModCoreInstaller.initializeModCore(Minecraft.getMinecraft().mcDataDir);
    }
}
