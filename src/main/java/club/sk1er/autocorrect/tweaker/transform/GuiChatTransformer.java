package club.sk1er.autocorrect.tweaker.transform;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ListIterator;

public class GuiChatTransformer implements ITransformer {

    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.client.gui.GuiChat"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            String methodName = mapMethodName(classNode, methodNode);

            if (methodName.equalsIgnoreCase("initGui") || methodName.equalsIgnoreCase("func_73866_w_")) {
                ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();

                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();

                    if (node instanceof TypeInsnNode && node.getOpcode() == NEW) {
                        ((TypeInsnNode) node).desc = "club/sk1er/autocorrect/gui/AutoCorrectEnabledInput";
                    } else if (node instanceof MethodInsnNode && node.getOpcode() == INVOKESPECIAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) node;
                        methodInsnNode.owner = "club/sk1er/autocorrect/gui/AutoCorrectEnabledInput";
                        iterator.add(new TypeInsnNode(CHECKCAST, "net/minecraft/client/gui/GuiTextField"));
                    }
                }
            } else if (methodName.equalsIgnoreCase("keyTyped") || methodName.equalsIgnoreCase("func_73869_a")) {
                ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                LabelNode label = new LabelNode();
                boolean injectedEsc = false;

                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();

                    // INVOKEVIRTUAL net/minecraft/client/gui/GuiChat.getSentHistory (I)V

                    if (node instanceof MethodInsnNode && node.getOpcode() == INVOKEVIRTUAL) {
                        MethodInsnNode methNode = ((MethodInsnNode) node);

                        String methName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(methNode.owner, methNode.name, methNode.desc);

                        switch (methName) {
                            case "getSentHistory":
                            case "func_146402_a":
                            case "sendChatMessage":
                            case "func_175275_f": {
                                setupIterator(iterator, label, 3);

                                break;
                            }
                            case "autocompletePlayerNames":
                            case "func_146404_p_": {
                                setupIterator(iterator, label, 2);

                                break;
                            }
                            case "displayGuiScreen":
                            case "func_147108_a": {
                                if (injectedEsc) break;

                                injectedEsc = true;

                                setupIterator(iterator, label, 5);
                            }
                        }
                    } else if (node.getOpcode() == RETURN) {
                        iterator.previous();
                        iterator.add(label);
                        iterator.next();
                    }
                }
            }
        }
    }

    private void setupIterator(ListIterator<AbstractInsnNode> iterator, LabelNode label, int offset) {
        for (int i = 0; i < offset; i++) {
            iterator.previous();
        }

        injectInsns(iterator, label);

        for (int i = 0; i < offset; i++) {
            iterator.next();
        }
    }

    private void injectInsns(ListIterator<AbstractInsnNode> iterator, LabelNode label) {
        /*
        ALOAD 0
        GETFIELD net/minecraft/client/gui/GuiChat.inputField : Lnet/minecraft/client/gui/GuiTextField;
        ILOAD 1
        ILOAD 2
        INVOKEVIRTUAL net/minecraft/client/gui/GuiTextField.textboxKeyTyped (CI)Z
        POP
         */

        if (!(Boolean) (Launch.blackboard.get("fml.deobfuscatedEnvironment"))) {
            iterator.add(new VarInsnNode(ALOAD, 0));
            iterator.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/gui/GuiChat", "field_146415_a", "Lnet/minecraft/client/gui/GuiTextField;"));
            iterator.add(new VarInsnNode(ILOAD, 1));
            iterator.add(new VarInsnNode(ILOAD, 2));
            iterator.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/client/gui/GuiTextField", "func_146201_a", "(CI)Z"));
            iterator.add(new JumpInsnNode(IFNE, label));
        } else {
            iterator.add(new VarInsnNode(ALOAD, 0));
            iterator.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/gui/GuiChat", "inputField", "Lnet/minecraft/client/gui/GuiTextField;"));
            iterator.add(new VarInsnNode(ILOAD, 1));
            iterator.add(new VarInsnNode(ILOAD, 2));
            iterator.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/client/gui/GuiTextField", "textboxKeyTyped", "(CI)Z"));
            iterator.add(new JumpInsnNode(IFNE, label));
        }
    }
}
