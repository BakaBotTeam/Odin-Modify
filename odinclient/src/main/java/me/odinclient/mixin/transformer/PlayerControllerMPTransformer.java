package me.odinclient.mixin.transformer;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class PlayerControllerMPTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!"net.minecraft.client.multiplayer.PlayerControllerMP".equals(transformedName)) {
            return basicClass;
        }

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, 0);

        for (MethodNode method : classNode.methods) {
            if ("onPlayerDamageBlock".equals(method.name) && "(Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;)Z".equals(method.desc)) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode) instruction).name.equals("playSound")) {
                        AbstractInsnNode targetNode = instruction.getPrevious().getPrevious();
                        if (targetNode instanceof LdcInsnNode && ((LdcInsnNode) targetNode).cst.equals(4.0F)) {
                            InsnList toInject = new InsnList();
                            toInject.add(new MethodInsnNode(INVOKESTATIC, "NoBreakEffect", "getInstance", "()LNoBreakEffect;", false));
                            toInject.add(new MethodInsnNode(INVOKEVIRTUAL, "NoBreakEffect", "getEnabled", "()Z", false));
                            LabelNode labelNode = new LabelNode();
                            toInject.add(new JumpInsnNode(IFEQ, labelNode));
                            toInject.add(new MethodInsnNode(INVOKESTATIC, "NoBreakEffect", "getInstance", "()LNoBreakEffect;", false));
                            toInject.add(new MethodInsnNode(INVOKEVIRTUAL, "NoBreakEffect", "getNoBreakSound", "()Z", false));
                            toInject.add(new JumpInsnNode(IFEQ, labelNode));
                            method.instructions.insert(targetNode, toInject);
                            method.instructions.insert(instruction, labelNode);
                            break;
                        }
                    }
                }
            }
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
