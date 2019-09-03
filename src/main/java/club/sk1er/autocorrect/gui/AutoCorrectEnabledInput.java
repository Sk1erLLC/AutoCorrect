package club.sk1er.autocorrect.gui;

import club.sk1er.autocorrect.AutoCorrect;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.MathHelper;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.patterns.PatternRule;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AutoCorrectEnabledInput extends GuiTextField {
    private AtomicBoolean updating = new AtomicBoolean();
    private List<RuleMatch> currentIssues = new ArrayList<>();
    private long lastTypeTime = System.currentTimeMillis();
    private List<String> suggestions = null;
    private RuleMatch currentMatch = null;
    private int selectedSuggestion = -1;
    private final List<String> NO_SUGGESTIONS = Lists.asList("No suggestions!", new String[]{});
    private int lastMouseX = 0;
    private int lastMouseY = 0;

    public AutoCorrectEnabledInput(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
    }

    @Override
    public void drawTextBox() {
        super.drawTextBox();

        if (this.getVisible() && (System.currentTimeMillis() - lastTypeTime) > 1000) {
            updateSpellcheck();
            lastTypeTime += 20000;
        }

        String s = this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());

        if (suggestions != null) {
            String longest = "";

            List<String> realSuggestions = suggestions.size() == 0 ? NO_SUGGESTIONS : suggestions;

            for (String sugg : realSuggestions) {
                if (sugg.length() > longest.length()) longest = sugg;
            }

            int boxWidth = 3 + fontRendererInstance.getStringWidth(longest);
            int boxHeight = 3 + (realSuggestions.size() * (fontRendererInstance.FONT_HEIGHT + 2));

            int bottomY = (this.yPosition + (this.height - 8) / 2) - 5;

            int charOffset = this.cursorPosition - this.lineScrollOffset;
            int x = this.xPosition + fontRendererInstance.getStringWidth(s.substring(0, charOffset));

            Gui.drawRect(x, bottomY - boxHeight, x + boxWidth, bottomY, 0xFF000000);

            int startY = bottomY - 2 - fontRendererInstance.FONT_HEIGHT;

            int mX = Mouse.getEventX() * this.width / Minecraft.getMinecraft().displayWidth;
            int mY = (Minecraft.getMinecraft().currentScreen.height) - Mouse.getEventY() * (Minecraft.getMinecraft().currentScreen.height) / Minecraft.getMinecraft().displayHeight - 1;

            if (mX >= x && mX <= x + boxWidth && mY >= bottomY - boxHeight && mY <= bottomY && mX != lastMouseX && mY != lastMouseY) {
                int insideY = mY - (bottomY - boxHeight);

                int clickIndex = insideY / (fontRendererInstance.FONT_HEIGHT + 4);
                selectedSuggestion = (suggestions.size() - 1) - clickIndex;

                lastMouseX = mX;
                lastMouseY = mY;
            }

            int index = 0;
            for (String sugg : realSuggestions) {
                int color = index == selectedSuggestion ? 0xfff7ec19 : 0xFFEDEDED;

                fontRendererInstance.drawString(sugg, x + 2, startY, color);
                startY -= fontRendererInstance.FONT_HEIGHT + 2;

                index++;
            }
        }

        if (currentIssues != null) {
            int allowedFrom = this.lineScrollOffset;
            int allowedTo = this.lineScrollOffset + s.length();

            int beginX = this.enableBackgroundDrawing ? this.xPosition + 4 : this.xPosition;
            int y = this.enableBackgroundDrawing ? this.yPosition + (this.height - 8) / 2 : this.yPosition;
            y += fontRendererInstance.FONT_HEIGHT + 1;

            for (RuleMatch issue : currentIssues) {
                if (issue.getFromPos() < allowedFrom || issue.getToPos() > allowedTo) continue;

                int offX = fontRendererInstance.getStringWidth(text.substring(0, issue.getFromPos()));
                int xLength = fontRendererInstance.getStringWidth(text.substring(issue.getFromPos(), issue.getToPos()));

                Gui.drawRect(beginX + offX, y, beginX + offX + xLength, y + 1, 0xFFf0240a);
            }
        }
    }

    @Override
    public void setText(String p_146180_1_) {
        super.setText(p_146180_1_);

        currentIssues = null;
        suggestions = null;
        currentMatch = null;
        selectedSuggestion = -1;

        updateSpellcheck();
    }

    @Override
    public void writeText(String p_146191_1_) {
        super.writeText(p_146191_1_);

        if (this.cursorPosition >= this.text.length()) return;

        currentIssues = null;
        suggestions = null;
        currentMatch = null;
        selectedSuggestion = -1;

        updateSpellcheck();
    }

    @Override
    public boolean textboxKeyTyped(char p_146201_1_, int p_146201_2_) {
        if (p_146201_2_ == Keyboard.KEY_TAB || p_146201_2_ == Keyboard.KEY_RETURN && suggestions != null) {
            if (suggestions.size() - 1 < selectedSuggestion || selectedSuggestion < 0) return true;

            completeSuggestion();
            return true;
        }

        boolean typed = super.textboxKeyTyped(p_146201_1_, p_146201_2_);

        if (p_146201_2_ == Keyboard.KEY_SPACE) {
            updateSpellcheck();
        } else if (p_146201_2_ == Keyboard.KEY_UP && suggestions != null) {
            selectedSuggestion = Math.min(selectedSuggestion + 1, suggestions.size() - 1);
            return true;
        } else if (p_146201_2_ == Keyboard.KEY_DOWN && suggestions != null) {
            selectedSuggestion = Math.max(selectedSuggestion - 1, 0);
            return true;
        }

        if (typed) {
            lastTypeTime = System.currentTimeMillis();
        }

        return typed;
    }

    @Override
    public void mouseClicked(int mX, int mY, int button) {
        super.mouseClicked(mX, mY, button);

        boolean flag = mX >= this.xPosition && mX < this.xPosition + this.width && mY >= this.yPosition && mY < this.yPosition + this.height;

        if (suggestions != null) {
            String longest = "";

            for (String sugg : suggestions) {
                if (sugg.length() > longest.length()) longest = sugg;
            }

            int boxWidth = (2 * 2) + fontRendererInstance.getStringWidth(longest);
            int boxHeight = (2 * 2) + (suggestions.size() * (fontRendererInstance.FONT_HEIGHT + 2));

            int bottomY = (this.yPosition + (this.height - 8) / 2) - 4;

            String s = this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
            int charOffset = this.cursorPosition - this.lineScrollOffset;
            int x = this.xPosition + fontRendererInstance.getStringWidth(s.substring(0, charOffset));

            if (mX < x || mX > x + boxWidth || mY < bottomY - boxHeight || mY > bottomY) {
                suggestions = null;
            } else if (selectedSuggestion < suggestions.size() && selectedSuggestion >= 0) {
                completeSuggestion();
            }
        }

        // We're in the text box, we're focused, and we right clicked.
        if (this.isFocused() && flag && button == 1)
        {
            int i = mX - this.xPosition;

            if (this.getEnableBackgroundDrawing())
            {
                i -= 4;
            }

            String s = this.fontRendererInstance.trimStringToWidth(this.getText().substring(this.lineScrollOffset), this.getWidth());
            int clickPosition = this.fontRendererInstance.trimStringToWidth(s, i).length() + this.lineScrollOffset;
            this.setCursorPosition(clickPosition);

            updateSpellcheck();

            generateSuggestions(clickPosition);
        }
    }

    private void completeSuggestion() {
        this.text = this.text.substring(0, currentMatch.getFromPos())
                + suggestions.get(selectedSuggestion)
                + this.text.substring(currentMatch.getToPos());

        currentIssues.removeIf((it) -> it.equals(currentMatch));

        setCursorPosition(currentMatch.getFromPos() + suggestions.get(selectedSuggestion).length());

        currentMatch = null;
        suggestions = null;
        selectedSuggestion = -1;
    }

    @Override
    public void setCursorPosition(int p_146190_1_) {
        super.setCursorPosition(p_146190_1_);

        if (currentMatch != null) {
            if (p_146190_1_ < currentMatch.getFromPos() || p_146190_1_ > currentMatch.getToPos()) {
                currentMatch = null;
                suggestions = null;
                selectedSuggestion = -1;
            }
        } else if (currentIssues != null) {
            generateSuggestions(p_146190_1_);
        }
    }

    private void generateSuggestions(int p_146190_1_) {
        for (RuleMatch match : currentIssues) {
            if (match.getFromPos() <= p_146190_1_ && p_146190_1_ <= match.getToPos()) {
                // Show options popup
                suggestions = match.getSuggestedReplacements().stream().limit(20).collect(Collectors.toList());
                currentMatch = match;
                selectedSuggestion = 0;

                break;
            }
        }
    }

    private void updateSpellcheck() {
        if (updating.compareAndSet(false, true)) {
            new Thread(() -> {
                try {
                    currentIssues = AutoCorrect.INSTANCE.checker.check(getText())
                            .stream()
                            .filter((it) -> {
                                if (it.getRule() instanceof UppercaseSentenceStartRule) return false;

                                if (it.getRule() instanceof PatternRule) {
                                    return !((PatternRule) (it.getRule())).getFullId().startsWith("I_LOWERCASE");
                                }

                                return true;
                            })
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    updating.set(false);
                }
            }).start();
        }
    }
}
