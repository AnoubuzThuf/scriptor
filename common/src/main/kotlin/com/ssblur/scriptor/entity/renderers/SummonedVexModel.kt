package com.ssblur.scriptor.entity.renderers

import com.mojang.blaze3d.vertex.PoseStack
import com.ssblur.scriptor.entity.SummonedVex
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.model.ArmedModel
import net.minecraft.client.model.HierarchicalModel
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeDeformation
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.item.ItemStack
import java.util.function.Function


@Environment(EnvType.CLIENT)
class SummonedVexModel(modelPart: ModelPart) : HierarchicalModel<SummonedVex>(Function { resourceLocation: ResourceLocation ->
    RenderType.entityTranslucent(resourceLocation)
}), ArmedModel {
    private val root: ModelPart
    private val body: ModelPart
    private val rightArm: ModelPart
    private val leftArm: ModelPart
    private val rightWing: ModelPart
    private val leftWing: ModelPart
    private val head: ModelPart

    init {
        this.root = modelPart.getChild("root")
        this.body = this.root.getChild("body")
        this.rightArm = this.body.getChild("right_arm")
        this.leftArm = this.body.getChild("left_arm")
        this.rightWing = this.body.getChild("right_wing")
        this.leftWing = this.body.getChild("left_wing")
        this.head = this.root.getChild("head")
    }

    override fun setupAnim(vex: SummonedVex, f: Float, g: Float, h: Float, i: Float, j: Float) {
        this.root().getAllParts().forEach { obj: ModelPart? -> obj!!.resetPose() }
        this.head.yRot = i * (Math.PI / 180.0).toFloat()
        this.head.xRot = j * (Math.PI / 180.0).toFloat()
        val k = Mth.cos(h * 5.5f * (Math.PI / 180.0).toFloat()) * 0.1f
        this.rightArm.zRot = (Math.PI / 5).toFloat() + k
        this.leftArm.zRot = -((Math.PI / 5).toFloat() + k)
        if (vex.isCharging()) {
            this.body.xRot = 0.0f
            this.setArmsCharging(vex.getMainHandItem(), vex.getOffhandItem(), k)
        } else {
            this.body.xRot = (Math.PI / 20).toFloat()
        }

        this.leftWing.yRot =
            1.0995574f + Mth.cos(h * 45.836624f * (Math.PI / 180.0).toFloat()) * (Math.PI / 180.0).toFloat() * 16.2f
        this.rightWing.yRot = -this.leftWing.yRot
        this.leftWing.xRot = 0.47123888f
        this.leftWing.zRot = -0.47123888f
        this.rightWing.xRot = 0.47123888f
        this.rightWing.zRot = 0.47123888f
    }

    private fun setArmsCharging(itemStack: ItemStack, itemStack2: ItemStack, f: Float) {
        if (itemStack.isEmpty() && itemStack2.isEmpty()) {
            this.rightArm.xRot = -1.2217305f
            this.rightArm.yRot = (Math.PI / 12).toFloat()
            this.rightArm.zRot = -0.47123888f - f
            this.leftArm.xRot = -1.2217305f
            this.leftArm.yRot = (-Math.PI / 12).toFloat()
            this.leftArm.zRot = 0.47123888f + f
        } else {
            if (!itemStack.isEmpty()) {
                this.rightArm.xRot = (Math.PI * 7.0 / 6.0).toFloat()
                this.rightArm.yRot = (Math.PI / 12).toFloat()
                this.rightArm.zRot = -0.47123888f - f
            }

            if (!itemStack2.isEmpty()) {
                this.leftArm.xRot = (Math.PI * 7.0 / 6.0).toFloat()
                this.leftArm.yRot = (-Math.PI / 12).toFloat()
                this.leftArm.zRot = 0.47123888f + f
            }
        }
    }

    override fun root(): ModelPart {
        return this.root
    }

    override fun translateToHand(humanoidArm: HumanoidArm, poseStack: PoseStack) {
        val bl = humanoidArm == HumanoidArm.RIGHT
        val modelPart = if (bl) this.rightArm else this.leftArm
        this.root.translateAndRotate(poseStack)
        this.body.translateAndRotate(poseStack)
        modelPart.translateAndRotate(poseStack)
        poseStack.scale(0.55f, 0.55f, 0.55f)
        this.offsetStackPosition(poseStack, bl)
    }

    private fun offsetStackPosition(poseStack: PoseStack, bl: Boolean) {
        if (bl) {
            poseStack.translate(0.046875, -0.15625, 0.078125)
        } else {
            poseStack.translate(-0.046875, -0.15625, 0.078125)
        }
    }

    companion object {
        fun createBodyLayer(): LayerDefinition {
            val meshDefinition = MeshDefinition()
            val partDefinition = meshDefinition.getRoot()
            val partDefinition2 =
                partDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0f, -2.5f, 0.0f))
            partDefinition2.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(0, 0)
                    .addBox(-2.5f, -5.0f, -2.5f, 5.0f, 5.0f, 5.0f, CubeDeformation(0.0f)),
                PartPose.offset(0.0f, 20.0f, 0.0f)
            )
            val partDefinition3 = partDefinition2.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(0, 10)
                    .addBox(-1.5f, 0.0f, -1.0f, 3.0f, 4.0f, 2.0f, CubeDeformation(0.0f))
                    .texOffs(0, 16)
                    .addBox(-1.5f, 1.0f, -1.0f, 3.0f, 5.0f, 2.0f, CubeDeformation(-0.2f)),
                PartPose.offset(0.0f, 20.0f, 0.0f)
            )
            partDefinition3.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create().texOffs(23, 0)
                    .addBox(-1.25f, -0.5f, -1.0f, 2.0f, 4.0f, 2.0f, CubeDeformation(-0.1f)),
                PartPose.offset(-1.75f, 0.25f, 0.0f)
            )
            partDefinition3.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create().texOffs(23, 6)
                    .addBox(-0.75f, -0.5f, -1.0f, 2.0f, 4.0f, 2.0f, CubeDeformation(-0.1f)),
                PartPose.offset(1.75f, 0.25f, 0.0f)
            )
            partDefinition3.addOrReplaceChild(
                "left_wing",
                CubeListBuilder.create().texOffs(16, 14).mirror()
                    .addBox(0.0f, 0.0f, 0.0f, 0.0f, 5.0f, 8.0f, CubeDeformation(0.0f)).mirror(false),
                PartPose.offset(0.5f, 1.0f, 1.0f)
            )
            partDefinition3.addOrReplaceChild(
                "right_wing",
                CubeListBuilder.create().texOffs(16, 14)
                    .addBox(0.0f, 0.0f, 0.0f, 0.0f, 5.0f, 8.0f, CubeDeformation(0.0f)),
                PartPose.offset(-0.5f, 1.0f, 1.0f)
            )
            return LayerDefinition.create(meshDefinition, 32, 32)
        }
    }
}
