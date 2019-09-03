package club.sk1er.autocorrect;

import net.minecraftforge.fml.common.Mod;
import org.languagetool.JLanguageTool;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.language.AmericanEnglish;

@Mod(modid = "autocorrect", name = "AutoCorrect", version = "1.0")
public class AutoCorrect {
    @Mod.Instance
    public static AutoCorrect INSTANCE;

    public JLanguageTool checker = new MultiThreadedJLanguageTool(new AmericanEnglish());
}
