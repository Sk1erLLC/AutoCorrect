package club.sk1er.autocorrect.tweaker.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class GuiTextFieldTransformer implements ITransformer {
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.client.gui.GuiTextField"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        // Make all my fields protected!

        classNode.fields.forEach(field -> {
            field.access = Opcodes.ACC_PUBLIC;
        });
    }
}
