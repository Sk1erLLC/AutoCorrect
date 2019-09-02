package club.sk1er.autocorrect;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.languagetool.JLanguageTool;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.BritishEnglish;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mod(modid = "autocorrect", name = "AutoCorrect", version = "1.0")
public class AutoCorrect {
    @Mod.Instance
    public static AutoCorrect INSTANCE;

    public JLanguageTool checker = new MultiThreadedJLanguageTool(new AmericanEnglish());
}
