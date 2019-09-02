package club.sk1er.autocorrect.tweaker.transform;

import org.objectweb.asm.tree.*;

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

                System.out.println("Found initGui");

                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();

                    if (node instanceof TypeInsnNode && node.getOpcode() == NEW) {
                        ((TypeInsnNode) node).desc = "club/sk1er/autocorrect/gui/AutoCorrectEnabledInput";
                        System.out.println("Replaced type");
                    } else if (node instanceof MethodInsnNode && node.getOpcode() == INVOKESPECIAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) node;
                        methodInsnNode.owner = "club/sk1er/autocorrect/gui/AutoCorrectEnabledInput";
                        iterator.add(new TypeInsnNode(CHECKCAST, "net/minecraft/client/gui/GuiTextField"));
                        System.out.println("Replaced invoke");
                    }
                }
            } else if (methodName.equalsIgnoreCase("keyTyped") || methodName.equalsIgnoreCase("func_73869_a")) {
                ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                LabelNode label = new LabelNode();

                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();

                    // INVOKEVIRTUAL net/minecraft/client/gui/GuiChat.getSentHistory (I)V

                    if (node instanceof MethodInsnNode && node.getOpcode() == INVOKEVIRTUAL) {
                        MethodInsnNode methNode = ((MethodInsnNode) node);

                        if (methNode.owner.equals("net/minecraft/client/gui/GuiChat")
                                && (methNode.name.equals("getSentHistory") || methNode.name.equals("func_146402_a"))) {

                            iterator.remove();
                            AbstractInsnNode prev1 = iterator.previous();
                            iterator.remove();
                            AbstractInsnNode prev2 = iterator.previous();
                            iterator.remove();

                            /*
                            ALOAD 0
                            GETFIELD net/minecraft/client/gui/GuiChat.inputField : Lnet/minecraft/client/gui/GuiTextField;
                            ILOAD 1
                            ILOAD 2
                            INVOKEVIRTUAL net/minecraft/client/gui/GuiTextField.textboxKeyTyped (CI)Z
                            POP
                             */
                            iterator.add(new VarInsnNode(ALOAD, 0));
                            iterator.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/gui/GuiChat", "inputField", "Lnet/minecraft/client/gui/GuiTextField;"));
                            iterator.add(new VarInsnNode(ILOAD, 1));
                            iterator.add(new VarInsnNode(ILOAD, 2));
                            iterator.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/client/gui/GuiTextField", "textboxKeyTyped", "(CI)Z"));
                            iterator.add(new JumpInsnNode(IFNE, label));
                            iterator.add(prev2);
                            iterator.add(prev1);
                            iterator.add(methNode);
                            iterator.next();

                            System.out.println("Injected up arrows");
                        } else if (methNode.owner.equals("net/minecraft/client/gui/GuiChat")
                                && (methNode.name.equals("autocompletePlayerNames") || methNode.name.equals("func_146404_p_"))) {
                            iterator.remove();
                            AbstractInsnNode prev = iterator.previous();
                            iterator.remove();

                            /*
                            ALOAD 0
                            GETFIELD net/minecraft/client/gui/GuiChat.inputField : Lnet/minecraft/client/gui/GuiTextField;
                            ILOAD 1
                            ILOAD 2
                            INVOKEVIRTUAL net/minecraft/client/gui/GuiTextField.textboxKeyTyped (CI)Z
                            POP
                             */
                            iterator.add(new VarInsnNode(ALOAD, 0));
                            iterator.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/gui/GuiChat", "inputField", "Lnet/minecraft/client/gui/GuiTextField;"));
                            iterator.add(new VarInsnNode(ILOAD, 1));
                            iterator.add(new VarInsnNode(ILOAD, 2));
                            iterator.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/client/gui/GuiTextField", "textboxKeyTyped", "(CI)Z"));
                            iterator.add(new JumpInsnNode(IFNE, label));
                            iterator.add(prev);
                            iterator.add(methNode);
                            iterator.next();

                            System.out.println("Injected tab");
                        } else if (methNode.owner.equals("net/minecraft/client/gui/GuiChat")
                                && methNode.name.equals("sendChatMessage") || methNode.name.equals("func_175275_f")) {
                            iterator.remove();
                            AbstractInsnNode prev1 = iterator.previous();
                            iterator.remove();
                            AbstractInsnNode prev2 = iterator.previous();
                            iterator.remove();

                            /*
                            ALOAD 0
                            GETFIELD net/minecraft/client/gui/GuiChat.inputField : Lnet/minecraft/client/gui/GuiTextField;
                            ILOAD 1
                            ILOAD 2
                            INVOKEVIRTUAL net/minecraft/client/gui/GuiTextField.textboxKeyTyped (CI)Z
                            POP
                             */
                            iterator.add(new VarInsnNode(ALOAD, 0));
                            iterator.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/gui/GuiChat", "inputField", "Lnet/minecraft/client/gui/GuiTextField;"));
                            iterator.add(new VarInsnNode(ILOAD, 1));
                            iterator.add(new VarInsnNode(ILOAD, 2));
                            iterator.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/client/gui/GuiTextField", "textboxKeyTyped", "(CI)Z"));
                            iterator.add(new JumpInsnNode(IFNE, label));
                            iterator.add(prev2);
                            iterator.add(prev1);
                            iterator.add(methNode);
                            iterator.next();

                            System.out.println("Injected enter" +
                                    "");
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
}
